package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.messages.JsonMessage
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class JSONJacksonRequestReplyTransformer implements MuleMessageTransformer,
        JsonMessage {
    private final Closure closure
    private final MuleContext muleContext
    private final Class inputClass

    JSONJacksonRequestReplyTransformer(Closure closure,
                                       MuleContext muleContext,
                                       Class inputClass) {
        this.inputClass = inputClass
        this.muleContext = muleContext
        this.closure = closure
    }

    MuleMessage transform(MuleMessage muleMessage) {
        def jsonText = muleMessage.payloadAsString
        def mapper = new ObjectMapper()
        def input = mapper.readValue(jsonText, inputClass)
        def response = this.closure(input)
        def jsonString = mapper.writer().writeValueAsString(response)
        getJSONMessage(jsonString, muleContext)
    }
}
