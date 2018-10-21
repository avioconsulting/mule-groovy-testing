package com.avioconsulting.mule.testing.muleinterfaces.containers

import com.avioconsulting.mule.testing.muleinterfaces.MuleRegistryListener
import groovy.json.JsonSlurper
import org.mule.runtime.module.embedded.internal.classloading.JdkOnlyClassLoaderFactory

// similar to MavenContainerClassLoaderFactory but do more work up front so that we're not
// constantly re-evaluating Maven dependencies
class OurMavenClassLoaderFactory {
    private static final String CE_PATCHES_GROUP_ID = "org.mule.patches"
    private static final String EE_PATCHES_GROUP_ID = "com.mulesoft.mule.patches"
    private final ClassLoader classLoader
    private final List<URL> services

    OurMavenClassLoaderFactory(BaseEngineConfig engineConfig,
                               File repoDirectory,
                               File muleHomeDirectory) {
        def dependencyGraph = getDependencyGraph()
        def eeDeps = flattenDependencies("com.mulesoft.mule.distributions:mule-runtime-impl-bom:${engineConfig.muleVersion}",
                                         dependencyGraph)
        def embeddedDeps = flattenDependencies("org.mule.distributions:mule-module-embedded-impl:${engineConfig.muleVersion}",
                                               dependencyGraph)
        def bundleDependencies = (eeDeps + embeddedDeps).sort { d1, d2 ->
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

    Map<String, Dependency> getDependencyGraph() {
        def rawMap = new JsonSlurper().parse(new File('dependencies.json'))
        rawMap.collectEntries { key, map ->
            [
                    key,
                    Dependency.parse(key,
                                     map)
            ]
        } as Map<String, Dependency>
    }

    List<Dependency> flattenDependencies(String key,
                                         Map<String, Dependency> dependencyGraph,
                                         Map<String, Dependency> totals = [:],
                                         boolean recurse = false) {
        def root = dependencyGraph[key]
        assert root: "Unable to find expected key ${key} in ${dependencyGraph}"
        totals[key] = root
        root.dependencies.each { depKey ->
            if (!totals.containsKey(depKey)) {
                flattenDependencies(depKey,
                                    dependencyGraph,
                                    totals,
                                    true)
            }
        }
        recurse ? null : totals.values().toList()
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
