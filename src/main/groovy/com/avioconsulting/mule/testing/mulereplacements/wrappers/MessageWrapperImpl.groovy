package com.avioconsulting.mule.testing.mulereplacements.wrappers

class MessageWrapperImpl implements MessageWrapper {
    private final Object muleMessage
    private final Object payload

    /**
     *
     * @param payload
     * @param messageBuilder - org.mule.runtime.api.message.Message.Builder
     */
    MessageWrapperImpl(Object payload,
                       Object messageBuilder) {
        this.muleMessage = messageBuilder.value(payload).build()
        this.payload = payload
    }

    MessageWrapperImpl(Object nativeMuleMessage) {
        this.muleMessage = nativeMuleMessage
        this.payload = nativeMuleMessage.payload
    }

    Object getMuleMessage() {
        this.muleMessage
    }

    @Override
    Object getPayload() {
        this.payload
    }

    @Override
    Object getValueInsideTypedValue() {
        assert payload != null
        assert payload.getClass().name.contains('TypedValue')
        payload.value
    }
}
