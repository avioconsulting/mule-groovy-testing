package com.avioconsulting.mule.testing.transformers.json

import com.avioconsulting.mule.testing.messages.JsonMessage
import com.fasterxml.jackson.databind.ObjectMapper
import org.mule.api.MuleContext
import org.mule.api.MuleMessage

class JSONJacksonRequestReplyTransformer extends JSONTransformer
        implements JsonMessage {
    private final Closure closure
    private final MuleContext muleContext
    private final Class inputClass

    JSONJacksonRequestReplyTransformer(Closure closure,
                                       MuleContext muleContext,
                                       Class inputClass,
                                       Class expectedPayloadType) {
        super(expectedPayloadType)
        this.inputClass = inputClass
        this.muleContext = muleContext
        this.closure = closure
    }

    MuleMessage transform(String jsonText) {
        def mapper = new ObjectMapper()
        def input = mapper.readValue(jsonText, inputClass)
        def response = this.closure(input)
        def jsonString = mapper.writer().writeValueAsString(response)
        getJSONMessage(jsonString, muleContext)
    }
}
