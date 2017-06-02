package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.runners.RunnerConfig
import org.mule.api.MuleContext
import org.mule.api.MuleEvent

class FlowRunnerImpl implements FlowRunner, Invoker {
    private final MuleContext muleContext
    private Invoker invoker
    private Closure muleOutputEventHook = null
    private Closure withInputEvent = null
    private RunnerConfig runnerConfig = new RunnerConfig()

    FlowRunnerImpl(MuleContext muleContext) {
        this.muleContext = muleContext
    }

    def json(@DelegatesTo(JsonInvoker) Closure closure) {
        def jsonInvoker = new JsonInvokerImpl(muleContext,
                                              runnerConfig)
        invoker = jsonInvoker
        def code = closure.rehydrate(jsonInvoker, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
    }

    def withOutputEvent(Closure closure) {
        muleOutputEventHook = closure
    }

    def withOutputHttpStatus(Closure closure) {
        withOutputEvent { MuleEvent outputEvent ->
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

    def apiKitReferencesThisFlow() {
        runnerConfig.apiKitReferencesThisFlow = true
    }

    MuleEvent getEvent() {
        assert invoker: 'Need to specify a proper format! (e.g. json)'
        def event = invoker.event
        if (withInputEvent) {
            withInputEvent(event)
        }
        event
    }

    def transformOutput(MuleEvent event) {
        def response = invoker.transformOutput(event)
        if (muleOutputEventHook) {
            muleOutputEventHook(event)
        }
        response
    }
}
