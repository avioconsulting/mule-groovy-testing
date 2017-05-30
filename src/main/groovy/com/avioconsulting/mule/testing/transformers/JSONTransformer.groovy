package com.avioconsulting.mule.testing.transformers

import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

abstract class JSONTransformer implements MuleMessageTransformer {
    private final Class expectedPayloadType

    JSONTransformer(Class expectedPayloadType) {
        this.expectedPayloadType = expectedPayloadType
    }

    abstract MuleMessage transform(String jsonString)

    MuleMessage transform(MuleMessage muleMessage) {
        def payload = muleMessage.payload
        if (expectedPayloadType.isInstance(payload)) {
            return transform(muleMessage.payloadAsString)
        }
        throw new Exception("Expected payload to be of type ${expectedPayloadType} here but it actually was ${payload.class}. Check the connectors you're mocking and make sure you transformed the payload properly! (e.g. payload into VMs must be Strings)")
    }
}
