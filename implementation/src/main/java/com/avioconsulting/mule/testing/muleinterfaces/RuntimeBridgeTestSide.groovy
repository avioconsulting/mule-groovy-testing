package com.avioconsulting.mule.testing.muleinterfaces

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.*

class RuntimeBridgeTestSide implements
        InvokerEventFactory,
        IFetchClassLoaders {
    private final Object runtimeBridgeMuleSide

    String getArtifactName() {
        return artifactName
    }
    private final String artifactName

    RuntimeBridgeTestSide(Object runtimeBridgeMuleSide,
                          String artifactName) {
        this.runtimeBridgeMuleSide = runtimeBridgeMuleSide
        this.artifactName = artifactName
    }

    FlowWrapper getFlow(String flowName) {
        def muleFlow = getNativeFlow(flowName)
        new FlowWrapper(muleFlow.name,
                        muleFlow,
                        runtimeBridgeMuleSide)
    }

    private Object getNativeFlow(String flowName) {
        def muleFlowOptional = runtimeBridgeMuleSide.lookupByName(flowName)
        assert muleFlowOptional.isPresent(): "Flow with name '${flowName}' was not found. Are you using the right flow name?"
        muleFlowOptional.get()
    }

    private EventWrapper getMuleEvent(MessageWrapper message,
                                      String flowName) {
        assert message instanceof MessageWrapperImpl
        def flow = getNativeFlow(flowName)
        def muleEvent = runtimeBridgeMuleSide.getNewEvent(message.muleMessage,
                                                          flow)
        new EventWrapperImpl(muleEvent,
                             runtimeBridgeMuleSide)
    }

    @Override
    EventWrapper getMuleEventWithPayload(Object payload,
                                         String flowName) {
        def message = new MessageWrapperImpl(payload,
                                             runtimeBridgeMuleSide)
        getMuleEvent(message,
                     flowName)
    }

    @Override
    EventWrapper getMuleEventWithPayload(Object payload,
                                         String flowName,
                                         Map attributes) {
        assert false: 'Not yet implemented'
    }

    def dispose() {
        runtimeBridgeMuleSide.dispose()
    }

    ClassLoader getAppClassloader() {
        runtimeBridgeMuleSide.getAppClassloader()
    }

    @Override
    ClassLoader getRuntimeClassLoader() {
        runtimeBridgeMuleSide.getRuntimeClassLoader()
    }

    BatchNotifyListenerWrapper getBatchNotifyListener() {
        new BatchNotifyListenerWrapper(runtimeBridgeMuleSide.getBatchNotifyListener())
    }

    InvokeExceptionWrapper createInvocationException(Exception cause) {
        assert cause.getClass().getName().contains('MessagingException')
        def message = new MessageWrapperImpl(cause.muleMessage)
        def event = new EventWrapperImpl(cause.event,
                                         runtimeBridgeMuleSide)
        new InvokeExceptionWrapper(cause,
                                   message,
                                   event)
    }
}
