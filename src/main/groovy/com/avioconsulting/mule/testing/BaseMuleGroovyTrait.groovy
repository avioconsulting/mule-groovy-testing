package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.batch.BatchWaitUtil
import com.avioconsulting.mule.testing.dsl.invokers.*
import com.avioconsulting.mule.testing.dsl.mocking.*
import com.avioconsulting.mule.testing.dsl.mocking.sfdc.Choice
import com.avioconsulting.mule.testing.dsl.mocking.sfdc.ChoiceImpl
import com.avioconsulting.mule.testing.mocks.StandardMock
import com.avioconsulting.mule.testing.mulereplacements.MockingConfiguration
import com.avioconsulting.mule.testing.payloadvalidators.SOAPPayloadValidator
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.NotImplementedException
import org.apache.logging.log4j.Logger
import org.mule.runtime.core.api.MuleContext
import org.mule.runtime.core.api.construct.Flow
import org.mule.runtime.core.api.event.CoreEvent
import org.mule.runtime.module.launcher.MuleContainer

// basic idea here is to have a trait that could be mixed in to any type of testing framework situation
// this trait should be stateless
trait BaseMuleGroovyTrait {
    abstract Logger getLogger()

    MuleContext createMuleContext(MockingConfiguration mockingConfiguration) {
        def directory = new File('.mule')
        System.setProperty('mule.home',
                           directory.absolutePath)
        logger.info "Checking for .mule directory at ${directory.absolutePath}"
//        if (directory.exists()) {
//            logger.info "Removing ${directory.absolutePath}"
//            directory.deleteDir()
//        }
        // TODO: copy log4j in and re-enable removal
        def domainsDir = new File(directory, 'domains')
        domainsDir.mkdirs()
        def appsDir = new File(directory, 'apps')
        if (appsDir.exists()) {
            appsDir.deleteDir()
        }
        appsDir.mkdirs()
        // TODO: Have to do this from maven deps (without api gateway since it seems to have a missing class and we do not need it anyways)
//        FileUtils.copyDirectory(new File('/Applications/AnypointStudio_7.app/Contents/Eclipse/plugins/org.mule.tooling.server.4.1.2.ee_7.1.3.201805211611/mule/services'),
//                                new File(directory,
//                                         'services'))
        def container = new MuleContainer()
        container.start(false)
        container.deploymentService.deployDomain(new File('src/test/resources/default').toURI())
        container.deploymentService.deploy(new File('src/test/resources/41test').toURI())
//        def contextFactory = new DefaultMuleContextFactory()
//        def muleContextBuilder = new DefaultMuleContextBuilder()
//        def configBuilders = [
//                new SimpleConfigurationBuilder(startUpProperties),
//                // certain processors like validation require this
//                new SpringXmlConfigurationBuilder(configResources)
//        ] as List<ConfigurationBuilder>
//        contextFactory.createMuleContext(configBuilders,
//                                         muleContextBuilder)
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

    def runFlow(MuleContext muleContext,
                String flowName,
                @DelegatesTo(FlowRunner) Closure closure) {
        def flow = muleContext.registry.lookupFlowConstruct(flowName) as Flow
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

    CoreEvent runSoapApikitFlow(MuleContext muleContext,
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

    CoreEvent runFlow(MuleContext muleContext,
                      String flowName,
                      CoreEvent event) {
        def flow = muleContext.registry.lookupFlowConstruct(flowName)
        assert flow instanceof Flow
        flow.process(event)
    }

    def waitForBatchCompletion(MuleContext muleContext,
                               List<String> jobsToWaitFor = null,
                               boolean throwUnderlyingException = false,
                               Closure closure) {
        def batchWaitUtil = new BatchWaitUtil(muleContext)
        batchWaitUtil.waitFor(jobsToWaitFor, throwUnderlyingException, closure)
    }

    def runBatch(MuleContext muleContext,
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
                         MuleContext muleContext,
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
                      MuleContext muleContext,
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
                    MuleContext muleContext,
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
                           MuleContext muleContext,
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
                     MuleContext muleContext,
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
