package com.avioconsulting.mule.testing.mulereplacements.wrappers

class MessageWrapperImpl implements MessageWrapper {
    private final Object muleMessage
    private final Object payload

    MessageWrapperImpl(Object payload,
                       Object messageBuilder) {
        this.muleMessage = messageBuilder.value(payload).build()
        this.payload = payload
    }

    Object getMuleMessage() {
        this.muleMessage
    }

    @Override
    Object getPayload() {
        this.payload
    }
}
