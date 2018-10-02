package com.avioconsulting.mule.testing.mulereplacements.wrappers

class EventWrapperImpl implements EventWrapper {
    protected MessageWrapper message
    protected final Object nativeEvent

    EventWrapperImpl(Object nativeEvent) {
        this.message = new MessageWrapperImpl(nativeEvent.message)
        this.nativeEvent = nativeEvent
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
    ClassLoader getMuleClassLoader() {
        nativeEvent.getClass().classLoader
    }

    Object getNativeMuleEvent() {
        nativeEvent
    }
}
