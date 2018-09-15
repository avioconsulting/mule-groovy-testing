package com.avioconsulting.mule.testing.mulereplacements.wrappers

class EventWrapperImpl implements EventWrapper {
    private final MessageWrapper message
    private final Object nativeEvent

    EventWrapperImpl(Object nativeEvent) {
        this.message = new MessageWrapperImpl(nativeEvent.message)
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
