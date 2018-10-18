package com.avioconsulting.mule.testing.mulereplacements.wrappers

class EventWrapperImpl implements
        EventWrapper {
    protected final MessageWrapper message
    protected final Object nativeEvent
    private final Object runtimeBridgeMuleSide

    EventWrapperImpl(Object nativeEvent,
                     Object runtimeBridgeMuleSide) {
        this.runtimeBridgeMuleSide = runtimeBridgeMuleSide
        this.message = new MessageWrapperImpl(nativeEvent.message)
        this.nativeEvent = nativeEvent
    }

    private EventWrapperImpl(Object existingNativeEvent,
                             MessageWrapper newMessageWrapper,
                             Object runtimeBridgeMuleSide) {
        this.nativeEvent = existingNativeEvent
        this.message = newMessageWrapper
        this.runtimeBridgeMuleSide = runtimeBridgeMuleSide
    }

    EventWrapper withNewPayload(MessageWrapperImpl newMessage) {
        def muleMsg = newMessage.muleMessage
        if (isInterceptionEvent()) {
            // mocks can't return new events, they have to mutate, so we'll mutate the message
            // inside the underlying event
            this.nativeEvent.message(muleMsg)
            // you can't read .message of the mock native event and see the new message (it will be the original one)
            // so we have to pass it along ourselves to the private constructor above
            return new EventWrapperImpl(this.nativeEvent,
                                        newMessage,
                                        runtimeBridgeMuleSide)
        }
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
        withNewPayload(message)
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
        withNewPayload(message)
    }

    @Override
    EventWrapper withNewPayload(Object payload,
                                String mediaType,
                                Map attributes) {
        def message = new MessageWrapperImpl(payload,
                                             runtimeBridgeMuleSide,
                                             mediaType,
                                             attributes)
        withNewPayload(message)
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
        withNewPayload(message)
    }

    private boolean isInterceptionEvent() {
        nativeEvent.class.name.contains('InterceptionEvent')
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
        assert !isInterceptionEvent(): 'Have not implemented this'
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
        withNewPayload(message)
    }

    Object getNativeMuleEvent() {
        nativeEvent
    }


    @Override
    String toString() {
        nativeEvent.toString()
    }
}
