package com.avioconsulting.mule.testing.junit

import com.avioconsulting.mule.testing.OpenPortLocator
import com.avioconsulting.mule.testing.dsl.invokers.FlowRunner
import com.avioconsulting.mule.testing.dsl.invokers.FlowRunnerImpl
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

abstract class BaseApiKitTest extends
        BaseJunitTest {
    private static final String TEST_PORT_PROPERTY = 'avio.test.http.port'

    abstract String getApiNameUnderTest()

    abstract String getApiVersionUnderTest()

    // version friendly convention
    String getFullApiName() {
        "api-${apiNameUnderTest}-${apiVersionUnderTest}"
    }

    String getFlowName() {
        "${fullApiName}-main"
    }

    // using CloudHub combine friendly convention
    String getHttpListenerPath() {
        '/' + [apiNameUnderTest, 'api', apiVersionUnderTest, '*'].join('/')
    }

    static int getChosenHttpPort() {
        // avoid duplicate ports
        Integer.parseInt(System.getProperty(TEST_PORT_PROPERTY))
    }

    @Override
    Map getStartUpProperties() {
        def properties = super.getStartUpProperties()
        // have to have the listener running to use apikit
        // http listener gets go
        // ing before the properties object this method creates has had its values take effect
        def port = OpenPortLocator.httpPort
        logger.info 'Using open port {} for HTTP listener',
                    port
        System.setProperty(TEST_PORT_PROPERTY,
                           port as String)
        properties.put('http.listener.config', 'test-http-listener-config')
        // by convention, assume this
        properties.put('skip.apikit.validation', 'false')
        properties.put('return.validation.failures', 'true')
        properties
    }

    @Override
    Map<String, String> getConfigResourceSubstitutes() {
        ['global.xml': 'global-test.xml']
    }

    def runApiKitFlow(String httpMethod,
                      String path,
                      Map queryParams = null,
                      @DelegatesTo(FlowRunner) Closure closure) {
        def flow = runtimeBridge.getFlow(flowName)
        def runner = new FlowRunnerImpl(runtimeBridge,
                                        flow,
                                        flowName)
        def code = closure.rehydrate(runner, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        def inputEvent = setHttpProps(runner.event,
                                      httpMethod,
                                      path,
                                      queryParams)
        def outputEvent = runFlow(runtimeBridge,
                                  flowName,
                                  inputEvent)
        runner.transformOutput(outputEvent)
    }

    private EventWrapper setHttpProps(EventWrapper event,
                                      String method,
                                      String path,
                                      Map queryParams) {
        def urlParts = httpListenerPath.split('/')
        assert urlParts.last() == '*': 'Expected wildcard listener!'
        urlParts = urlParts[0..-2]
        urlParts.addAll(path.split('/'))
        urlParts.removeAll { part -> part == '' }
        def url = '/' + urlParts.join('/')
        def appClassLoader = runtimeBridge.appClassloader
        def multiMapClass = appClassLoader.loadClass('org.mule.runtime.api.util.MultiMap')
        def getMultiMap = { Map incoming ->
            multiMapClass.newInstance(incoming)
        }
        def attrClass = appClassLoader.loadClass('org.mule.extension.http.api.HttpRequestAttributes')
        queryParams = (queryParams ?: [:]).collectEntries { key, value ->
            // everything needs to be a string to mimic real HTTP listener
            [key.toString(), value.toString()]
        }
        def headers = [
                host          : "localhost:${System.getProperty(TEST_PORT_PROPERTY)}".toString(),
                // Even though content type is set on the message/payload mediatype,
                // apikit router in mule 4 depends on this
                'content-type': event.message.mimeType
        ]
        // public HttpRequestAttributes(MultiMap<String, String> headers, String listenerPath, String relativePath, String version, String scheme, String method, String requestPath, String requestUri, String queryString, MultiMap<String, String> queryParams, Map<String, String> uriParams, String remoteAddress, Certificate clientCertificate) {
        //        this(headers, listenerPath, relativePath, version, scheme, method, requestPath, requestUri, queryString, queryParams, uriParams, "", remoteAddress, clientCertificate);
        //    }
        def attributes = attrClass.newInstance(getMultiMap(headers),
                                               httpListenerPath,
                                               url,
                                               'HTTP/1.1',
                                               'http',
                                               method,
                                               url,
                                               url,
                                               '', // query string
                                               getMultiMap(queryParams),
                                               [:], // uri params
                                               '/remoteaddress',
                                               null)
        logger.info 'APIkit flow invocation: simulating HTTP listener using attributes: {}',
                    attributes
        event.withNewAttributes(attributes)
    }
}
