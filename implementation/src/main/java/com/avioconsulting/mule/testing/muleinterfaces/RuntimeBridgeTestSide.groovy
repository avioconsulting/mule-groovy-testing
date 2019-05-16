package com.avioconsulting.mule.testing.muleinterfaces

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.*
import groovy.util.logging.Log4j2

@Log4j2
class RuntimeBridgeTestSide extends FetchClassLoaders
        implements InvokerEventFactory {
    private final Object runtimeBridgeMuleSide
    private final MockingConfiguration mockingConfiguration

    String getArtifactName() {
        return artifactName
    }
    private final String artifactName

    RuntimeBridgeTestSide(Object runtimeBridgeMuleSide,
                          String artifactName,
                          MockingConfiguration mockingConfiguration) {
        super(runtimeBridgeMuleSide)
        this.mockingConfiguration = mockingConfiguration
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
        def muleFlowOptional = runtimeBridgeMuleSide.lookupByName(flowName,
                                                                  mockingConfiguration.lazyInitEnabled)
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
                                         String mediaType) {
        def message = new MessageWrapperImpl(payload,
                                             runtimeBridgeMuleSide,
                                             mediaType)
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

    def startMessageSourceFlows() {
        def flows = mockingConfiguration.keepListenersOnForTheseFlows
        log.info 'Starting message source flows {} since lazy init is on',
                 flows
        flows.each { flow ->
            runtimeBridgeMuleSide.lookupByName(flow,
                                               mockingConfiguration.lazyInitEnabled)
        }
    }
}
