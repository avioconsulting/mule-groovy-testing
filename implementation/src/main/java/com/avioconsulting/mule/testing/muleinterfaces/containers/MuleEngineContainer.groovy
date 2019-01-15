package com.avioconsulting.mule.testing.muleinterfaces.containers

import com.avioconsulting.mule.testing.muleinterfaces.MockingConfiguration
import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j2
import org.apache.commons.io.FileUtils

@Log4j2
class MuleEngineContainer {
    private final BaseEngineConfig engineConfig
    private final Object container
    private final Object registryListener
    private final File muleHomeDirectory
    @Lazy
    private static String testingFrameworkVersion = {
        def resourceStream = MuleEngineContainer.getResourceAsStream('/META-INF/maven/com.avioconsulting.mule/testing/pom.properties')
        // if we run the tests for the tests in an IDE, we won't have this but we will 100% of the time
        // if we're a lib from .m2/repo
        if (!resourceStream) {
            return 'DEV_VERSION'
        }
        def props = new Properties()
        props.load(resourceStream)
        props.getProperty('version')
    }()

    private static String getHeader() {
        def message = "AVIO Testing Framework ${testingFrameworkVersion}"
        def headerFooter = '**********************************************************************'
        def carriageReturn = System.lineSeparator()
        [
                carriageReturn + headerFooter, // looks better if starts on its own line
                '*' + message.center(headerFooter.length()-2) + '*',
                headerFooter
        ].join(carriageReturn)
    }

    MuleEngineContainer(BaseEngineConfig engineConfig) {
        log.info getHeader()
        try {
            this.engineConfig = engineConfig
            muleHomeDirectory = new File('.mule')
            def muleStartedFile = new File(muleHomeDirectory,
                                           'mule-started.json')
            String dependencyJsonText = MuleEngineContainer.getResourceAsStream('/mule4_dependencies.json')?.text
            assert dependencyJsonText: 'Unable to find the /mule4_dependencies.json resource. Did you forget to use the dependency-resolver-maven-plugin plugin in your pom to generate it?'
            if (engineConfig.verboseExceptions) {
                System.setProperty('mule.verbose.exceptions',
                                   'true')
            }
            System.setProperty('mule.home',
                               muleHomeDirectory.absolutePath)
            System.setProperty('mule.testingMode',
                               'true')
            log.info "Checking for temporary .mule directory at ${muleHomeDirectory.absolutePath}"
            if (muleHomeDirectory.exists()) {
                log.info "Removing ${muleHomeDirectory.absolutePath} to ensure clean state"
                FileUtils.deleteDirectory(muleHomeDirectory)
            }
            def confDirectory = new File(muleHomeDirectory,
                                         'conf')
            confDirectory.mkdirs()
            createLoggingAndDomainDirectories(confDirectory)
            // clean out apps regardless of whether our .mule directory is already there
            createAppsDirectory()
            def classLoaderFactory = getClassLoaderFactory(engineConfig,
                                                           dependencyJsonText)
            copyServices(classLoaderFactory.services)
            def containerModulesClassLoader = classLoaderFactory.classLoader
            // see FilterOutNonTestingExtensionsClassLoader for why we're doing this
            def containerClassLoader = new FilterOutNonTestingExtensionsClassLoader(containerModulesClassLoader,
                                                                                    engineConfig.filterEngineExtensions)
// work around this - https://jira.apache.org/jira/browse/LOG4J2-2152
            def preserve = Thread.currentThread().contextClassLoader
            registryListener = null
            try {
                Thread.currentThread().contextClassLoader = containerClassLoader
                def containerKlass = containerClassLoader.loadClass('org.mule.runtime.module.launcher.MuleContainer')
                container = containerKlass.newInstance()
                container.start(false)
                muleStartedFile.text = dependencyJsonText
                def registryListenerKlass = containerClassLoader.loadClass('com.avioconsulting.mule.testing.muleinterfaces.MuleRegistryListener')
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

    private void copyServices(List<URL> services) {
        def servicesDir = new File(muleHomeDirectory,
                                   'services')
        log.info 'Copying services {} to {}',
                 services,
                 servicesDir
        services.each { svcUrl ->
            FileUtils.copyFileToDirectory(new File(svcUrl.toURI()),
                                          servicesDir)
        }
    }

    private OurMavenClassLoaderFactory getClassLoaderFactory(BaseEngineConfig engineConfig,
                                                             String dependencyJsonText) {
        log.info 'Building classloader factory'
        def dependencyGraph = new JsonSlurper().parseText(dependencyJsonText).collect { d ->
            Dependency.parse(d)
        }
        new OurMavenClassLoaderFactory(engineConfig,
                                       muleHomeDirectory,
                                       dependencyGraph)
    }

    private void createAppsDirectory() {
        def appsDir = new File(muleHomeDirectory,
                               'apps')
        if (appsDir.exists()) {
            appsDir.deleteDir()
        }
        appsDir.mkdirs()
    }

    private void createLoggingAndDomainDirectories(File confDirectory) {
        // mule won't start without a log4j2 config
        def log4jResource = MuleEngineContainer.getResourceAsStream('/log4j2-for-mule-home.xml')
        assert log4jResource
        def targetFile = new File(confDirectory,
                                  'log4j2.xml')
        targetFile.text = log4jResource.text
        def domainsDir = new File(muleHomeDirectory,
                                  'domains')
        domainsDir.mkdirs()
        // won't start apps without this domain there but it can be empty
        def defaultDomainDir = new File(domainsDir,
                                        'default')
        defaultDomainDir.mkdirs()
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

    BaseEngineConfig getEngineConfig() {
        return engineConfig
    }

    File getMuleHomeDirectory() {
        return muleHomeDirectory
    }
}
