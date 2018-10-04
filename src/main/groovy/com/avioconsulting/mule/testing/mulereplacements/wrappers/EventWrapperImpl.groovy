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

    EventWrapper createNewEventFromOld(MessageWrapperImpl newMessage) {
        def muleMsg = newMessage.muleMessage
        if (isInterceptionEvent()) {
            // mocks can't return new events, they have to mutate, so we'll mutate the message
            // inside the underlying event
            this.nativeEvent.message(muleMsg)
            // you can't read .message of the mock native event and see the new message so we have to pass it along
            // ourselves to the private constructor above
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
    EventWrapper createNewEventFromOld(Object payload,
                                       String mediaType) {
        def message = new MessageWrapperImpl(payload,
                                             runtimeBridgeMuleSide,
                                             mediaType)
        createNewEventFromOld(message)
    }

    @Override
    EventWrapper newStreamedEvent(String payload,
                                  String mediaType,
                                  Map attributes) {
        def stream = new ByteArrayInputStream(payload.bytes)
        def streamProvider = runtimeBridgeMuleSide.getMuleStreamCursor(this.nativeEvent,
                                                                       stream)
        def message = new MessageWrapperImpl(streamProvider,
                                             runtimeBridgeMuleSide,
                                             mediaType,
                                             attributes)
        createNewEventFromOld(message)
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
    EventWrapper withNewAttributes(Map attributes) {
        def message = new MessageWrapperImpl(this.message.payload,
                                             runtimeBridgeMuleSide,
                                             this.message.mimeType,
                                             attributes)
        createNewEventFromOld(message)
    }

    Object getNativeMuleEvent() {
        nativeEvent
    }
}
