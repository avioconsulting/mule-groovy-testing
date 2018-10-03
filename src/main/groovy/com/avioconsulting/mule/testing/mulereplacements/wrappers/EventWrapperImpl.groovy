package com.avioconsulting.mule.testing.mulereplacements.wrappers

class EventWrapperImpl implements
        EventWrapper {
    protected final MessageWrapper message
    protected final Object nativeEvent

    EventWrapperImpl(Object nativeEvent) {
        this.message = new MessageWrapperImpl(nativeEvent.message)
        this.nativeEvent = nativeEvent
    }

    private EventWrapperImpl(Object existingNativeEvent,
                             MessageWrapper newMessageWrapper) {
        this.nativeEvent = existingNativeEvent
        this.message = newMessageWrapper
    }

    EventWrapper createNewEventFromOld(Object runtimeBridgeMuleSide,
                                       MessageWrapperImpl newMessage) {
        if (nativeEvent.class.name.contains('InterceptionEvent')) {
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
        new EventWrapperImpl(muleEvent)
    }

    @Override
    MessageWrapper getMessage() {
        message
    }

    @Override
    String getMessageAsString() {
        message.messageAsString
    }

    Object getNativeMuleEvent() {
        nativeEvent
    }
}
