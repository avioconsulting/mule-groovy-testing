package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.runners.JsonJacksonRunner
import com.avioconsulting.mule.testing.runners.JsonMapRunner
import com.avioconsulting.mule.testing.runners.JsonRunner
import org.mule.api.MuleContext
import org.mule.api.MuleEvent

class JsonInvokerImpl implements JsonInvoker, Invoker {
    private final MuleContext muleContext
    private JsonRunner jsonRunner

    JsonInvokerImpl(MuleContext muleContext) {
        this.muleContext = muleContext
    }

    def jackson(inputObject, Class outputClass) {
        jsonRunner = new JsonJacksonRunner(inputObject, outputClass, muleContext)
    }

    def inputMap(Map input) {
        jsonRunner = new JsonMapRunner(input, muleContext)
    }

    def noStreaming() {

    }

    MuleEvent getEvent() {
        assert jsonRunner
        jsonRunner.event
    }

    def transformOutput(MuleEvent event) {
        jsonRunner.transformOutput(event)
    }
}
