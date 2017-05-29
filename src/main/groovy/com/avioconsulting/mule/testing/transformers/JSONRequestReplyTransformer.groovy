package com.avioconsulting.mule.testing.transformers

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.mule.DefaultMuleMessage
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class JSONRequestReplyTransformer implements MuleMessageTransformer {
    private final Closure closure
    private final MuleContext muleContext

    JSONRequestReplyTransformer(Closure closure, MuleContext muleContext) {
        this.muleContext = muleContext
        this.closure = closure
    }

    MuleMessage transform(MuleMessage muleMessage) {
        def jsonText = muleMessage.payloadAsString
        def map = new JsonSlurper().parseText(jsonText)
        def response = this.closure(map)
        assert response instanceof Map
        def jsonString = JsonOutput.toJson(response)
        def messageProps = [
                'content-type': 'application/json; charset=utf-8',
                'http.status' : 200
        ]
        def payload = new ByteArrayInputStream(jsonString.bytes)
        new DefaultMuleMessage(payload,
                               messageProps,
                               null,
                               null,
                               muleContext)
    }
}
