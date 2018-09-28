package com.avioconsulting.mule.testing.mulereplacements.wrappers

class MockEventWrapperImpl extends EventWrapperImpl implements MockEventWrapper {
    /**
     *
     * @param nativeEvent - org.mule.runtime.api.interception.InterceptionEvent
     */
    MockEventWrapperImpl(Object nativeEvent) {
        super(nativeEvent)
    }

    @Override
    void changeMessage(MessageWrapper messageWrapper) {
        assert messageWrapper instanceof MessageWrapperImpl
        nativeEvent.message(messageWrapper.muleMessage)
        // in case other transformers look at it
        this.message = messageWrapper
    }
}
