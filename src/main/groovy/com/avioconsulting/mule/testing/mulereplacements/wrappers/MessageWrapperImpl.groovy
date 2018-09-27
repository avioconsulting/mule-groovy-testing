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
        unwrapTypedValue(this.payload)
    }

    // TODO: Refactor: Should we be using this outside of here
    @Deprecated()
    static Object unwrapTypedValue(Object payload) {
        assert payload != null
        assert payload.getClass().name.contains('TypedValue')
        payload.value
    }

    // TODO: Refactor: Should we be using this outside of here
    @Deprecated()
    static boolean isPayloadStreaming(Object payload) {
        assert payload != null
        assert payload.getClass().name.contains('TypedValue')
        payload.dataType.isStreamType()
    }

    @Override
    String getMessageAsString() {
        def value = valueInsideTypedValue
        def klass = value.getClass().name
        if (klass.contains('ManagedCursorStreamProvider')) {
            def cursor = value.openCursor()
            try {
                def result = cursor.text
                return result
            }
            finally {
                cursor.close()
            }
        } else if (klass == String.name) {
            return value
        } else {
            throw new Exception("Do not know how to handle payload of type ${klass}")
        }
    }

    private def getDataType() {
        muleMessage.payload.dataType
    }

    @Override
    String getMimeType() {
        dataType.mimeType.toString()
    }

    @Override
    Class getDataTypeClass() {
        dataType.getType()
    }
}
