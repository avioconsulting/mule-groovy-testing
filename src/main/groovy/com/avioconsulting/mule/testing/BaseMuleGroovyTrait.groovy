package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.batch.BatchWaitUtil
import com.avioconsulting.mule.testing.dsl.invokers.*
import com.avioconsulting.mule.testing.dsl.mocking.*
import com.avioconsulting.mule.testing.dsl.mocking.sfdc.Choice
import com.avioconsulting.mule.testing.dsl.mocking.sfdc.ChoiceImpl
import com.avioconsulting.mule.testing.mocks.StandardMock
import com.avioconsulting.mule.testing.mulereplacements.MockingConfiguration
import com.avioconsulting.mule.testing.mulereplacements.MuleRegistryListener
import com.avioconsulting.mule.testing.mulereplacements.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.payloadvalidators.SOAPPayloadValidator
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.NotImplementedException
import org.apache.logging.log4j.Logger
import org.mule.maven.client.api.MavenClient
import org.mule.maven.client.api.MavenClientProvider
import org.mule.maven.client.api.model.BundleDescriptor
import org.mule.maven.client.api.model.BundleScope
import org.mule.maven.client.api.model.MavenConfiguration
import org.mule.runtime.module.embedded.api.Product
import org.mule.runtime.module.embedded.internal.DefaultEmbeddedContainerBuilder
import org.mule.runtime.module.embedded.internal.MavenContainerClassLoaderFactory
import org.mule.runtime.module.embedded.internal.classloading.JdkOnlyClassLoaderFactory

import static java.lang.System.setProperty

// basic idea here is to have a trait that could be mixed in to any type of testing framework situation
// this trait should be stateless
trait BaseMuleGroovyTrait {
    abstract Logger getLogger()

    RuntimeBridgeTestSide createMuleContext(MockingConfiguration mockingConfiguration) {
        def directory = new File('.mule')
        System.setProperty('mule.home',
                           directory.absolutePath)
        logger.info "Checking for tempporary .mule directory at ${directory.absolutePath}"
        if (directory.exists()) {
            logger.info "Removing ${directory.absolutePath}"
            directory.deleteDir()
        }
        setProperty("mule.mode.embedded", "true");
        // mule won't start without a log4j2 config
        def log4jResource = BaseMuleGroovyTrait.getResource('/log4j2-for-mule-home.xml')
        assert log4jResource
        def confDirectory = new File(directory, 'conf')
        confDirectory.mkdirs()
        def targetFile = new File(confDirectory, 'log4j2.xml')
        FileUtils.copyFile(new File(log4jResource.toURI()),
                           targetFile)
        def domainsDir = new File(directory, 'domains')
        domainsDir.mkdirs()
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
        def services = classLoaderFactory.getServices('4.1.2',
                                                      Product.MULE_EE)
        def servicesDir = new File(directory, 'services')
        services.each { svcUrl ->
            FileUtils.copyFileToDirectory(new File(svcUrl.toURI()),
                                          servicesDir)
        }
        // TODO: mule version hard coded?
        def containerModulesClassLoader = classLoaderFactory.create('4.1.2',
                                                                    Product.MULE_EE,
                                                                    JdkOnlyClassLoaderFactory.create(),
                                                                    directory.toURI().toURL())
        def containerClassLoader = createEmbeddedImplClassLoader(containerModulesClassLoader,
                                                                 mavenClient,
                                                                 '4.1.2')
        // work around this - https://jira.apache.org/jira/browse/LOG4J2-2152
        def preserve = Thread.currentThread().contextClassLoader
        Object container = null
        Object registryListener = null
        Object muleSide = null
        try {
            Thread.currentThread().contextClassLoader = containerClassLoader
            // TODO: Hard coded name?
            def containerKlass = containerClassLoader.loadClass("org.mule.runtime.module.launcher.MuleContainer")
            container = containerKlass.newInstance()
            container.start(false)
            def registryListenerKlass = containerClassLoader.loadClass(MuleRegistryListener.name)
            // TODO: Mule predictably does not like a Groovy based mocking config coming in here. Even if GroovyObject was taken care of, we will probably have a class/classpath mismatch. Groovy/dynamic might still be the easiest way to deal with this without completely rewriting the testing framework
            registryListener = registryListenerKlass.newInstance()
            registryListener.mockingConfiguration = mockingConfiguration
            container.deploymentService.addDeploymentListener(registryListener)
            assert container
            assert registryListener
            // TODO: Hard coded app (also can domain be created as a dir beforehand so we don't have to deploy it?). see embedded controller
            // won't start apps without this domain there but it can be empty
            container.deploymentService.deployDomain(new File('src/test/resources/default').toURI())
            // TODO: How do we pass in our properties??
            // TODO: We need the repository directory. The Mule 4.0 build process puts it in target so we should be able to get it easily
            container.deploymentService.deploy(new File('src/test/resources/41test').toURI())
            // TODO: Hard code
            muleSide = registryListener.runtimeBridge
        }
        finally {
            Thread.currentThread().contextClassLoader = preserve
        }
        new RuntimeBridgeTestSide(muleSide)
    }

    private ClassLoader createEmbeddedImplClassLoader(ClassLoader parentClassLoader,
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

    Properties getStartUpProperties() {
        // MUnit Maven plugin uses this technique to avoid a license just to run unit tests
        System.setProperty('mule.testingMode',
                           'true')
        def properties = new Properties()
        // in case a Groovy/GStringImpl is in here
        def onlyJavaStrings = propertyMap.collectEntries { key, value ->
            [(key.toString()): value.toString()]
        }
        properties.putAll onlyJavaStrings
        // verbose in testing is good
        properties.put('mule.verbose.exceptions', true)
        properties
    }

    def getPropertyMap() {
        [:]
    }

    // TODO: Figure out how to do this w/ Mule 4
    List<String> keepListenersOnForTheseFlows() {
        []
    }

    Map<String, String> getConfigResourceSubstitutes() {
        [:]
    }

    String getMuleDeployPropertiesResources() {
        def muleDeployProperties = new Properties()
        def url = BaseMuleGroovyTrait.getResource('/mule-deploy.properties')
        assert url: 'Expected mule-deploy.properties to exist!'
        def propsFile = new File(url.toURI())
        def inputStream = propsFile.newInputStream()
        muleDeployProperties.load(inputStream)
        inputStream.close()
        muleDeployProperties.getProperty('config.resources')
    }

    String getConfigResources() {
        def mapping = configResourceSubstitutes
        def list = muleDeployPropertiesResources.split(',').collect { p ->
            def xmlEntry = p.trim()
            if (!mapping.containsKey(xmlEntry)) {
                return xmlEntry
            }
            def value = mapping[xmlEntry]
            value ?: null
        } - null
        list.join(',')
    }

    def runFlow(RuntimeBridgeTestSide muleContext,
                String flowName,
                @DelegatesTo(FlowRunner) Closure closure) {
        def flow = muleContext.getFlow(flowName)
        def runner = new FlowRunnerImpl(muleContext,
                                        flow,
                                        flowName)
        def code = closure.rehydrate(runner, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        def outputEvent = runFlow(muleContext,
                                  flowName,
                                  runner.getEvent())
        runner.transformOutput(outputEvent)
    }

    EventWrapper runSoapApikitFlow(RuntimeBridgeTestSide muleContext,
                                   String operation,
                                   String apiKitFlowName = 'api-main',
                                   @DelegatesTo(SoapInvoker) Closure closure) {
        def invoker = new SoapApikitInvokerImpl(muleContext,
                                                muleContext,
                                                apiKitFlowName,
                                                operation)
        def code = closure.rehydrate(invoker, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        def event = invoker.event
        runFlow(muleContext,
                apiKitFlowName,
                event)
    }

    EventWrapper runFlow(RuntimeBridgeTestSide muleContext,
                         String flowName,
                         EventWrapper event) {
        def flow = muleContext.getFlow(flowName)
        flow.process(event)
    }

    def waitForBatchCompletion(RuntimeBridgeTestSide muleContext,
                               List<String> jobsToWaitFor = null,
                               boolean throwUnderlyingException = false,
                               Closure closure) {
        def batchWaitUtil = new BatchWaitUtil(muleContext)
        batchWaitUtil.waitFor(jobsToWaitFor, throwUnderlyingException, closure)
    }

    def runBatch(RuntimeBridgeTestSide muleContext,
                 String batchName,
                 List<String> jobsToWaitFor = null,
                 boolean throwUnderlyingException = false,
                 @DelegatesTo(BatchRunner) Closure closure) {
        throw new NotImplementedException()
//        def runner = new FlowRunnerImpl(muleContext,
//                                        null,// batch doesn't inherit from flow
//                                        batchName)
//        def code = closure.rehydrate(runner, this, this)
//        code.resolveStrategy = Closure.DELEGATE_ONLY
//        code()
//        def batchJob = muleContext.registry.get(batchName) as BatchJobAdapter
//        waitForBatchCompletion(muleContext,
//                               jobsToWaitFor,
//                               throwUnderlyingException) {
//            batchJob.execute(runner.getEvent())
//        }
    }

    def mockRestHttpCall(MockingConfiguration mockingConfiguration,
                         RuntimeBridgeTestSide muleContext,
                         String connectorName,
                         @DelegatesTo(HttpRequestResponseChoice) Closure closure) {
        def formatterChoice = new HttpRequestResponseChoiceImpl(muleContext)
        def code = closure.rehydrate(formatterChoice, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        mockingConfiguration.addMock(connectorName,
                                     formatterChoice.transformer)
    }

    def mockVmReceive(MockingConfiguration mockingConfiguration,
                      RuntimeBridgeTestSide muleContext,
                      String connectorName,
                      @DelegatesTo(StandardRequestResponse) Closure closure) {
        def formatterChoice = new VMRequestResponseChoiceImpl(muleContext)
        def code = closure.rehydrate(formatterChoice, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        def mock = new StandardMock(formatterChoice.transformer)
        mockingConfiguration.addMock(connectorName,
                                     mock)
    }

    def mockGeneric(MockingConfiguration mockingConfiguration,
                    RuntimeBridgeTestSide muleContext,
                    String connectorName,
                    @DelegatesTo(StandardRequestResponse) Closure closure) {
        def formatterChoice = new GenericRequestResponseChoiceImpl(muleContext)
        def code = closure.rehydrate(formatterChoice, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        def mock = new StandardMock(formatterChoice.transformer)
        mockingConfiguration.addMock(connectorName,
                                     mock)
    }

    def mockSalesForceCall(MockingConfiguration mockingConfiguration,
                           RuntimeBridgeTestSide muleContext,
                           String connectorName,
                           @DelegatesTo(Choice) Closure closure) {
        def choice = new ChoiceImpl(muleContext,
                                    muleContext)
        def code = closure.rehydrate(choice, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        mockingConfiguration.addMock(connectorName,
                                     choice.mock)
    }

    def mockSoapCall(MockingConfiguration mockingConfiguration,
                     RuntimeBridgeTestSide muleContext,
                     String connectorName,
                     @DelegatesTo(SOAPFormatter) Closure closure) {
        def payloadValidator = new SOAPPayloadValidator()
        def soapFormatter = new SOAPFormatterImpl(muleContext,
                                                  payloadValidator)
        def code = closure.rehydrate(soapFormatter, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        def mock = new StandardMock(soapFormatter.transformer)
        mockingConfiguration.addMock(connectorName,
                                     mock)
    }
}
