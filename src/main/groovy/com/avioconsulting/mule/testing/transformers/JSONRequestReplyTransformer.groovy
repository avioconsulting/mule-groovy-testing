package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.messages.JsonMessage
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class JSONRequestReplyTransformer implements MuleMessageTransformer,
        JsonMessage {
    private final Closure closure
    private final MuleContext muleContext

    JSONRequestReplyTransformer(Closure closure,
                                MuleContext muleContext) {
        this.muleContext = muleContext
        this.closure = closure
    }

    MuleMessage transform(MuleMessage muleMessage) {
        def jsonText = muleMessage.payloadAsString
        def map = new JsonSlurper().parseText(jsonText)
        def response = this.closure(map)
        assert response instanceof Map
        def jsonString = JsonOutput.toJson(response)
        getJSONMessage(jsonString, muleContext)
    }
}
