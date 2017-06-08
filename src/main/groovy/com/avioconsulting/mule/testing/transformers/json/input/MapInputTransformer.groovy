package com.avioconsulting.mule.testing.transformers.json.input

import com.avioconsulting.mule.testing.dsl.mocking.ConnectorType
import groovy.json.JsonSlurper
import org.mule.api.MuleContext

class MapInputTransformer extends Common {
    MapInputTransformer(MuleContext muleContext,
                        ConnectorType mockedConnectorType,
                        List<Class> allowedPayloadTypes) {
        super(muleContext, mockedConnectorType, allowedPayloadTypes)
    }

    def transform(String jsonString) {
        new JsonSlurper().parseText(jsonString)
    }
}
