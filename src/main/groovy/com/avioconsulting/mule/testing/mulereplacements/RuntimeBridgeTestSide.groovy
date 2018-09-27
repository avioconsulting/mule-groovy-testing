package com.avioconsulting.mule.testing.mulereplacements

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.MessageFactory
import com.avioconsulting.mule.testing.TransformingEventFactory
import com.avioconsulting.mule.testing.mulereplacements.wrappers.*

class RuntimeBridgeTestSide implements
        InvokerEventFactory,
        TransformingEventFactory,
        MessageFactory {
    private final Object runtimeBridgeMuleSide

    String getArtifactName() {
        return artifactName
    }
    private final String artifactName

    RuntimeBridgeTestSide(Object runtimeBridgeMuleSide, String artifactName) {
        this.runtimeBridgeMuleSide = runtimeBridgeMuleSide
        this.artifactName = artifactName
    }

    FlowWrapper getFlow(String flowName) {
        def muleFlowOptional = runtimeBridgeMuleSide.lookupByName(flowName)
        assert muleFlowOptional.isPresent(): "Flow with name '${flowName}' was not found. Are you using the right flow name?"
        def muleFlow = muleFlowOptional.get()
        new FlowWrapper(muleFlow.name, muleFlow)
    }

    private EventWrapper getMuleEvent(MessageWrapper message, String flowName) {
        assert message instanceof MessageWrapperImpl
        def muleEvent = runtimeBridgeMuleSide.getNewEvent(message.muleMessage,
                                                          flowName)
        new EventWrapperImpl(muleEvent)
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
                                         Map properties) {
        assert false: 'NIE'
    }

    @Override
    EventWrapper getMuleEventWithPayload(Object payload,
                                         EventWrapper rewriteEvent) {
        assert false: 'NIE'
    }

    @Override
    EventWrapper getMuleEventWithPayload(Object payload,
                                         EventWrapper rewriteEvent,
                                         Map attributes) {
        getMuleEventWithPayload(payload,
                                rewriteEvent,
                                null,
                                attributes)
    }

    @Override
    EventWrapper getMuleEventWithPayload(Object payload,
                                         EventWrapper rewriteEvent,
                                         String mediaType,
                                         Map attributes) {
        // TODO: Might be able to more strongly type this at some point and avoid the if
        if (rewriteEvent instanceof MockEventWrapper) {
            assert false: 'Implement this path'
        }
        def message = new MessageWrapperImpl(payload,
                                             runtimeBridgeMuleSide,
                                             mediaType,
                                             attributes)
        def muleEvent = runtimeBridgeMuleSide.getEventFromOldEvent(message.getMuleMessage(),
                                                                   rewriteEvent.getNativeMuleEvent())
        new EventWrapperImpl(muleEvent)
    }

    @Override
    EventWrapper getStreamedMuleEventWithPayload(String payload,
                                                 EventWrapper rewriteEvent,
                                                 String mediaType,
                                                 Map attributes) {
        assert rewriteEvent instanceof EventWrapperImpl
        def stream = new ByteArrayInputStream(payload.bytes)
        def streamProvider = runtimeBridgeMuleSide.getMuleStreamCursor(rewriteEvent.nativeMuleEvent,
                                                                       stream)
        getMuleEventWithPayload(streamProvider,
                                rewriteEvent,
                                mediaType,
                                attributes)
    }

    @Override
    MessageWrapper buildMessage(Object payload) {
        new MessageWrapperImpl(payload,
                               runtimeBridgeMuleSide)
    }
}
