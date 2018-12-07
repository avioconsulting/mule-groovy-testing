package com.avioconsulting.mule.testing.muleinterfaces.wrappers

class InterceptEventWrapperImpl extends
        EventWrapperImpl {
    private final Map<String, Object> variableOverrides

    InterceptEventWrapperImpl(Object nativeEvent,
                              Object runtimeBridgeMuleSide) {
        super(nativeEvent,
              runtimeBridgeMuleSide)
        variableOverrides = [:]
    }

    private InterceptEventWrapperImpl(Object existingNativeEvent,
                                      MessageWrapper newMessageWrapper,
                                      Object runtimeBridgeMuleSide,
                                      Map<String, Object> variableOverrides) {
        super(existingNativeEvent,
              newMessageWrapper,
              runtimeBridgeMuleSide)
        this.variableOverrides = variableOverrides
    }

    @Override
    protected EventWrapper withNewMessage(MessageWrapperImpl newMessage) {
        def muleMsg = newMessage.muleMessage
        // mocks can't return new events, they have to mutate, so we'll mutate the message
        // inside the underlying event
        this.nativeEvent.message(muleMsg)
        // you can't read .message of the mock native event (see DefaultInterceptionEvent, in vs. out)
        // and see the new message (it will be the original one)
        // so we have to pass it along ourselves to the private constructor above
        return new InterceptEventWrapperImpl(this.nativeEvent,
                                             newMessage,
                                             runtimeBridgeMuleSide,
                                             variableOverrides)
    }

    @Override
    Object getVariable(String variableName) {
        // just as described in withNewMessage above, you can't read new flowVars, so if we change, we track it
    }

    @Override
    EventWrapper withVariable(String variableName,
                              Object value) {
        this.nativeMuleEvent.addVariable(variableName,
                                         value)
        // just as described in withNewMessage above, you can't read new flowVars, so if we change, we track it
        def newOverrides = this.variableOverrides + [(variableName): value]
        return new InterceptEventWrapperImpl(this.nativeEvent,
                                             this.message,
                                             runtimeBridgeMuleSide,
                                             newOverrides)
    }

    @Override
    EventWrapper withVariable(String variableName,
                              Object value,
                              String mediaType) {
        def resolvedMediaType = runtimeBridgeMuleSide.getMediaType(mediaType)
        this.nativeMuleEvent.addVariable(variableName,
                                         value,
                                         resolvedMediaType)
        // just as described in withNewMessage above, you can't read new flowVars, so if we change, we track it
        def newOverrides = this.variableOverrides + [(variableName): value]
        return new InterceptEventWrapperImpl(this.nativeEvent,
                                             this.message,
                                             runtimeBridgeMuleSide,
                                             newOverrides)
    }
}
