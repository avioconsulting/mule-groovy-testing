package com.avioconsulting.mule.testing.muleinterfaces.wrappers

class EventWrapperImpl implements
        EventWrapper {
    protected final MessageWrapper message
    protected final Object nativeEvent
    protected final Object runtimeBridgeMuleSide

    EventWrapperImpl(Object nativeEvent,
                     Object runtimeBridgeMuleSide) {
        this.runtimeBridgeMuleSide = runtimeBridgeMuleSide
        this.message = new MessageWrapperImpl(nativeEvent.message)
        this.nativeEvent = nativeEvent
    }

    protected EventWrapperImpl(Object existingNativeEvent,
                               MessageWrapper newMessageWrapper,
                               Object runtimeBridgeMuleSide) {
        this.nativeEvent = existingNativeEvent
        this.message = newMessageWrapper
        this.runtimeBridgeMuleSide = runtimeBridgeMuleSide
    }

    protected EventWrapper withNewMessage(MessageWrapperImpl newMessage) {
        def muleMsg = newMessage.muleMessage
        def muleEvent = runtimeBridgeMuleSide.getEventFromOldEvent(muleMsg,
                                                                   this.nativeEvent)
        new EventWrapperImpl(muleEvent,
                             runtimeBridgeMuleSide)
    }

    @Override
    EventWrapper withNewPayload(Object payload,
                                String mediaType) {
        def message = new MessageWrapperImpl(payload,
                                             runtimeBridgeMuleSide,
                                             mediaType)
        withNewMessage(message)
    }

    @Override
    EventWrapper withSoapPayload(String xmlPayload,
                                 Map attributes) {
        def stream = new ByteArrayInputStream(xmlPayload.bytes)
        def soapOutputPayloadClass = runtimeBridgeMuleSide
                .getAppClassloader()
                .loadClass('org.mule.runtime.extension.api.soap.SoapOutputPayload')
        def streamTypedValue = runtimeBridgeMuleSide.getSoapTypedValue(stream)
        def soapOutputPayload = soapOutputPayloadClass.newInstance(streamTypedValue,
                                                                   [:], // attachments
                                                                   [:]) // headers
        def message = new MessageWrapperImpl(soapOutputPayload,
                                             runtimeBridgeMuleSide,
                                             'application/java',
                                             attributes)
        withNewMessage(message)
    }

    @Override
    EventWrapper withNewPayload(Object payload,
                                String mediaType,
                                Map attributes) {
        def message = new MessageWrapperImpl(payload,
                                             runtimeBridgeMuleSide,
                                             mediaType,
                                             attributes)
        withNewMessage(message)
    }

    @Override
    EventWrapper withNewStreamingPayload(String payload,
                                         String mediaType,
                                         Map attributes,
                                         boolean useRepeatableStream) {
        def stream = new ByteArrayInputStream(payload.bytes)
        MessageWrapperImpl message
        if (useRepeatableStream) {
            def streamProvider = runtimeBridgeMuleSide.getMuleStreamCursor(this.nativeEvent,
                                                                           stream)
            message = new MessageWrapperImpl(streamProvider,
                                             runtimeBridgeMuleSide,
                                             mediaType,
                                             attributes)
        } else {
            message = new MessageWrapperImpl(stream,
                                             runtimeBridgeMuleSide,
                                             mediaType,
                                             attributes)
        }
        withNewMessage(message)
    }

    @Override
    MessageWrapper getMessage() {
        message
    }

    @Override
    String getMessageAsString() {
        message.messageAsString
    }

    @Override
    Object getVariable(String variableName) {
        nativeEvent.getVariables()[variableName]
    }

    @Override
    EventWrapper withVariable(String variableName,
                              Object value) {
        def msg = this.message as MessageWrapperImpl
        def muleEvent = runtimeBridgeMuleSide.getEventFromOldEvent(msg.muleMessage,
                                                                   this.nativeEvent,
                                                                   variableName,
                                                                   value)
        new EventWrapperImpl(muleEvent,
                             runtimeBridgeMuleSide)
    }

    @Override
    EventWrapper withNewAttributes(attributes) {
        def message = new MessageWrapperImpl(this.message.payload,
                                             runtimeBridgeMuleSide,
                                             this.message.mimeType,
                                             attributes)
        withNewMessage(message)
    }

    Object getNativeMuleEvent() {
        nativeEvent
    }


    @Override
    String toString() {
        nativeEvent.toString()
    }
}
