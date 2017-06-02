package com.avioconsulting.mule.testing.runners

import com.fasterxml.jackson.databind.ObjectMapper
import org.mule.api.MuleContext

class JsonJacksonRunner extends JsonRunner {
    private final inputObject
    private final Class outputClass
    private final ObjectMapper mapper

    JsonJacksonRunner(inputObject,
                      Class outputClass,
                      MuleContext muleContext,
                      RunnerConfig runnerConfig) {
        super(muleContext, runnerConfig)
        this.outputClass = outputClass
        this.inputObject = inputObject
        mapper = new ObjectMapper()
    }

    protected String getJsonString() {
        mapper.writer().writeValueAsString(inputObject)
    }

    protected boolean isEnforceContentType() {
        // we just return a string in this case, so don't enforce a type
        if (outputClass == null) {
            return false
        }
        super.isEnforceContentType()
    }

    protected Object getObjectFromOutput(String outputJson) {
        if (outputClass == null) {
            // just return the raw string payload in case it's a simple response
            return outputJson
        }
        mapper.readValue(outputJson, outputClass)
    }
}
