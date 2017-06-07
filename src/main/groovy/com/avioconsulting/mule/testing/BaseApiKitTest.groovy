package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.dsl.invokers.FlowRunner
import com.avioconsulting.mule.testing.dsl.invokers.FlowRunnerImpl
import org.mule.api.MuleMessage
import org.mule.api.transport.PropertyScope

abstract class BaseApiKitTest extends BaseTest {
    protected abstract String getApiNameUnderTest()

    protected abstract String getApiVersionUnderTest()

    // version friendly convention
    protected String getFullApiName() {
        "api-${apiNameUnderTest}-${apiVersionUnderTest}"
    }

    protected String getFlowName() {
        "${fullApiName}-main"
    }

    // using CloudHub combine friendly convention
    protected String getHttpListenerPath() {
        '/' + [apiNameUnderTest, 'api', apiVersionUnderTest, '*'].join('/')
    }

    @Override
    Properties getStartUpProperties() {
        def properties = super.getStartUpProperties()
        // have to have the listener running to use apikit
        // http listener gets going before the properties object this method creates has had its values take effect
        System.setProperty('avio.test.http.port',
                           getHttpPort() as String)
        properties.put('http.listener.config', 'test-http-listener-config')
        // by convention, assume this
        properties.put('skip.apikit.validation', 'false')
        properties.put('return.validation.failures', 'true')
        properties
    }

    @Override
    protected List<String> getFlowsExcludedOfInboundDisabling() {
        // apikit complains unless these 2 are both open
        ['main', 'console'].collect { suffix ->
            // toString here to ensure we return Java string and not Groovy strings
            "${fullApiName}-${suffix}".toString()
        }
    }

    @Override
    List<String> getConfigResourcesList() {
        [
                'global-test.xml',
                "${fullApiName}.xml".toString()
        ]
    }

    def runApiKitFlow(String httpMethod,
                      String path,
                      Map queryParams = null,
                      @DelegatesTo(FlowRunner) Closure closure) {
        def runner = new FlowRunnerImpl(muleContext)
        def code = closure.rehydrate(runner, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        def inputEvent = runner.event
        setHttpProps(inputEvent.message,
                     httpMethod,
                     path,
                     queryParams)
        def outputEvent = runFlow(flowName, inputEvent)
        runner.transformOutput(outputEvent)
    }

    private def setHttpProps(MuleMessage message,
                             String method,
                             String path,
                             Map queryParams) {
        def urlParts = httpListenerPath.split('/')
        assert urlParts.last() == '*': 'Expected wildcard listener!'
        urlParts = urlParts[0..-2]
        urlParts.addAll(path.split('/'))
        urlParts.removeAll { part -> part == '' }
        def url = '/' + urlParts.join('/')
        println "Setting http request path to ${url}..."
        def props = [
                'listener.path': httpListenerPath,
                method         : method,
                'relative.path': url,
                'request.path' : url,
                'request.uri'  : url,
        ]
        if (queryParams) {
            props['query.params'] = queryParams.collectEntries { key, value ->
                // everything needs to be a string to mimic real HTTP listener
                [key.toString(), value.toString()]
            }
        }
        def httpProps = props.collectEntries { prop, value ->
            ["http.${prop}".toString(), value]
        }
        httpProps['host'] = "localhost:${getHttpPort()}".toString()
        httpProps.each { prop, value ->
            message.setProperty(prop, value, PropertyScope.INBOUND)
        }
    }

    private Integer httpPort = null

    protected int getHttpPort() {
        if (httpPort) {
            return httpPort
        }
        httpPort = (8088..8199).find { candidate ->
            try {
                def socket = new ServerSocket(candidate)
                socket.close()
                true
            }
            catch (IOException ignored) {
                false
            }
        }
    }

}
