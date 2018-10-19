package com.avioconsulting.mule.testing.muleinterfaces.containers

import com.avioconsulting.mule.testing.muleinterfaces.MockingConfiguration
import com.avioconsulting.mule.testing.muleinterfaces.MuleRegistryListener
import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
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
import org.mule.runtime.module.launcher.MuleContainer

@Log4j2
class MuleEngineContainer {
    private final BaseEngineConfig engineConfig
    private final Object container
    private final Object registryListener
    private final File muleHomeDirectory

    MuleEngineContainer(BaseEngineConfig engineConfig) {
        try {
            this.engineConfig = engineConfig
            muleHomeDirectory = new File('.mule')
            System.setProperty('mule.home',
                               muleHomeDirectory.absolutePath)
            System.setProperty('mule.testingMode',
                               'true')
            log.info "Checking for temporary .mule directory at ${muleHomeDirectory.absolutePath}"
            if (muleHomeDirectory.exists()) {
                log.info "Removing ${muleHomeDirectory.absolutePath}"
                muleHomeDirectory.deleteDir()
            }
            // mule won't start without a log4j2 config
            def log4jResource = MuleEngineContainer.getResourceAsStream('/log4j2-for-mule-home.xml')
            assert log4jResource
            def confDirectory = new File(muleHomeDirectory, 'conf')
            confDirectory.mkdirs()
            def targetFile = new File(confDirectory, 'log4j2.xml')
            targetFile.text = log4jResource.text
            def domainsDir = new File(muleHomeDirectory, 'domains')
            domainsDir.mkdirs()
            // won't start apps without this domain there but it can be empty
            def defaultDomainDir = new File(domainsDir, 'default')
            defaultDomainDir.mkdirs()
            def appsDir = new File(muleHomeDirectory, 'apps')
            if (appsDir.exists()) {
                appsDir.deleteDir()
            }
            appsDir.mkdirs()
            def mavenClientProvider = MavenClientProvider.discoverProvider(DefaultEmbeddedContainerBuilder.classLoader)
            def m2Directory = new File(System.getProperty('user.home'), '.m2')
            def repo = new File(m2Directory, 'repository')
            // because we run in offline model, see pom.xml
            assert repo.exists() : "If your local Maven repo directory. ${repo}, does not already exist by now, we will not be able to run anyways"
            // the Aether Maven client is not very sophisticated. It attempts to use the 1st profile in settings.xml
            // Therefore we include our dependencies in the testing framework's POM and then rely
            // on Maven to download them before this code even runs. thus forcing offline mode
            def mavenConfig = MavenConfiguration.newMavenConfigurationBuilder()
                    .localMavenRepositoryLocation(repo)
                    // see pom.xml for why we are doing this
                    .offlineMode(true)
                    .build()
            // Mule uses this Maven client to derive its classpath from the local ~/.m2/repository directory
            def mavenClient = mavenClientProvider.createMavenClient(mavenConfig)
            def classLoaderFactory = new MavenContainerClassLoaderFactory(mavenClient)
            def services = classLoaderFactory.getServices(engineConfig.muleVersion,
                                                          Product.MULE_EE)
            def servicesDir = new File(muleHomeDirectory, 'services')
            services.findAll { svc ->
                !svc.toString().contains('api-gateway-contract-service')
            }.each { svcUrl ->
                FileUtils.copyFileToDirectory(new File(svcUrl.toURI()),
                                              servicesDir)
            }
            def containerModulesClassLoader = classLoaderFactory.create(engineConfig.muleVersion,
                                                                        Product.MULE_EE,
                                                                        JdkOnlyClassLoaderFactory.create(),
                                                                        muleHomeDirectory.toURI().toURL())
            def containerClassLoader = createEmbeddedImplClassLoader(containerModulesClassLoader,
                                                                     mavenClient,
                                                                     engineConfig.muleVersion)
            // see FilterOutNonTestingExtensionsClassLoader for why we're doing this
            containerClassLoader = new FilterOutNonTestingExtensionsClassLoader(containerClassLoader,
                                                                                engineConfig.filterEngineExtensions)
            // work around this - https://jira.apache.org/jira/browse/LOG4J2-2152
            def preserve = Thread.currentThread().contextClassLoader
            registryListener = null
            try {
                Thread.currentThread().contextClassLoader = containerClassLoader
                def containerKlass = containerClassLoader.loadClass(MuleContainer.name)
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
        catch (e) {
            log.error 'Unable to load Mule container!',
                      e
            throw e
        }
    }

    def shutdown() {
        container.shutdown()
    }

    def undeployApplication(RuntimeBridgeTestSide app) {
        app.dispose()
        container.deploymentService.undeploy(app.artifactName)
    }

    RuntimeBridgeTestSide deployApplication(String artifactName,
                                            URI application,
                                            MockingConfiguration mockingConfiguration,
                                            Properties properties) {
        // have to do this before we deploy to catch the event
        registryListener.setMockingConfiguration(artifactName,
                                                 mockingConfiguration)
        container.deploymentService.deploy(application,
                                           properties)
        // this we have to do after the deployment
        def muleSide = registryListener.getRuntimeBridge(artifactName)
        new RuntimeBridgeTestSide(muleSide,
                                  artifactName)
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
        // need to be able to at least load our registry listener
        embeddedUrls.add(MuleRegistryListener.protectionDomain.codeSource.location)
        new URLClassLoader(embeddedUrls.toArray(new URL[0]),
                           parentClassLoader)
    }

    BaseEngineConfig getEngineConfig() {
        return engineConfig
    }

    File getMuleHomeDirectory() {
        return muleHomeDirectory
    }
}
