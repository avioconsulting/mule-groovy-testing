package com.avioconsulting.mule.testing.mulereplacements.wrappers

class InterceptEventWrapperImpl extends
        EventWrapperImpl {
    InterceptEventWrapperImpl(Object nativeEvent,
                              Object runtimeBridgeMuleSide) {
        super(nativeEvent,
              runtimeBridgeMuleSide)
    }

    private InterceptEventWrapperImpl(Object existingNativeEvent,
                                      MessageWrapper newMessageWrapper,
                                      Object runtimeBridgeMuleSide) {
        super(existingNativeEvent,
              newMessageWrapper,
              runtimeBridgeMuleSide)
    }

    @Override
    protected EventWrapper withNewMessage(MessageWrapperImpl newMessage) {
        def muleMsg = newMessage.muleMessage
        // mocks can't return new events, they have to mutate, so we'll mutate the message
        // inside the underlying event
        this.nativeEvent.message(muleMsg)
        // you can't read .message of the mock native event and see the new message (it will be the original one)
        // so we have to pass it along ourselves to the private constructor above
        return new InterceptEventWrapperImpl(this.nativeEvent,
                                             newMessage,
                                             runtimeBridgeMuleSide)
    }

    @Override
    EventWrapper withVariable(String variableName,
                              Object value) {
        throw new Exception('Have not implemented this yet!')
    }
}
