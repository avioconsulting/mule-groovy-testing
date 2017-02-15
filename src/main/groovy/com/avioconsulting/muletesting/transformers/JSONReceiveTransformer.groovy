package com.avioconsulting.muletesting.transformers

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonSlurper
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class JSONReceiveTransformer implements MuleMessageTransformer {
    private final Class jsonClass
    private final YieldType yieldType
    private final Object closure

    def JSONReceiveTransformer(Class jsonClass, YieldType yieldType, closure) {
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
        this.closure(yieldObject)
        // we have no reply
        muleMessage
    }
}
