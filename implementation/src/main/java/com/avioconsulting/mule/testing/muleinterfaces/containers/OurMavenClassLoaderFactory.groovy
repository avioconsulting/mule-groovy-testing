package com.avioconsulting.mule.testing.muleinterfaces.containers


import org.mule.runtime.module.embedded.internal.classloading.JdkOnlyClassLoaderFactory

// similar to MavenContainerClassLoaderFactory but do more work up front so that we're not
// constantly re-evaluating Maven dependencies
class OurMavenClassLoaderFactory {
    private static final String CE_PATCHES_GROUP_ID = "org.mule.patches"
    private static final String EE_PATCHES_GROUP_ID = "com.mulesoft.mule.patches"
    private final ClassLoader classLoader
    private final List<URL> services
    private final List<URL> patches
    private static final List<String> ANALYTICS_SERVICES = [
            'api-gateway-contract-service',
            'api-gateway-events-collector-service'
    ]

    OurMavenClassLoaderFactory(BaseEngineConfig engineConfig,
                               File muleHomeDirectory,
                               List<Dependency> runtimeDependencyGraph) {
        def bundleDependencies = runtimeDependencyGraph.sort { d1, d2 ->
            if (isPatchDependency(d1)) {
                return -1
            } else if (isPatchDependency(d2)) {
                return 1
            } else {
                return 0
            }
        }
        def serviceDependencies = bundleDependencies.findAll { dep ->
            def file = dep.filename
            // this may seem weird but it's the best intersection of what
            // MavenContainerClassLoaderFactory does
            file.endsWith('.zip') || file.endsWith('-mule-service.jar')
        }
        def filterAnalyticsPluginEnabled = engineConfig.filterEngineExtensions.contains(BaseEngineConfig.ANALYTICS_PLUGIN)
        services = serviceDependencies.collect { svcDep ->
            svcDep.URL
        }
        services.removeAll() { svcUrl ->
            filterAnalyticsPluginEnabled && ANALYTICS_SERVICES.any { analyticsService ->
                svcUrl.toString().contains(analyticsService)
            }
        }
        if (services.any { svcUrl -> svcUrl.toString().contains('mule-service-http-ee') }) {
            // these 2 services collide
            services.removeAll { svcUrl ->
                def svcString = svcUrl.toString()
                svcString.contains('mule-service-http') && !svcString.contains('mule-service-http-ee')
            }
        }
        patches = bundleDependencies.findAll { d ->
            isPatchDependency(d)
        }.collect { d ->
            d.URL
        }
        def urls = (bundleDependencies - serviceDependencies).collect { dep ->
            dep.URL
        }
        urls.add(new URL(new File(muleHomeDirectory,
                                  'conf').toURI().toString() + '/'))
        // we need ourselves to be resolvable for our bridge classes, etc.
        urls.add(OurMavenClassLoaderFactory.protectionDomain.codeSource.location)
        classLoader = new URLClassLoader(urls.toArray(new URL[0]),
                                         JdkOnlyClassLoaderFactory.create())
    }

    private static boolean isPatchDependency(Dependency dependency) {
        def groupId = dependency.groupId
        return groupId == CE_PATCHES_GROUP_ID || groupId == EE_PATCHES_GROUP_ID
    }

    ClassLoader getClassLoader() {
        return classLoader
    }

    List<URL> getServices() {
        return services
    }

    List<URL> getPatches() {
        return patches
    }
}
