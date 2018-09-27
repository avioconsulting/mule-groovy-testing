package com.avioconsulting.mule.testing.transformers.json.input

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.fasterxml.jackson.databind.ObjectMapper

class JacksonInputTransformer<T extends ConnectorInfo> extends Common<T> {
    def mapper = new ObjectMapper()
    private final List<Class> inputClasses

    JacksonInputTransformer(IPayloadValidator<T> payloadValidator,
                            List<Class> inputClasses) {
        super(payloadValidator)
        this.inputClasses = inputClasses
    }

    JacksonInputTransformer(IPayloadValidator<T> payloadValidator,
                            Class inputClass) {
        super(payloadValidator)
        this.inputClasses = [inputClass]
    }

    def transform(String jsonString) {
        if (jsonString == '' || jsonString == null) {
            return null
        }
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
