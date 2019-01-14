package com.avioconsulting.mule.testing.transformers.json.input

import com.avioconsulting.mule.testing.TestingFrameworkException
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Log4j2

@Log4j2
class JacksonInputTransformer<T extends ConnectorInfo> extends
        Common<T> {
    def mapper = new ObjectMapper()
    private final List<Class> inputClasses

    JacksonInputTransformer(List<Class> inputClasses) {
        this.inputClasses = inputClasses
    }

    JacksonInputTransformer(Class inputClass) {
        this.inputClasses = [inputClass]
    }

    def transform(String jsonString) {
        if (jsonString == '' || jsonString == null) {
            log.info 'No JSON to unmarshal'
            return null
        }
        def errors = []
        for (def klass : inputClasses) {
            try {
                log.info 'Unmarshalling JSON {} to klass {}',
                         jsonString,
                         klass
                return mapper.readValue(jsonString,
                                        klass)
            }
            catch (e) {
                errors << e
            }
        }
        throw new TestingFrameworkException("Unable to do Jackson deserialization using classes ${inputClasses}, errors: ${errors}!")
    }
}
