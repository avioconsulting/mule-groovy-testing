package com.avioconsulting.mule.testing.containers

import com.avioconsulting.mule.testing.mulereplacements.MockingConfiguration
import com.avioconsulting.mule.testing.mulereplacements.MuleRegistryListener
import com.avioconsulting.mule.testing.mulereplacements.RuntimeBridgeTestSide
import groovy.util.logging.Log4j2
import org.apache.commons.io.FileUtils
import org.mule.maven.client.api.MavenClient
import org.mule.maven.client.api.MavenClientProvider
import org.mule.maven.client.api.model.BundleDescriptor
import org.mule.maven.client.api.model.BundleScope
import org.mule.maven.client.api.model.MavenConfiguration
import org.mule.runtime.module.embedded.api.Product
import org.mule.runtime.module.embedded.internal.DefaultEmbeddedContainerBuilder
import org.mule.runtime.module.embedded.internal.MavenContainerClassLoaderFactory
import org.mule.runtime.module.embedded.internal.classloading.JdkOnlyClassLoaderFactory

@Log4j2
class MuleEngineContainer {
    BaseEngineConfig getEngineConfig() {
        return engineConfig
    }
    private final BaseEngineConfig engineConfig
    private final Object container
    private final Object registryListener

    MuleEngineContainer(BaseEngineConfig engineConfig) {
        this.engineConfig = engineConfig
        def directory = new File('.mule')
        System.setProperty('mule.home',
                           directory.absolutePath)
        System.setProperty('mule.testingMode',
                           'true')
        log.info "Checking for tempporary .mule directory at ${directory.absolutePath}"
        if (directory.exists()) {
            log.info "Removing ${directory.absolutePath}"
            directory.deleteDir()
        }
        // TODO: Do we need this still?
        System.setProperty('mule.mode.embedded',
                           'true');
        // mule won't start without a log4j2 config
        def log4jResource = MuleEngineContainer.getResource('/log4j2-for-mule-home.xml')
        assert log4jResource
        def confDirectory = new File(directory, 'conf')
        confDirectory.mkdirs()
        def targetFile = new File(confDirectory, 'log4j2.xml')
        FileUtils.copyFile(new File(log4jResource.toURI()),
                           targetFile)
        def domainsDir = new File(directory, 'domains')
        domainsDir.mkdirs()
        // won't start apps without this domain there but it can be empty
        def defaultDomainDir = new File(domainsDir, 'default')
        defaultDomainDir.mkdirs()
        def appsDir = new File(directory, 'apps')
        if (appsDir.exists()) {
            appsDir.deleteDir()
        }
        appsDir.mkdirs()
        def mavenClientProvider = MavenClientProvider.discoverProvider(DefaultEmbeddedContainerBuilder.classLoader)
        // TODO: No hard coding, use Maven settings file??
        def repo = new File('/Users/brady/.m2/repository')
        assert repo.exists()
        def mavenConfig = MavenConfiguration.newMavenConfigurationBuilder()
                .localMavenRepositoryLocation(repo)
                .offlineMode(true)
        // TODO: hard coding
                .userSettingsLocation(new File('/Users/brady/.m2/settings.xml'))
                .build()
        def mavenClient = mavenClientProvider.createMavenClient(mavenConfig)
        def classLoaderFactory = new MavenContainerClassLoaderFactory(mavenClient)
        def services = classLoaderFactory.getServices(engineConfig.muleVersion,
                                                      Product.MULE_EE)
        def servicesDir = new File(directory, 'services')
        services.each { svcUrl ->
            FileUtils.copyFileToDirectory(new File(svcUrl.toURI()),
                                          servicesDir)
        }
        def containerModulesClassLoader = classLoaderFactory.create(engineConfig.muleVersion,
                                                                    Product.MULE_EE,
                                                                    JdkOnlyClassLoaderFactory.create(),
                                                                    directory.toURI().toURL())
        def containerClassLoader = createEmbeddedImplClassLoader(containerModulesClassLoader,
                                                                 mavenClient,
                                                                 engineConfig.muleVersion)
        // work around this - https://jira.apache.org/jira/browse/LOG4J2-2152
        def preserve = Thread.currentThread().contextClassLoader
        registryListener = null
        try {
            Thread.currentThread().contextClassLoader = containerClassLoader
            // TODO: Hard coded name?
            def containerKlass = containerClassLoader.loadClass("org.mule.runtime.module.launcher.MuleContainer")
            container = containerKlass.newInstance()
            container.start(false)
            def registryListenerKlass = containerClassLoader.loadClass(MuleRegistryListener.name)
            registryListener = registryListenerKlass.newInstance()
            container.deploymentService.addDeploymentListener(registryListener)
            assert container
            assert registryListener
        }
        finally {
            Thread.currentThread().contextClassLoader = preserve
        }
    }

    def shutdown() {
        container.shutdown()
    }

    def undeployApplication(RuntimeBridgeTestSide app) {
        container.deploymentService.undeploy(app.artifactName)
    }

    // TODO: Derive artifactName?
    RuntimeBridgeTestSide deployApplication(String artifactName,
                                            URI application,
                                            MockingConfiguration mockingConfiguration) {
        // have to do this before we deploy to catch the event
        registryListener.setMockingConfiguration(artifactName,
                                                 mockingConfiguration)
        // TODO: How do we pass in our properties??
        // TODO: We need the repository directory. The Mule 4.0 build process puts it in target so we should be able to get it easily
        container.deploymentService.deploy(application)
        // this we have to do after the deployment
        def muleSide = registryListener.getRuntimeBridge(artifactName)
        new RuntimeBridgeTestSide(muleSide, null)
    }

    private static ClassLoader createEmbeddedImplClassLoader(ClassLoader parentClassLoader,
                                                             MavenClient mavenClient,
                                                             String muleVersion) throws MalformedURLException {
        def embeddedBomDescriptor = new BundleDescriptor.Builder()
                .setGroupId('org.mule.distributions')
                .setArtifactId('mule-module-embedded-impl-bom')
                .setVersion(muleVersion)
                .setType('pom')
                .build()
        def embeddedImplDescriptor = new BundleDescriptor.Builder()
                .setGroupId('org.mule.distributions')
                .setArtifactId('mule-module-embedded-impl')
                .setVersion(muleVersion)
                .setType('jar')
                .build()
        def embeddedBundleImplDescriptor = mavenClient.resolveBundleDescriptor(embeddedImplDescriptor)
        def embeddedImplDependencies = mavenClient.resolveBundleDescriptorDependencies(false,
                                                                                       embeddedBomDescriptor)
        def embeddedUrls = embeddedImplDependencies.findAll { dep ->
            dep.scope != BundleScope.PROVIDED
        }.collect { dep ->
            dep.bundleUri.toURL()
        }
        embeddedUrls.add(embeddedBundleImplDescriptor.bundleUri.toURL())
        embeddedUrls.add(MuleRegistryListener.protectionDomain.codeSource.location)
        new URLClassLoader(embeddedUrls.toArray(new URL[0]),
                           parentClassLoader)
    }
}
