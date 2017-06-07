package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.runners.JsonJacksonRunner
import com.avioconsulting.mule.testing.runners.JsonMapRunner
import com.avioconsulting.mule.testing.runners.JsonRunner
import com.avioconsulting.mule.testing.runners.RunnerConfig
import org.mule.api.MuleContext
import org.mule.api.MuleEvent

class JsonInvokerImpl implements JsonInvoker, Invoker {
    private final MuleContext muleContext
    private JsonRunner jsonRunner
    private final RunnerConfig runnerConfig

    JsonInvokerImpl(MuleContext muleContext,
                    RunnerConfig runnerConfig) {
        this.runnerConfig = runnerConfig
        this.muleContext = muleContext
    }

    def jackson(Object inputObject) {
        jsonRunner = new JsonJacksonRunner(inputObject,
                                           null,
                                           muleContext,
                                           runnerConfig)
    }

    def jackson(inputObject, Class outputClass) {
        jsonRunner = new JsonJacksonRunner(inputObject,
                                           outputClass,
                                           muleContext,
                                           runnerConfig)
    }

    def jackson(Class outputClass) {
        jsonRunner = new JsonJacksonRunner(null,
                                           outputClass,
                                           muleContext,
                                           runnerConfig)
    }

    def map(Map input) {
        jsonRunner = new JsonMapRunner(input,
                                       muleContext,
                                       runnerConfig)
    }

    def noStreaming() {
        assert jsonRunner: 'Need to specify a type of JSON serialization (jackson, map) first!'
        jsonRunner.disableStreaming()
    }

    MuleEvent getEvent() {
        assert jsonRunner: 'Need to specify a type of JSON serialization (jackson, map)'
        jsonRunner.event
    }

    def transformOutput(MuleEvent event) {
        jsonRunner.transformOutput(event)
    }
}
