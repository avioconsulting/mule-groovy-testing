package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.dsl.invokers.FlowRunner
import com.avioconsulting.mule.testing.dsl.invokers.FlowRunnerImpl
import com.avioconsulting.mule.testing.dsl.mocking.*
import com.avioconsulting.mule.testing.payloadvalidators.SOAPPayloadValidator
import com.fasterxml.jackson.databind.ObjectMapper
import com.mulesoft.weave.reader.ByteArraySeekableStream
import groovy.json.JsonSlurper
import org.junit.Before
import org.junit.BeforeClass
import org.mule.api.MuleEvent
import org.mule.api.MuleMessage
import org.mule.module.client.MuleClient
import org.mule.modules.interceptor.processors.MuleMessageTransformer
import org.mule.munit.common.mocking.Attribute
import org.mule.munit.common.mocking.SpyProcess
import org.mule.munit.runner.functional.FunctionalMunitSuite

import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar
import javax.xml.namespace.QName
import java.util.concurrent.CopyOnWriteArrayList

abstract class BaseTest extends FunctionalMunitSuite {
    @BeforeClass
    static void extendMethods() {
        // splitter/aggregate returns a wrapped list
        MuleMessage.metaClass.fromAggregateJsonToString = {
            CopyOnWriteArrayList[] array = delegate.payload
            ByteArraySeekableStream[] streams = array.flatten()
            streams.collect { str -> str.text }
        }
        MuleMessage.metaClass.fromAggregateJsonToMap = {
            def jsonText = fromAggregateJsonToString()
            jsonText.collect { json -> new JsonSlurper().parseText(json) }
        }
        MuleMessage.metaClass.fromAggregateJsonDeserialize = { klass ->
            def jsonText = fromAggregateJsonToString()
            def objectMapper = new ObjectMapper()
            jsonText.collect { json ->
                objectMapper.readValue(json, klass)
            }
        }
        MuleMessage.metaClass.fromAggregateObjects = {
            CopyOnWriteArrayList[] array = delegate.payload
            array.flatten()
        }
    }

    Properties getStartUpProperties() {
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

    @Override
    protected List<String> getFlowsExcludedOfInboundDisabling() {
        []
    }

    abstract List<String> getConfigResourcesList()

    String getConfigResources() {
        def directory = new File('.mule')
        println "Checking for .mule directory at ${directory.absolutePath}"
        if (directory.exists()) {
            println "Removing ${directory.absolutePath}"
            directory.deleteDir()
        }
        configResourcesList.join ","
    }

    @Before
    void handleUnmocked() {
        // don't complain if they explicitly said they don't want to mock these
        if (!haveToMockMuleConnectors()) {
            return
        }
        setupFallThroughMock('consumer', 'ws')
        setupFallThroughMock('request', 'http')
        setupFallThroughMock('outbound-endpoint', 'vm')
    }

    // if you miss a mock, the error message is far from obvious
    // this helps mock things by default and return a useful error message
    private void setupFallThroughMock(String processorName, String namespace) {
        // this is the easiest way to get ahold of the event surrounding a message
        MuleEvent capturedEvent = null
        spyMessageProcessor(processorName)
                .ofNamespace(namespace)
                .before(new SpyProcess() {
            void spy(MuleEvent muleEvent) {
                capturedEvent = muleEvent
            }
        })
        // any other mock will supercede this one
        whenMessageProcessor(processorName)
                .ofNamespace(namespace)
                .thenApply(new MuleMessageTransformer() {
            MuleMessage transform(MuleMessage incoming) {
                // best we can do right now is get the activity before
                def madeIt = false
                try {
                    def processor = capturedEvent.flowConstruct.messageProcessors.last()
                    def fetcher = { item ->
                        def annotations = processor.getAnnotations()
                        annotations[new QName('http://www.mulesoft.org/schema/mule/documentation', item)]
                    }
                    def fileName = fetcher('sourceFileName')
                    def sourceFileLine = fetcher('sourceFileLine')
                    def name = fetcher('name')
                    madeIt = true
                    throw new Exception(
                            "You have an unmocked ${namespace}:${processorName} transport! It's located NEAR the '${name}' processor on line ${sourceFileLine} in ${fileName}")
                }
                // in case our detection method breaks, still show something
                catch (e) {
                    // don't interrupt what we just did
                    if (madeIt) {
                        throw e
                    }
                    throw new Exception(
                            "You have an unmocked ${namespace}:${processorName} transport! Its location in the code could not be located!")
                }
            }
        })
    }

    def runFlow(String flowName,
                @DelegatesTo(FlowRunner) Closure closure) {
        def runner = new FlowRunnerImpl(muleContext)
        def code = closure.rehydrate(runner, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        def outputEvent = runFlow(flowName, runner.event)
        runner.transformOutput(outputEvent)
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

    def mockRestHttpCall(String connectorName,
                         @DelegatesTo(HttpRequestResponseChoice) Closure closure) {
        def mocker = whenMessageProcessor('request')
                .ofNamespace('http')
                .withAttributes(Attribute.attribute('name')
                                        .ofNamespace('doc')
                                        .withValue(connectorName))
        def spy = spyMessageProcessor('request')
                .ofNamespace('http')
                .withAttributes(Attribute.attribute('name')
                                        .ofNamespace('doc')
                                        .withValue(connectorName))
        def locator = new ProcessorLocator(connectorName)
        def formatterChoice = new HttpRequestResponseChoiceImpl(spy,
                                                                locator,
                                                                muleContext)
        def code = closure.rehydrate(formatterChoice, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        mocker.thenApply(formatterChoice.transformer)
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