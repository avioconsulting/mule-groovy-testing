package com.avioconsulting.mule.testing.mulereplacements

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.mulereplacements.wrappers.*

class RuntimeBridgeTestSide implements EventFactory {
    private final Object runtimeBridgeMuleSide

    RuntimeBridgeTestSide(Object runtimeBridgeMuleSide) {
        this.runtimeBridgeMuleSide = runtimeBridgeMuleSide
    }

    FlowWrapper getFlow(String flowName) {
        def muleFlowOptional = runtimeBridgeMuleSide.lookupByName(flowName)
        assert muleFlowOptional.isPresent(): "Flow with name '${flowName}' was not found. Are you using the right flow name?"
        def muleFlow = muleFlowOptional.get()
        new FlowWrapperImpl(muleFlow.name, muleFlow)
    }

    @Override
    EventWrapper getMuleEvent(MessageWrapper message, String flowName) {
        assert message instanceof MessageWrapperImpl
        def muleEvent = runtimeBridgeMuleSide.getNewEvent(message.muleMessage,
                                                          flowName)
        new EventWrapperImpl(muleEvent)
    }

    @Override
    EventWrapper getMuleEvent(MessageWrapper muleMessage, Object rewriteEvent) {
        assert false: 'NIE'
    }

    @Override
    EventWrapper getMuleEventWithPayload(Object payload,
                                         String flowName) {
        def message = new MessageWrapperImpl(payload,
                                             runtimeBridgeMuleSide.messageBuilder)
        getMuleEvent(message,
                     flowName)
    }

    @Override
    EventWrapper getMuleEventWithPayload(Object payload, String flowName, Map properties) {
        assert false: 'NIE'
    }

    @Override
    EventWrapper getMuleEventWithPayload(Object payload, EventWrapper rewriteEvent) {
        assert false: 'NIE'
    }

    @Override
    EventWrapper getMuleEventWithPayload(Object payload, EventWrapper rewriteEvent, Map properties) {
        assert false: 'NIE'
    }
}
