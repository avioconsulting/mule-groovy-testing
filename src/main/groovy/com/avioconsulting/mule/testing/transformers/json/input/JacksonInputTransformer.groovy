package com.avioconsulting.mule.testing.transformers.json.input

import com.avioconsulting.mule.testing.dsl.ConnectorType
import com.fasterxml.jackson.databind.ObjectMapper
import org.mule.api.MuleContext

class JacksonInputTransformer extends Common {
    def mapper = new ObjectMapper()
    private final Class inputClass

    JacksonInputTransformer(MuleContext muleContext,
                            ConnectorType connectorType,
                            List<Class> allowedPayloadTypes,
                            Class inputClass) {
        super(muleContext, connectorType, allowedPayloadTypes)
        this.inputClass = inputClass
    }

    def transform(String jsonString) {
        mapper.readValue(jsonString, inputClass)
    }
}
