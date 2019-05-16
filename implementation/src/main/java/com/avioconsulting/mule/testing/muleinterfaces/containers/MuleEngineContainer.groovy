package com.avioconsulting.mule.testing.muleinterfaces.containers

import com.avioconsulting.mule.testing.muleinterfaces.MockingConfiguration
import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j2
import org.apache.commons.io.FileUtils

import java.util.zip.ZipInputStream

@Log4j2
class MuleEngineContainer {
    private final BaseEngineConfig engineConfig
    private final Object container
    private final Object deployListener
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
                '*' + message.center(headerFooter.length() - 2) + '*',
                headerFooter
        ].join(carriageReturn)
    }

    MuleEngineContainer(BaseEngineConfig engineConfig) {
        log.info getHeader()
        try {
            this.engineConfig = engineConfig
            muleHomeDirectory = new File('.mule')
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
                def remove = [
                        '.mule',
                        'apps',
                        'lib',
                        'logs',
                        'conf'
                ]
                log.info 'Removing {} from {} to ensure clean state',
                         remove,
                         muleHomeDirectory.absolutePath
                remove.each { dir ->
                    def dirFile = new File(muleHomeDirectory,
                                           dir)
                    FileUtils.deleteDirectory(dirFile)
                }
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
            copyPatches(classLoaderFactory.patches)
            def containerModulesClassLoader = classLoaderFactory.classLoader
            // see FilterOutNonTestingExtensionsClassLoader for why we're doing this
            def containerClassLoader = new FilterOutNonTestingExtensionsClassLoader(containerModulesClassLoader,
                                                                                    engineConfig.filterEngineExtensions)
// work around this - https://jira.apache.org/jira/browse/LOG4J2-2152
            def preserve = Thread.currentThread().contextClassLoader
            deployListener = null
            try {
                Thread.currentThread().contextClassLoader = containerClassLoader
                def containerKlass = containerClassLoader.loadClass('org.mule.runtime.module.launcher.MuleContainer')
                container = containerKlass.newInstance()
                container.start(false)
                def deployListenerKlass = containerClassLoader.loadClass('com.avioconsulting.mule.testing.muleinterfaces.viamuleclassloader.TestingFrameworkDeployListener')
                deployListener = deployListenerKlass.newInstance()
                container.deploymentService.addDeploymentListener(deployListener)
                assert container
                assert deployListener
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
        def extractServices = true
        // our 'cache' from last time, unzipping is slow
        def serviceJsonFile = new File(muleHomeDirectory,
                                       'services.json')
        def desiredServiceJson = JsonOutput.toJson(services.sort())
        if (servicesDir.exists() && serviceJsonFile.exists()) {
            if (serviceJsonFile.text == desiredServiceJson) {
                extractServices = false
            }
        }
        if (servicesDir.exists() && extractServices) {
            log.info 'Existing services directory is out of date, cleaning out'
            FileUtils.deleteDirectory(servicesDir)
            servicesDir.mkdirs()
        }
        if (!extractServices) {
            log.info 'Using existing services'
            return
        }

        log.info 'Unzipping services {} to {}',
                 services,
                 servicesDir
        // Mule 4.1.x allowed placing the services JAR in here
        // 4.2.0 needs them expanded
        services.each { svcUrl ->
            def serviceFile = new File(svcUrl.toURI())
            def serviceDestDir = new File(servicesDir,
                                          serviceFile.name.replace('-mule-service.jar',
                                                                   ''))
            def zipStream = new ZipInputStream(new FileInputStream(serviceFile))
            def zipEntry = zipStream.nextEntry
            def buffer = new byte[1024]
            while (zipEntry != null) {
                if (!zipEntry.directory) {
                    def destFile = new File(serviceDestDir,
                                            zipEntry.name)
                    destFile.parentFile.mkdirs()
                    def fos = new FileOutputStream(destFile)
                    int len
                    while ((len = zipStream.read(buffer)) > 0) {
                        fos.write(buffer,
                                  0,
                                  len)
                    }
                    fos.close()
                }
                zipEntry = zipStream.getNextEntry()
            }
            zipStream.closeEntry()
            zipStream.close()
        }
        serviceJsonFile.text = desiredServiceJson
    }

    private void copyPatches(List<URL> patches) {
        def libDir = new File(muleHomeDirectory,
                              'lib')
        def patchesDir = new File(libDir,
                                  'patches')
        patchesDir.mkdirs()
        log.info 'Copying patches {} to {}',
                 patches,
                 patchesDir
        patches.each { patchUrl ->
            FileUtils.copyFileToDirectory(new File(patchUrl.toURI()),
                                          patchesDir)
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
        deployListener.setMockingConfiguration(artifactName,
                                               mockingConfiguration)
        container.deploymentService.deploy(application,
                                           properties)
        // this we have to do after the deployment
        def muleSide = deployListener.getRuntimeBridge(artifactName)
        def testSide = new RuntimeBridgeTestSide(muleSide,
                                                 artifactName,
                                                 mockingConfiguration)
        // if lazy init is on, then the source someone wants enabled won't be on without this
        if (mockingConfiguration.lazyInitEnabled) {
            testSide.startMessageSourceFlows()
        }
        testSide
    }

    BaseEngineConfig getEngineConfig() {
        return engineConfig
    }

    File getMuleHomeDirectory() {
        return muleHomeDirectory
    }
}
