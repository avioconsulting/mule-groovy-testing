package com.avioconsulting.mule.testing.transformers.json

import com.avioconsulting.mule.testing.dsl.mocking.MockedConnectorType
import com.avioconsulting.mule.testing.messages.JsonMessage
import com.avioconsulting.mule.testing.transformers.OutputTransformer
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.mule.api.MuleContext
import org.mule.api.MuleMessage

class JSONMapRequestReplyTransformer extends JSONTransformer implements
        JsonMessage, OutputTransformer {
    private final Closure closure
    private final MuleContext muleContext

    JSONMapRequestReplyTransformer(Closure closure,
                                   MuleContext muleContext,
                                   Class expectedPayloadType,
                                   MockedConnectorType mockedConnectorType) {
        super(expectedPayloadType,
              muleContext,
              mockedConnectorType)
        this.muleContext = muleContext
        this.closure = closure
    }

    MuleMessage transform(String jsonText) {
        def map = new JsonSlurper().parseText(jsonText)
        def response = this.closure(map)
        transformOutput(response)
    }

    MuleMessage transformOutput(Object response) {
        assert response instanceof Map
        def jsonString = JsonOutput.toJson(response)
        getJSONMessage(jsonString, muleContext)
    }
}
