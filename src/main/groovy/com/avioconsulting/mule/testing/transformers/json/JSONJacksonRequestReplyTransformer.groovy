package com.avioconsulting.mule.testing.transformers.json

import com.avioconsulting.mule.testing.dsl.mocking.MockedConnectorType
import com.avioconsulting.mule.testing.messages.JsonMessage
import com.avioconsulting.mule.testing.transformers.OutputTransformer
import com.fasterxml.jackson.databind.ObjectMapper
import org.mule.api.MuleContext
import org.mule.api.MuleMessage

class JSONJacksonRequestReplyTransformer extends JSONTransformer
        implements JsonMessage, OutputTransformer {
    private final Closure closure
    private final MuleContext muleContext
    private final Class inputClass
    def mapper = new ObjectMapper()

    JSONJacksonRequestReplyTransformer(Closure closure,
                                       MuleContext muleContext,
                                       Class inputClass,
                                       Class expectedPayloadType,
                                       MockedConnectorType mockedConnectorType) {
        super(expectedPayloadType,
              muleContext,
              mockedConnectorType)
        this.inputClass = inputClass
        this.muleContext = muleContext
        this.closure = closure
    }

    MuleMessage transform(String jsonText) {
        def input = mapper.readValue(jsonText, inputClass)
        def response = this.closure(input)
        transformOutput(response)
    }

    MuleMessage transformOutput(Object response) {
        def jsonString = mapper.writer().writeValueAsString(response)
        getJSONMessage(jsonString, muleContext)
    }
}
