package com.avioconsulting.mule.testing.junit

import com.avioconsulting.mule.testing.OpenPortLocator
import com.avioconsulting.mule.testing.dsl.invokers.FlowRunner
import com.avioconsulting.mule.testing.dsl.invokers.FlowRunnerImpl
import com.avioconsulting.mule.testing.muleinterfaces.HttpAttributeBuilder
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper

abstract class BaseApiKitTest extends
        BaseJunitTest implements
        HttpAttributeBuilder {
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
        def port = Integer.parseInt(System.getProperty(TEST_PORT_PROPERTY))
        def attributes = getHttpListenerAttributes(httpListenerPath,
                                                   method,
                                                   path,
                                                   queryParams,
                                                   runtimeBridge,
                                                   event.message.mimeType,
                                                   port)
        logger.info 'APIkit flow invocation: simulating HTTP listener using attributes: {}',
                    attributes
        event.withNewAttributes(attributes)
    }
}
