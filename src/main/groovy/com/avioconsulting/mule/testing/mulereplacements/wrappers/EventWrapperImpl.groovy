package com.avioconsulting.mule.testing.mulereplacements.wrappers

class EventWrapperImpl implements EventWrapper {
    private final MessageWrapper message
    private final Object nativeEvent

    EventWrapperImpl(MessageWrapper message, Object nativeEvent) {
        this.message = message
        this.nativeEvent = nativeEvent
    }

    @Override
    MessageWrapper getMessage() {
        message
    }

    Object getNativeMuleEvent() {
        nativeEvent
    }
}
