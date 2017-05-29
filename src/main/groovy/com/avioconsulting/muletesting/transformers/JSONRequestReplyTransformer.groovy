package com.avioconsulting.muletesting.transformers

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonSlurper
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class JSONRequestReplyTransformer implements MuleMessageTransformer {
    private final Class jsonClass
    private final YieldType yieldType
    private final Object closure

    JSONRequestReplyTransformer(Class jsonClass, YieldType yieldType, closure) {
        this.closure = closure
        this.yieldType = yieldType
        this.jsonClass = jsonClass
    }

    MuleMessage transform(MuleMessage muleMessage) {
        def jsonText = muleMessage.payloadAsString
        def mapper = new ObjectMapper()
        def deserialized = mapper.readValue jsonText, this.jsonClass
        def map = new JsonSlurper().parseText(jsonText)
        def yieldObject = this.yieldType == YieldType.Map ? map : deserialized
        def response = this.closure(yieldObject)
        if (!(response instanceof MuleMessage)) {
            throw new Exception(
                    "This has only been implemented for closures that return complete messages, you returned ${response.class}")
        }
        response
    }
}
