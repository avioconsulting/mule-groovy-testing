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
    Properties getStartUpProperties() {
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

    // TODO: Get these in TestingConfiguration and also reconfigure configResources/properties to use TestingConfiguration
    @Override
    List<String> keepListenersOnForTheseFlows() {
        // apikit complains unless these 2 are both open
        ['main', 'console'].collect { suffix ->
            // toString here to ensure we return Java string and not Groovy strings
            "${fullApiName}-${suffix}".toString()
        }
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
        def inputEvent = runner.event
        inputEvent = setHttpProps(inputEvent,
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
        logger.info "Setting http request path to ${url}..."
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
        httpProps['host'] = "localhost:${System.getProperty(TEST_PORT_PROPERTY)}".toString()
        // TODO: Need to set these as attributes on the event (and confirm they are still valid)
        httpProps.each { prop, value ->
            message.setProperty(prop, value, PropertyScope.INBOUND)
        }
        // TODO: Return a fresh event
    }
}
