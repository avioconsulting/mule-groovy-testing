package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.payload_types.ContentTypeCheckDisabledValidator
import com.avioconsulting.mule.testing.payload_types.HttpListenerPayloadValidator
import org.mule.api.MuleContext
import org.mule.api.MuleEvent

class FlowRunnerImpl implements FlowRunner, Invoker {
    private final MuleContext muleContext
    private Invoker invoker
    private Closure closure
    private Closure muleOutputEventHook = null
    private Closure withInputEvent = null

    FlowRunnerImpl(MuleContext muleContext) {
        this.muleContext = muleContext
    }

    def json(@DelegatesTo(JsonInvoker) Closure closure) {
        def jsonInvoker = new JsonInvokerImpl(muleContext,
                                              new HttpListenerPayloadValidator())
        invoker = jsonInvoker
        this.closure = closure
    }

    def java(@DelegatesTo(JavaInvoker) Closure closure) {
        def javaInvoker = new JavaInvokerImpl(muleContext)
        invoker = javaInvoker
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
        if (invoker instanceof JsonInvokerImpl) {
            invoker = new JsonInvokerImpl(muleContext,
                                          new ContentTypeCheckDisabledValidator(invoker.payloadValidator))
        }
    }

    MuleEvent getEvent() {
        assert invoker: 'Need to specify a proper format! (e.g. json)'
        def code = closure.rehydrate(invoker, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        def event = invoker.event
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
