package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.EventFactoryImpl
import com.avioconsulting.mule.testing.payloadvalidators.ContentTypeCheckDisabledValidator
import com.avioconsulting.mule.testing.payloadvalidators.HttpListenerPayloadValidator
import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.construct.Flow

class FlowRunnerImpl implements FlowRunner, BatchRunner {
    private final MuleContext muleContext
    private Invoker invoker
    private Closure closure
    private Closure muleOutputEventHook = null
    private Closure withInputEvent = null
    private final EventFactory eventFactory
    private final Flow flow

    FlowRunnerImpl(MuleContext muleContext,
                   String flowName) {
        this.flow = muleContext.registry.lookupFlowConstruct(flowName) as Flow
        this.muleContext = muleContext
        this.eventFactory = new EventFactoryImpl(muleContext)
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
                                              flow.name)
        invoker = javaInvoker
        this.closure = closure
    }

    @Override
    def soap(@DelegatesTo(SoapInvoker) Closure closure) {
        def soapInvoker = new SoapOperationFlowInvokerImpl(muleContext,
                                                           eventFactory,
                                                           flowName)
        invoker = soapInvoker
        this.closure = closure
    }

    def withOutputEvent(Closure closure) {
        muleOutputEventHook = closure
    }

    def withOutputHttpStatus(Closure closure) {
        withOutputEvent { MuleEvent outputEvent ->
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

    MuleEvent getEvent() {
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

    def transformOutput(MuleEvent event) {
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
