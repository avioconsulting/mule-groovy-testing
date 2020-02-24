package com.avioconsulting.mule.testing.dsl.invokers


import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.FlowWrapper
import groovy.util.logging.Log4j2

@Log4j2
class FlowRunnerImpl extends FlowRunnerLiteImpl implements
        FlowRunner,
        BatchRunner {
    private Closure withInputEvent = null

    FlowRunnerImpl(RuntimeBridgeTestSide runtimeBridge,
                   FlowWrapper flowMessageProcessor,
                   String flowName) {
        super(flowMessageProcessor,
              runtimeBridge,
              flowName)
    }

    def withInputEvent(Closure closure) {
        withInputEvent = closure
    }

    @Override
    EventWrapper getEvent() {
        def event = super.getEvent()
        return withInputEvent ? withInputEvent(event) : event
    }
}
