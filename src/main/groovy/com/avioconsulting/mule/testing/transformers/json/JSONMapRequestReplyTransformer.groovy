package com.avioconsulting.mule.testing.transformers.json

import com.avioconsulting.mule.testing.dsl.mocking.MockedConnectorType
import com.avioconsulting.mule.testing.messages.JsonMessage
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.mule.api.MuleContext
import org.mule.api.MuleMessage

class JSONMapRequestReplyTransformer extends JSONTransformer implements
        JsonMessage {
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
        assert response instanceof Map
        def jsonString = JsonOutput.toJson(response)
        getJSONMessage(jsonString, muleContext)
    }
}
