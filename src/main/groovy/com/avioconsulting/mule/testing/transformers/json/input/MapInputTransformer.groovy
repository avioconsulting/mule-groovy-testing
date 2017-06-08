package com.avioconsulting.mule.testing.transformers.json.input

import com.avioconsulting.mule.testing.dsl.mocking.ConnectorType
import groovy.json.JsonSlurper
import org.mule.api.MuleContext

class MapInputTransformer extends Common {
    MapInputTransformer(MuleContext muleContext,
                        ConnectorType mockedConnectorType,
                        Class expectedPayloadType) {
        super(muleContext, mockedConnectorType, expectedPayloadType)
    }

    def transform(String jsonString) {
        new JsonSlurper().parseText(jsonString)
    }
}
