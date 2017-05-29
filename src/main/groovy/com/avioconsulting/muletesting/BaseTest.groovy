package com.avioconsulting.muletesting

import com.fasterxml.jackson.databind.ObjectMapper
import com.mulesoft.weave.reader.ByteArraySeekableStream
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.junit.Before
import org.junit.BeforeClass
import org.mule.api.MuleEvent
import org.mule.api.MuleMessage
import org.mule.module.client.MuleClient
import org.mule.module.json.JsonData
import org.mule.modules.interceptor.processors.MuleMessageTransformer
import org.mule.munit.common.mocking.SpyProcess
import org.mule.munit.runner.functional.FunctionalMunitSuite

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
        properties.putAll propertyMap
        // verbose in testing is good
        properties.put('mule.verbose.exceptions', true)
        properties
    }

    def getPropertyMap() {
        [:]
    }

    List<String> getFlowsExcludedOfInboundDisabling() {
        // apikit complains unless these 2 are both open
        ['api-main', 'api-console']
    }

    abstract List<String> getConfigResourcesList()

    String getConfigResources() {
        configResourcesList.join ","
    }

    MuleEvent runMuleFlowWithJson(String flow, object) {
        def json = JsonOutput.toJson(object)
        // Mule wraps JSON with the JsonData class
        runFlow(flow, testEvent(new JsonData(json)))
    }

    @Before
    void handleUnmocked() {
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
                    throw new Exception("You have an unmocked ${namespace}:${processorName} transport! It's located NEAR the '${name}' processor on line ${sourceFileLine} in ${fileName}")
                }
                // in case our detection method breaks, still show something
                catch (e) {
                    // don't interrupt what we just did
                    if (madeIt) {
                        throw e
                    }
                    throw new Exception("You have an unmocked ${namespace}:${processorName} transport! Its location in the code could not be located!")
                }
            }
        })
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
}
