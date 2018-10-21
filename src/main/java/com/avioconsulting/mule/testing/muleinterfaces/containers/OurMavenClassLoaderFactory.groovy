package com.avioconsulting.mule.testing.muleinterfaces.containers

import com.avioconsulting.mule.testing.muleinterfaces.MuleRegistryListener
import groovy.json.JsonSlurper
import org.mule.runtime.module.embedded.api.Product
import org.mule.runtime.module.embedded.internal.classloading.JdkOnlyClassLoaderFactory

// similar to MavenContainerClassLoaderFactory but do more work up front so that we're not
// constantly re-evaluating Maven dependencies
class OurMavenClassLoaderFactory {
    private static final String CE_PATCHES_GROUP_ID = "org.mule.patches"
    private static final String EE_PATCHES_GROUP_ID = "com.mulesoft.mule.patches"
    private final ClassLoader classLoader
    private final List<URL> services

    OurMavenClassLoaderFactory(BaseEngineConfig engineConfig,
                               Product product,
                               File repoDirectory,
                               File muleHomeDirectory) {
        def descriptor = getContainerBomBundleDescriptor(engineConfig.muleVersion,
                                                         product)
        def bundleDependencies = getDependencies(descriptor)
        bundleDependencies = bundleDependencies.sort { d1, d2 ->
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
            svcDep.getFullFilePath(repoDirectory)
        }
        services.removeAll() { svcUrl ->
            filterAnalyticsPluginEnabled && svcUrl.toString().contains('api-gateway-contract-service')
        }
        def urls = (bundleDependencies - serviceDependencies).collect { dep ->
            dep.getFullFilePath(repoDirectory)
        }
        urls.add(new URL(new File(muleHomeDirectory, 'conf').toURI().toString() + '/'))
        urls.add(MuleRegistryListener.protectionDomain.codeSource.location)
        classLoader = new URLClassLoader(urls.toArray(new URL[0]),
                                         JdkOnlyClassLoaderFactory.create())
    }

    List<Dependency> getDependencies(String descriptorKey) {
        def rawMap = new JsonSlurper().parse(new File('dependencies.json'))
        def asObjects = rawMap.collectEntries { key, map ->
            [
                    key,
                    Dependency.parse(key,
                                     map)
            ]
        } as Map<String, Dependency>
        Map<Dependency, Integer> totals = [:]
        flattenDependencies(descriptorKey,
                            asObjects,
                            totals)
        totals.keySet().toList()

    }

    def flattenDependencies(String key,
                            Map<String, Dependency> allResults,
                            Map<Dependency, Integer> totals) {
        def root = allResults[key]
        assert root: "Unable to find expected key ${key} in ${allResults}"
        totals[root] = 1
        root.dependencies.each { depKey ->
            flattenDependencies(depKey,
                                allResults,
                                totals)
        }
        totals
    }


    private static String getContainerBomBundleDescriptor(String version,
                                                          Product product) {
        "com.mulesoft.mule.distributions:mule-runtime-impl-bom:4.1.3"
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
}
