package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.mulereplacements.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.FlowWrapper
import com.avioconsulting.mule.testing.payloadvalidators.ContentTypeCheckDisabledValidator
import com.avioconsulting.mule.testing.payloadvalidators.HttpListenerPayloadValidator

class FlowRunnerImpl implements FlowRunner, BatchRunner {
    private final RuntimeBridgeTestSide runtimeBridge
    private Invoker invoker
    private Closure closure
    private Closure muleOutputEventHook = null
    private Closure withInputEvent = null
    private final InvokerEventFactory eventFactory
    private final FlowWrapper flow
    private final String flowName

    FlowRunnerImpl(RuntimeBridgeTestSide runtimeBridge,
                   FlowWrapper flowMessageProcessor,
                   String flowName) {
        this.flowName = flowName
        this.flow = flowMessageProcessor
        this.runtimeBridge = runtimeBridge
        this.eventFactory = runtimeBridge
    }

    def json(@DelegatesTo(JsonInvoker) Closure closure) {
        def jsonInvoker = new JsonInvokerImpl(new HttpListenerPayloadValidator(),
                                              eventFactory,
                                              flow)
        invoker = jsonInvoker
        this.closure = closure
    }

    def java(@DelegatesTo(JavaInvoker) Closure closure) {
        def javaInvoker = new JavaInvokerImpl(eventFactory,
                                              flowName)
        invoker = javaInvoker
        this.closure = closure
    }

    @Override
    def soap(@DelegatesTo(SoapInvoker) Closure closure) {
        def soapInvoker = new SoapOperationFlowInvokerImpl(eventFactory,
                                                           flowName)
        invoker = soapInvoker
        this.closure = closure
    }

    def withOutputEvent(Closure closure) {
        muleOutputEventHook = closure
    }

    def withOutputHttpStatus(Closure closure) {
        withOutputEvent { EventWrapper outputEvent ->
            if (outputEvent == null) {
                throw new Exception(
                        'A null event was returned (filter?) so No HTTP status was returned from your flow. With the real flow, an HTTP status of 200 will usually be set by default so this test is usually not required.')
            }
            def statusString = outputEvent.message.getOutboundProperty('http.status') as String
            if (!statusString) {
                throw new Exception('No HTTP status was returned from your flow. Did you forget?')
            }
            def statusInteger = Integer.parseInt(statusString)
            closure(statusInteger)
        }
    }

    def withInputEvent(Closure closure) {
        withInputEvent = closure
    }

    def disableContentTypeCheck() {
        assert invoker: 'Need to specify a proper format first! (e.g. json)'
        invoker = invoker.withNewPayloadValidator(new ContentTypeCheckDisabledValidator(invoker.payloadValidator))
    }

    EventWrapper getEvent() {
        assert invoker: 'Need to specify a proper format! (e.g. json)'
        def code = closure.rehydrate(invoker, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        def event = invoker.getEvent()
        if (withInputEvent) {
            withInputEvent(event)
        }
        event
    }

    def transformOutput(EventWrapper event) {
        def response = null
        // filters return null events
        if (event != null) {
            response = invoker.transformOutput(event)
        }

        if (muleOutputEventHook) {
            muleOutputEventHook(event)
        }

        response
    }
}
