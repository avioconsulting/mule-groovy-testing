package com.avioconsulting.mule.testing.muleinterfaces.containers

import com.avioconsulting.mule.testing.muleinterfaces.MuleRegistryListener
import org.apache.commons.io.FileUtils
import org.mule.maven.client.api.MavenClient
import org.mule.maven.client.api.model.BundleDependency
import org.mule.maven.client.api.model.BundleDescriptor
import org.mule.runtime.module.embedded.api.Product
import org.mule.runtime.module.embedded.internal.classloading.JdkOnlyClassLoaderFactory

// similar to MavenContainerClassLoaderFactory but do more work up front so that we're not
// constantly re-evaluating Maven dependencies
class OurMavenClassLoaderFactory {
    private static final String CE_PATCHES_GROUP_ID = "org.mule.patches"
    private static final String EE_PATCHES_GROUP_ID = "com.mulesoft.mule.patches"
    private final ClassLoader classLoader
    private final List<URL> services

    OurMavenClassLoaderFactory(MavenClient mavenClient,
                               BaseEngineConfig engineConfig,
                               Product product,
                               File muleHomeDirectory) {
        def descriptor = getContainerBomBundleDescriptor(engineConfig.muleVersion,
                                                         product)
        def bundleDependencies = mavenClient.resolveBundleDescriptorDependencies(false,
                                                                                 descriptor)
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
            def file = FileUtils.toFile(dep.bundleUri.toURL()).path.toLowerCase()
            // this may seem weird but it's the best intersection of what
            // MavenContainerClassLoaderFactory does
            file.endsWith('.zip') || file.endsWith('-mule-service.jar')
        }
        def filterAnalyticsPluginEnabled = engineConfig.filterEngineExtensions.contains(BaseEngineConfig.ANALYTICS_PLUGIN)
        services = serviceDependencies.collect { svcDep ->
            svcDep.bundleUri.toURL()
        }
        services.removeAll() { svcUrl ->
            filterAnalyticsPluginEnabled && svcUrl.toString().contains('api-gateway-contract-service')
        }
        def urls = (bundleDependencies - serviceDependencies).collect { dep ->
            dep.bundleUri.toURL()
        }
        urls.add(new URL(new File(muleHomeDirectory, 'conf').toURI().toString() + '/'))
        urls.add(MuleRegistryListener.protectionDomain.codeSource.location)
        classLoader = new URLClassLoader(urls.toArray(new URL[0]),
                                         JdkOnlyClassLoaderFactory.create())
    }

    private static BundleDescriptor getContainerBomBundleDescriptor(String version,
                                                                    Product product) {
        return new BundleDescriptor.Builder()
                .setGroupId(product == Product.MULE ? 'org.mule.distributions' : 'com.mulesoft.mule.distributions')
                .setArtifactId(product == Product.MULE ? 'mule-runtime-impl-bom' : 'mule-runtime-impl-bom')
                .setVersion(version)
                .setType('pom').build()
    }

    private static boolean isPatchDependency(BundleDependency dependency) {
        def groupId = dependency.descriptor.groupId
        return groupId == CE_PATCHES_GROUP_ID || groupId == EE_PATCHES_GROUP_ID
    }

    ClassLoader getClassLoader() {
        return classLoader
    }

    List<URL> getServices() {
        return services
    }
}
