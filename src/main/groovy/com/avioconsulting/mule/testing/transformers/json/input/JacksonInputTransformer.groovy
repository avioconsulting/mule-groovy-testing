package com.avioconsulting.mule.testing.transformers.json.input

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.fasterxml.jackson.databind.ObjectMapper
import org.mule.api.MuleContext

class JacksonInputTransformer extends Common {
    def mapper = new ObjectMapper()
    private final List<Class> inputClasses

    JacksonInputTransformer(MuleContext muleContext,
                            IPayloadValidator payloadValidator,
                            List<Class> inputClasses) {
        super(muleContext, payloadValidator)
        this.inputClasses = inputClasses
    }

    JacksonInputTransformer(MuleContext muleContext,
                            IPayloadValidator payloadValidator,
                            Class inputClass) {
        super(muleContext, payloadValidator)
        this.inputClasses = [inputClass]
    }

    def transform(String jsonString) {
        if (jsonString == '') {
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
