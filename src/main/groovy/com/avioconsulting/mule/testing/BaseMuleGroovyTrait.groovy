package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.batch.BatchWaitUtil
import com.avioconsulting.mule.testing.dsl.invokers.*
import com.avioconsulting.mule.testing.dsl.mocking.*
import com.avioconsulting.mule.testing.dsl.mocking.sfdc.Choice
import com.avioconsulting.mule.testing.dsl.mocking.sfdc.ChoiceImpl
import com.avioconsulting.mule.testing.mocks.StandardMock
import com.avioconsulting.mule.testing.mulereplacements.ContainerContainer
import com.avioconsulting.mule.testing.mulereplacements.MockingConfiguration
import com.avioconsulting.mule.testing.mulereplacements.MuleRegistryListener
import com.avioconsulting.mule.testing.payloadvalidators.SOAPPayloadValidator
import org.apache.commons.lang.NotImplementedException
import org.apache.logging.log4j.Logger
import org.mule.maven.client.api.model.MavenConfiguration
import org.mule.runtime.core.api.construct.Flow
import org.mule.runtime.core.api.event.CoreEvent
import org.mule.runtime.module.embedded.api.ArtifactConfiguration
import org.mule.runtime.module.embedded.api.ContainerConfiguration
import org.mule.runtime.module.embedded.api.EmbeddedContainer
import org.mule.runtime.module.embedded.api.Product

// basic idea here is to have a trait that could be mixed in to any type of testing framework situation
// this trait should be stateless
trait BaseMuleGroovyTrait {
    abstract Logger getLogger()

    ContainerContainer createMuleContext(MockingConfiguration mockingConfiguration) {
        def directory = new File('.mule')
        System.setProperty('mule.home',
                           directory.absolutePath)
        logger.info "Checking for tempporary .mule directory at ${directory.absolutePath}"
        if (directory.exists()) {
            logger.info "Removing ${directory.absolutePath}"
            directory.deleteDir()
        }
        def containerConfig = ContainerConfiguration.builder()
                .containerFolder(directory)
                .build()
        // TODO: No hard coding, use Maven settings file??
        def repo = new File('/Users/brady/.m2/repository')
        assert repo.exists()
        def mavenConfiguration = MavenConfiguration.newMavenConfigurationBuilder()
                .localMavenRepositoryLocation(repo)
        // TODO: hard coding
                .userSettingsLocation(new File('/Users/brady/.m2/settings.xml'))
                .build()
        def container = EmbeddedContainer.builder()
        // TODO: Where to get this version? Project POM somehow??
                .muleVersion('4.1.2')
                .product(Product.MULE_EE)
                .log4jConfigurationFile(BaseMuleGroovyTrait.getResource('/log4j2-for-mule-home.xml').toURI())
                .mavenConfiguration(mavenConfiguration)
                .containerConfiguration(containerConfig)
                .build()
        container.start()
        // TODO: Need to get this dynamically
        def artifactConfig = ArtifactConfiguration.builder()
                .artifactLocation(new File('src/test/resources/41test'))
                .build()
        def registryListener = new MuleRegistryListener()
//        container.deploymentService.addDeploymentListener(registryListener)
        container.deploymentService.deployApplication(artifactConfig)
        new ContainerContainer(registryListener.registry,
                               null)
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

    def runFlow(ContainerContainer muleContext,
                String flowName,
                @DelegatesTo(FlowRunner) Closure closure) {
        def flow = muleContext.registry.lookupByName(flowName) as Optional<Flow>
        assert flow.present
        def runner = new FlowRunnerImpl(muleContext,
                                        flow.get(),
                                        flowName)
        def code = closure.rehydrate(runner, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        def outputEvent = runFlow(muleContext,
                                  flowName,
                                  runner.getEvent())
        runner.transformOutput(outputEvent)
    }

    CoreEvent runSoapApikitFlow(ContainerContainer muleContext,
                                String operation,
                                String apiKitFlowName = 'api-main',
                                @DelegatesTo(SoapInvoker) Closure closure) {
        def eventFactory = new EventFactoryImpl(muleContext)
        def invoker = new SoapApikitInvokerImpl(muleContext,
                                                eventFactory,
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

    CoreEvent runFlow(ContainerContainer muleContext,
                      String flowName,
                      CoreEvent event) {
        def flowOpt = muleContext.registry.lookupByName(flowName) as Optional<Flow>
        assert flowOpt.present: "Flow with name '${flowName}' was not found. Are you using the right flow name?"
        flowOpt.get().process(event)
    }

    def waitForBatchCompletion(ContainerContainer muleContext,
                               List<String> jobsToWaitFor = null,
                               boolean throwUnderlyingException = false,
                               Closure closure) {
        def batchWaitUtil = new BatchWaitUtil(muleContext)
        batchWaitUtil.waitFor(jobsToWaitFor, throwUnderlyingException, closure)
    }

    def runBatch(ContainerContainer muleContext,
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
                         ContainerContainer muleContext,
                         String connectorName,
                         @DelegatesTo(HttpRequestResponseChoice) Closure closure) {
        def eventFactory = new EventFactoryImpl(muleContext)
        def formatterChoice = new HttpRequestResponseChoiceImpl(eventFactory)
        def code = closure.rehydrate(formatterChoice, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        mockingConfiguration.addMock(connectorName,
                                     formatterChoice.transformer)
    }

    def mockVmReceive(MockingConfiguration mockingConfiguration,
                      ContainerContainer muleContext,
                      String connectorName,
                      @DelegatesTo(StandardRequestResponse) Closure closure) {
        def eventFactory = new EventFactoryImpl(muleContext)
        def formatterChoice = new VMRequestResponseChoiceImpl(eventFactory)
        def code = closure.rehydrate(formatterChoice, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        def mock = new StandardMock(formatterChoice.transformer)
        mockingConfiguration.addMock(connectorName,
                                     mock)
    }

    def mockGeneric(MockingConfiguration mockingConfiguration,
                    ContainerContainer muleContext,
                    String connectorName,
                    @DelegatesTo(StandardRequestResponse) Closure closure) {
        def eventFactory = new EventFactoryImpl(muleContext)
        def formatterChoice = new GenericRequestResponseChoiceImpl(eventFactory)
        def code = closure.rehydrate(formatterChoice, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        def mock = new StandardMock(formatterChoice.transformer)
        mockingConfiguration.addMock(connectorName,
                                     mock)
    }

    def mockSalesForceCall(MockingConfiguration mockingConfiguration,
                           ContainerContainer muleContext,
                           String connectorName,
                           @DelegatesTo(Choice) Closure closure) {
        def eventFactory = new EventFactoryImpl(muleContext)
        def choice = new ChoiceImpl(muleContext,
                                    eventFactory)
        def code = closure.rehydrate(choice, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        mockingConfiguration.addMock(connectorName,
                                     choice.mock)
    }

    def mockSoapCall(MockingConfiguration mockingConfiguration,
                     ContainerContainer muleContext,
                     String connectorName,
                     @DelegatesTo(SOAPFormatter) Closure closure) {
        def eventFactory = new EventFactoryImpl(muleContext)
        def payloadValidator = new SOAPPayloadValidator()
        def soapFormatter = new SOAPFormatterImpl(eventFactory,
                                                  payloadValidator)
        def code = closure.rehydrate(soapFormatter, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        def mock = new StandardMock(soapFormatter.transformer)
        mockingConfiguration.addMock(connectorName,
                                     mock)
    }
}
