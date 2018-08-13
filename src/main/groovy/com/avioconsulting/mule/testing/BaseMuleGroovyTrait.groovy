package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.dsl.invokers.BatchRunner
import com.avioconsulting.mule.testing.dsl.invokers.FlowRunner
import com.avioconsulting.mule.testing.dsl.invokers.FlowRunnerImpl
import com.avioconsulting.mule.testing.dsl.mocking.*
import com.avioconsulting.mule.testing.dsl.mocking.sfdc.Choice
import com.avioconsulting.mule.testing.dsl.mocking.sfdc.ChoiceImpl
import com.avioconsulting.mule.testing.mocks.HttpMock
import com.avioconsulting.mule.testing.mulereplacements.GroovyTestingSpringXmlConfigurationBuilder
import com.avioconsulting.mule.testing.mulereplacements.MockingConfiguration
import com.avioconsulting.mule.testing.payloadvalidators.SOAPPayloadValidator
import com.mulesoft.module.batch.engine.BatchJobAdapter
import org.apache.logging.log4j.Logger
import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.api.MuleMessage
import org.mule.api.config.ConfigurationBuilder
import org.mule.config.builders.ExtensionsManagerConfigurationBuilder
import org.mule.construct.Flow
import org.mule.context.DefaultMuleContextBuilder
import org.mule.context.DefaultMuleContextFactory
import org.mule.module.client.MuleClient

import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar

// basic idea here is to have a trait that could be mixed in to any type of testing framework situation
// this trait should be stateless
trait BaseMuleGroovyTrait {
    abstract Logger getLogger()

    MuleContext createMuleContext(MockingConfiguration mockingConfiguration) {
        // TODO: Optimize this and only deal w/ new contexts when props/mocks/etc. change
        def contextFactory = new DefaultMuleContextFactory()
        def muleContextBuilder = new DefaultMuleContextBuilder()
        def configBuilders = [
                // certain processors like validation require this
                new ExtensionsManagerConfigurationBuilder(),
                new GroovyTestingSpringXmlConfigurationBuilder(configResources,
                                                               mockingConfiguration)
        ] as List<ConfigurationBuilder>
        contextFactory.createMuleContext(configBuilders,
                                         muleContextBuilder)
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

    // TODO: disable the listeners properly, we don't currently do this. Might want a test w/ the old way that ensures the listener is turned off
    List<String> getUnmockedFlowsWithListeners() {
        []
    }

    Map<String, String> getConfigResourceSubstitutes() {
        [:]
    }

    String getMuleDeployPropertiesResources() {
        def muleDeployProperties = new Properties()
        def propsFile = new File('mule-deploy.properties')
        assert propsFile.exists(): 'Expected mule-deploy.properties to exist!'
        def inputStream = propsFile.newInputStream()
        muleDeployProperties.load(inputStream)
        inputStream.close()
        muleDeployProperties.getProperty('config.resources')
    }

    String getConfigResources() {
        def directory = new File('.mule')
        logger.info "Checking for .mule directory at ${directory.absolutePath}"
        if (directory.exists()) {
            logger.info "Removing ${directory.absolutePath}"
            directory.deleteDir()
        }
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
        def runner = new FlowRunnerImpl(muleContext,
                                        flowName)
        def code = closure.rehydrate(runner, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        def outputEvent = runFlow(muleContext,
                                  flowName,
                                  runner.getEvent())
        runner.transformOutput(outputEvent)
    }

    MuleEvent runFlow(MuleContext muleContext,
                      String flowName,
                      MuleEvent event) {
        def flow = muleContext.registry.lookupFlowConstruct(flowName)
        assert flow instanceof Flow
        flow.process(event)
    }

    static def waitForBatchCompletion(List<String> jobsToWaitFor = null,
                                      boolean throwUnderlyingException = false,
                                      Closure closure) {
        //def batchWaitUtil = new BatchWaitUtil(muleContext)
        //batchWaitUtil.waitFor(jobsToWaitFor, throwUnderlyingException, closure)
    }

    def runBatch(MuleContext muleContext,
                 String batchName,
                 List<String> jobsToWaitFor = null,
                 boolean throwUnderlyingException = false,
                 @DelegatesTo(BatchRunner) Closure closure) {
        def runner = new FlowRunnerImpl(muleContext,
                                        batchName)
        def code = closure.rehydrate(runner, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        def batchJob = muleContext.registry.get(batchName) as BatchJobAdapter
        waitForBatchCompletion(jobsToWaitFor,
                               throwUnderlyingException) {
            batchJob.execute(runner.getEvent())
        }
    }

    static MuleMessage httpPost(map) {
        def timeoutSeconds = map.timeoutSeconds ?: 35
        def client = new MuleClient(muleContext)
        def properties = [
                'http.method' : 'POST',
                'content-type': map.contentType
        ]
        client.send map.url,
                    map.payload,
                    properties,
                    timeoutSeconds * 1000
    }

    static MuleMessage httpGet(map) {
        def timeoutSeconds = map.timeoutSeconds ?: 35
        def client = new MuleClient(muleContext)
        def properties = ['http.method': 'GET']
        def payload = null
        client.send map.url,
                    payload,
                    properties,
                    timeoutSeconds * 1000
    }

    def mockRestHttpCall(MockingConfiguration mockingConfiguration,
                         String connectorName,
                         @DelegatesTo(HttpRequestResponseChoice) Closure closure) {
        mockingConfiguration.addMock(connectorName,
                                     new HttpMock())
        // TODO: Hook the rest of this in
//        def locator = new ProcessorLocator(connectorName)
//        def formatterChoice = new HttpRequestResponseChoiceImpl(spy,
//                                                                locator,
//                                                                muleContext)
//        def code = closure.rehydrate(formatterChoice, this, this)
//        code.resolveStrategy = Closure.DELEGATE_ONLY
//        code()
//        mocker.thenApply(formatterChoice.transformer)
    }

    def mockVmReceive(String connectorName,
                      @DelegatesTo(StandardRequestResponse) Closure closure) {
        def mocker = whenMessageProcessor('outbound-endpoint')
                .ofNamespace('vm')
                .withAttributes(Attribute.attribute('name')
                                        .ofNamespace('doc')
                                        .withValue(connectorName))
        def formatterChoice = new VMRequestResponseChoiceImpl(muleContext)
        def code = closure.rehydrate(formatterChoice, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        mocker.thenApply(formatterChoice.transformer)
    }

    def mockSalesForceCall(String connectorName,
                           @DelegatesTo(Choice) Closure closure) {
        def locator = new ProcessorLocator(connectorName)
        def choice = new ChoiceImpl(muleContext, { String processorType ->
            spyMessageProcessor(processorType)
                    .ofNamespace('sfdc')
                    .withAttributes(Attribute.attribute('name')
                                            .ofNamespace('doc')
                                            .withValue(connectorName))
        }, locator)
        def code = closure.rehydrate(choice, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        def mocker = whenMessageProcessor(choice.connectorType)
                .ofNamespace('sfdc')
                .withAttributes(Attribute.attribute('name')
                                        .ofNamespace('doc')
                                        .withValue(connectorName))
        mocker.thenApply(choice.transformer)
    }

    def mockSoapCall(String connectorName,
                     @DelegatesTo(SOAPFormatter) Closure closure) {
        def mocker = whenMessageProcessor('consumer')
                .ofNamespace('ws')
                .withAttributes(Attribute.attribute('name')
                                        .ofNamespace('doc')
                                        .withValue(connectorName))
        def spy = spyMessageProcessor('consumer')
                .ofNamespace('ws')
                .withAttributes(Attribute.attribute('name')
                                        .ofNamespace('doc')
                                        .withValue(connectorName))
        def payloadValidator = new SOAPPayloadValidator()
        def soapFormatter = new SOAPFormatterImpl(muleContext,
                                                  spy,
                                                  payloadValidator)
        def code = closure.rehydrate(soapFormatter, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        mocker.thenApply(soapFormatter.transformer)
    }

    static XMLGregorianCalendar getXmlDate(int year, int oneBasedMonth, int dayOfMonth) {
        def zeroBasedMonth = oneBasedMonth - 1
        def gregorian = new GregorianCalendar(year, zeroBasedMonth, dayOfMonth)
        DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorian)
    }

    static XMLGregorianCalendar getXmlDateTime(int year, int oneBasedMonth, int dayOfMonth, int hourOfDay, int minute,
                                               int second = 0, String timeZoneId) {
        def zeroBasedMonth = oneBasedMonth - 1
        def gregorian = new GregorianCalendar(year, zeroBasedMonth, dayOfMonth, hourOfDay, minute, second)
        gregorian.setTimeZone(TimeZone.getTimeZone(timeZoneId))
        DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorian)
    }
}
