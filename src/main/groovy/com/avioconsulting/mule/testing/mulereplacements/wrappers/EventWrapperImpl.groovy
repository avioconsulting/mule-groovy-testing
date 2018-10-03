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
                             MessageWrapper newMessageWrapper) {
        this.nativeEvent = existingNativeEvent
        this.message = newMessageWrapper
        this.runtimeBridgeMuleSide = null
    }

    EventWrapper createNewEventFromOld(MessageWrapperImpl newMessage) {
        if (isInterceptionEvent()) {
            // mocks can't return new events, they have to mutate, so we'll mutate the message
            // inside the underlying event
            this.nativeEvent.message(newMessage.muleMessage)
            // you can't read .message of the mock native event and see the new message so we have to pass it along
            // ourselves to the private constructor above
            return new EventWrapperImpl(this.nativeEvent,
                                        newMessage)
        }
        def muleEvent = runtimeBridgeMuleSide.getEventFromOldEvent(newMessage.getMuleMessage(),
                                                                   this.nativeEvent)
        new EventWrapperImpl(muleEvent,
                             runtimeBridgeMuleSide)
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

    Object getNativeMuleEvent() {
        nativeEvent
    }
}
