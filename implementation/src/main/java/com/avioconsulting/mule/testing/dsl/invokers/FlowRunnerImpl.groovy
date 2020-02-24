package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.TestingFrameworkException
import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.FlowWrapper
import groovy.util.logging.Log4j2

@Log4j2
class FlowRunnerImpl extends FlowRunnerLiteImpl implements
        FlowRunner,
        BatchRunner {
    private Closure muleOutputEventHook = null
    private Closure withInputEvent = null

    FlowRunnerImpl(RuntimeBridgeTestSide runtimeBridge,
                   FlowWrapper flowMessageProcessor,
                   String flowName) {
        super(flowMessageProcessor,
              runtimeBridge,
              flowName)
    }

    def withOutputEvent(Closure closure) {
        muleOutputEventHook = closure
    }

    def withOutputHttpStatus(Closure closure) {
        withOutputEvent { EventWrapper outputEvent ->
            if (outputEvent == null) {
                throw new TestingFrameworkException(
                        'A null event was returned (filter?) so No HTTP status was returned from your flow. With the real flow, an HTTP status of 200 will usually be set by default so this test is usually not required.')
            }
            def status = outputEvent.getVariable('httpStatus')?.value as Integer
            if (!status) {
                throw new TestingFrameworkException('No HTTP status was returned from your flow in the httpStatus variable. Did you forget?')
            }
            closure(status)
        }
    }

    def withInputEvent(Closure closure) {
        withInputEvent = closure
    }

    @Override
    EventWrapper getEvent() {
        def event = super.getEvent()
        return withInputEvent ? withInputEvent(event) : event
    }

    @Override
    def transformOutput(EventWrapper event) {
        def response = super.transformOutput(event)
        if (muleOutputEventHook) {
            muleOutputEventHook(event)
        }
        return response
    }
}
