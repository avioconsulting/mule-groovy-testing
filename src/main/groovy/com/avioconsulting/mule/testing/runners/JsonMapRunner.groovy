package com.avioconsulting.mule.testing.runners

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.mule.api.MuleContext

class JsonMapRunner extends JsonRunner {
    private final Map inputMap

    JsonMapRunner(Map inputMap,
                  MuleContext muleContext,
                  RunnerConfig runnerConfig) {
        super(muleContext, runnerConfig)
        this.inputMap = inputMap
    }

    protected String getJsonString() {
        JsonOutput.toJson(inputMap)
    }

    protected Object getObjectFromOutput(String outputJson) {
        new JsonSlurper().parseText(outputJson)
    }
}
