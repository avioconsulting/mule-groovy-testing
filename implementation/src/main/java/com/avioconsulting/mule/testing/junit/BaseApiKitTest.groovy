package com.avioconsulting.mule.testing.junit


import com.avioconsulting.mule.testing.dsl.invokers.FlowRunnerLiteImpl
import com.avioconsulting.mule.testing.dsl.invokers.FlowRunnerWithoutEventControl
import com.avioconsulting.mule.testing.muleinterfaces.HttpAttributeBuilder
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper

abstract class BaseApiKitTest extends
        BaseJunitTest implements
        HttpAttributeBuilder {
    abstract String getApiNameUnderTest()

    abstract String getApiVersionUnderTest()

    String getFlowName() {
        "${apiNameUnderTest.toLowerCase()}-main"
    }

    // using CloudHub combine friendly convention
    String getHttpListenerPath() {
        '/' + [apiNameUnderTest, 'api', apiVersionUnderTest, '*'].join('/')
    }

    @Override
    Map getStartUpProperties() {
        super.getStartUpProperties() + [
                'skip.apikit.validation'    : 'false',
                'return.validation.failures': 'true'
        ]
    }

    def runApiKitFlow(String httpMethod,
                      String path,
                      Map queryParams = null,
                      Map headers = [:],
                      // since we control the event creation heavily here with our http request attributes
                      // make it explicit that you can't customize (FlowRunner interface implies you can,
                      // FlowRunnerWithoutEventControl implies you cannot)
                      @DelegatesTo(FlowRunnerWithoutEventControl) Closure closure) {
        def flow = runtimeBridge.getFlow(flowName)
        def runner = new FlowRunnerLiteImpl(flow,
                                            runtimeBridge,
                                            flowName)
        try {
            def code = closure.rehydrate(runner,
                                         this,
                                         this)
            code.resolveStrategy = Closure.DELEGATE_ONLY
            code()
            def inputEvent = setHttpProps(runner.event,
                                          httpMethod,
                                          path,
                                          queryParams,
                                          headers)
            def outputEvent = runFlow(runtimeBridge,
                                      flowName,
                                      inputEvent)
            runner.transformOutput(outputEvent)
        }
        finally {
            runner.closeLogContext()
        }
    }

    private EventWrapper setHttpProps(EventWrapper event,
                                      String method,
                                      String path,
                                      Map queryParams,
                                      Map headers) {
        // unless the sources/listeners are enabled (not required for apikit in Mule 4, unlike Mule 3)
        // then the listener config never tries to actually bind to the port. therefore the port
        // does not matter
        def portNumberDoesNotMatter = 9999
        def attributes = getHttpRequestAttributes(httpListenerPath,
                                                  method,
                                                  path,
                                                  queryParams,
                                                  runtimeBridge,
                                                  event.message.mimeType,
                                                  "localhost:${portNumberDoesNotMatter}",
                                                  headers)
        logger.info 'APIkit flow invocation: simulating HTTP listener using attributes: {}',
                    attributes
        event.withNewAttributes(attributes)
    }
}
