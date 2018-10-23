package com.avioconsulting.mule.testing.muleinterfaces.wrappers

class MessageWrapperImpl implements
        MessageWrapper {
    static final String TYPED_VALUE_CLASS_NAME = 'org.mule.runtime.api.metadata.TypedValue'
    private final Object muleMessage

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
        messageBuilder = payload != null && payload.getClass().name == TYPED_VALUE_CLASS_NAME ?
                messageBuilder.payload(payload) :
                messageBuilder.value(payload)
        if (mediaType) {
            messageBuilder = messageBuilder.mediaType(runtimeBridgeMuleSide.getMediaType(mediaType))
        }
        if (attributes) {
            messageBuilder = messageBuilder.attributesValue(attributes)
        }
        this.muleMessage = messageBuilder.build()
    }

    MessageWrapperImpl(Object nativeMuleMessage) {
        this.muleMessage = nativeMuleMessage
    }

    Object getMuleMessage() {
        this.muleMessage
    }

    @Override
    Object getPayload() {
        // Mule will wrap our payload in a typed value. We'd like to have consistent access
        this.muleMessage.payload
    }

    @Override
    Object getValueInsideTypedValue() {
        def payload = this.payload
        assert payload != null
        assert payload.getClass().name == TYPED_VALUE_CLASS_NAME
        payload.value
    }

    @Override
    String getMessageAsString() {
        def value = valueInsideTypedValue
        def klass = value.getClass().name
        // this is what repeatable streams look like
        if (klass.contains('ManagedCursorStreamProvider')) {
            def cursor = value.openCursor()
            try {
                def result = cursor.text
                return result
            }
            finally {
                cursor.close()
            }
        }
        // this is what non repeatable streams look like
        else if (InputStream.isAssignableFrom(value.getClass())) {
            return value.text
        } else if (klass == String.name) {
            return value
        } else if (value == null) {
            return value
        } else {
            throw new Exception("Do not know how to handle payload of type ${klass}")
        }
    }

    private def getDataType() {
        payload.dataType
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
