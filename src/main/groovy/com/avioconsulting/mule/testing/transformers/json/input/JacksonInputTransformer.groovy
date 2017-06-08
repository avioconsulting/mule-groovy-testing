package com.avioconsulting.mule.testing.transformers.json.input

import com.avioconsulting.mule.testing.dsl.ConnectorType
import com.fasterxml.jackson.databind.ObjectMapper
import org.mule.api.MuleContext

class JacksonInputTransformer extends Common {
    def mapper = new ObjectMapper()
    private final List<Class> inputClasses

    JacksonInputTransformer(MuleContext muleContext,
                            ConnectorType connectorType,
                            List<Class> allowedPayloadTypes,
                            List<Class> inputClasses) {
        super(muleContext, connectorType, allowedPayloadTypes)
        this.inputClasses = inputClasses
    }

    JacksonInputTransformer(MuleContext muleContext,
                            ConnectorType connectorType,
                            List<Class> allowedPayloadTypes,
                            Class inputClass) {
        super(muleContext, connectorType, allowedPayloadTypes)
        this.inputClasses = [inputClass]
    }

    def transform(String jsonString) {
        def errors = []
        for (def klass : inputClasses) {
            try {
                return mapper.readValue(jsonString, klass)
            }
            catch (e) {
                errors << e
            }
        }
        throw new Exception("Unable to do Jackson deserialization using classes ${inputClasses}, errors: ${errors}!")
    }
}
