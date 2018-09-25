package com.avioconsulting.mule.testing.mulereplacements.wrappers

class MessageWrapperImpl implements
        MessageWrapper {
    private final Object muleMessage
    private final Object payload

    /**
     *
     * @param payload
     * @param messageBuilder - org.mule.runtime.api.message.Message.Builder
     */
    MessageWrapperImpl(Object payload,
                       Object runtimeBridgeMuleSide,
                       String mediaType = null,
                       Object attributes = null) {
        def messageBuilder = runtimeBridgeMuleSide.messageBuilder
        messageBuilder = messageBuilder.value(payload)
        if (mediaType) {
            messageBuilder = messageBuilder.mediaType(runtimeBridgeMuleSide.getMediaType(mediaType))
        }
        if (attributes) {
            messageBuilder = messageBuilder.attributesValue(attributes)
        }
        this.muleMessage = messageBuilder.build()
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

    @Override
    String getMessageAsString() {
        def value = valueInsideTypedValue
        def klass = value.getClass().name
        if (klass.contains('ManagedCursorStreamProvider')) {
            // TODO: java.lang.IllegalStateException: Cannot open a new cursor on a closed stream
            def cursor = value.openCursor()
            try {
                def result = cursor.text
                return result
            }
            finally {
                cursor.close()
            }
        }
        else {
            throw new Exception("Do not know how to handle payload of type ${klass}")
        }
    }
}
