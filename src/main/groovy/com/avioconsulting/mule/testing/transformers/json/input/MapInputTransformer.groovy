package com.avioconsulting.mule.testing.transformers.json.input

import com.avioconsulting.mule.testing.dsl.ConnectorType
import groovy.json.JsonSlurper
import org.mule.api.MuleContext

class MapInputTransformer extends Common {
    MapInputTransformer(MuleContext muleContext,
                        ConnectorType connectorType,
                        List<Class> allowedPayloadTypes) {
        super(muleContext, connectorType, allowedPayloadTypes)
    }

    def transform(String jsonString) {
        new JsonSlurper().parseText(jsonString)
    }
}
