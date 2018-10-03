package com.avioconsulting.mule.testing.mulereplacements

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.TransformingEventFactory
import com.avioconsulting.mule.testing.mulereplacements.wrappers.*

class RuntimeBridgeTestSide implements
        InvokerEventFactory,
        TransformingEventFactory,
        IFetchAppClassLoader {
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
        def muleFlowOptional = runtimeBridgeMuleSide.lookupByName(flowName)
        assert muleFlowOptional.isPresent(): "Flow with name '${flowName}' was not found. Are you using the right flow name?"
        def muleFlow = muleFlowOptional.get()
        new FlowWrapper(muleFlow.name,
                        muleFlow,
                        runtimeBridgeMuleSide)
    }

    private EventWrapper getMuleEvent(MessageWrapper message, String flowName) {
        assert message instanceof MessageWrapperImpl
        def muleEvent = runtimeBridgeMuleSide.getNewEvent(message.muleMessage,
                                                          flowName)
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

    // TODO: Get rid of this, just use direct stuff in EventWrapperImpl since it now has the bridge
    @Deprecated
    @Override
    EventWrapper getMuleEventWithPayload(Object payload,
                                         String mediaType,
                                         EventWrapper rewriteEvent) {
        getMuleEventWithPayload(payload,
                                rewriteEvent,
                                mediaType,
                                [:])
    }

    // TODO: Get rid of this, just use direct stuff in EventWrapperImpl since it now has the bridge
    @Deprecated
    @Override
    EventWrapper getMuleEventWithAttributes(EventWrapper rewriteEvent,
                                            Map attributes) {
        def existingMessage = rewriteEvent.message
        getMuleEventWithPayload(existingMessage.payload,
                                rewriteEvent,
                                existingMessage.mimeType,
                                attributes)
    }

    // TODO: Get rid of this, just use direct stuff in EventWrapperImpl since it now has the bridge
    @Deprecated
    @Override
    EventWrapper getMuleEventWithPayload(Object payload,
                                         EventWrapper rewriteEvent,
                                         String mediaType,
                                         Map attributes) {
        def message = new MessageWrapperImpl(payload,
                                             runtimeBridgeMuleSide,
                                             mediaType,
                                             attributes)
        assert rewriteEvent instanceof EventWrapperImpl
        rewriteEvent.createNewEventFromOld(message)
    }

    // TODO: Get rid of this, just use direct stuff in EventWrapperImpl since it now has the bridge
    @Deprecated
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

    def dispose() {
        runtimeBridgeMuleSide.dispose()
    }

    ClassLoader getAppClassloader() {
        runtimeBridgeMuleSide.getAppClassloader()
    }
}
