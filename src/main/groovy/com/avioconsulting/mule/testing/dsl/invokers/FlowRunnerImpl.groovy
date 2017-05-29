package com.avioconsulting.mule.testing.dsl.invokers

import org.mule.api.MuleContext
import org.mule.api.MuleEvent

class FlowRunnerImpl implements FlowRunner, Invoker {
    private final MuleContext muleContext
    private Invoker invoker

    FlowRunnerImpl(MuleContext muleContext) {
        this.muleContext = muleContext
    }

    def json(@DelegatesTo(JsonInvoker) Closure closure) {
        def jsonInvoker = new JsonInvokerImpl(muleContext)
        invoker = jsonInvoker
        def code = closure.rehydrate(jsonInvoker, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
    }

    MuleEvent getEvent() {
        assert invoker : 'Need to specify a proper format! (e.g. json)'
        invoker.event
    }

    def transformOutput(MuleEvent event) {
        invoker.transformOutput(event)
    }
}
