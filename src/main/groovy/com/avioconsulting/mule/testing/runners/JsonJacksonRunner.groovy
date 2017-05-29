package com.avioconsulting.mule.testing.runners

import com.fasterxml.jackson.databind.ObjectMapper
import org.mule.api.MuleContext

class JsonJacksonRunner extends JsonRunner {
    private final inputObject
    private final Class outputClass
    private final ObjectMapper mapper

    JsonJacksonRunner(inputObject,
                      Class outputClass,
                      MuleContext muleContext) {
        super(muleContext)
        this.outputClass = outputClass
        this.inputObject = inputObject
        mapper = new ObjectMapper()
    }

    protected String getJsonString() {
        mapper.writer().writeValueAsString(inputObject)
    }

    protected Object getObjectFromOutput(String outputJson) {
        if (outputClass == null) {
            return
        }
        mapper.readValue(outputJson, outputClass)
    }
}
