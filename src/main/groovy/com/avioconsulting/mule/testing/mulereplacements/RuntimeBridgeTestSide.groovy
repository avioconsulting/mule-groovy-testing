package com.avioconsulting.mule.testing.mulereplacements

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.FlowWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.FlowWrapperImpl
import com.avioconsulting.mule.testing.mulereplacements.wrappers.MessageWrapper

class RuntimeBridgeTestSide implements EventFactory {
    private final Object runtimeBridgeMuleSide

    RuntimeBridgeTestSide(Object runtimeBridgeMuleSide) {
        this.runtimeBridgeMuleSide = runtimeBridgeMuleSide
    }

    FlowWrapper getFlow(String flowName) {
        def muleFlowOptional = runtimeBridgeMuleSide.lookupByName(flowName)
        assert muleFlowOptional.isPresent() : "Flow with name '${flowName}' was not found. Are you using the right flow name?"
        def muleFlow = muleFlowOptional.get()
        new FlowWrapperImpl(muleFlow.name, muleFlow)
    }

    @Override
    EventWrapper getMuleEvent(MessageWrapper muleMessage, String flowName) {
        return null
    }

    @Override
    EventWrapper getMuleEvent(MessageWrapper muleMessage, Object rewriteEvent) {
        return null
    }

    @Override
    EventWrapper getMuleEventWithPayload(Object payload, String flowName) {
        return null
    }

    @Override
    EventWrapper getMuleEventWithPayload(Object payload, String flowName, Map properties) {
        return null
    }

    @Override
    EventWrapper getMuleEventWithPayload(Object payload, EventWrapper rewriteEvent) {
        return null
    }

    @Override
    EventWrapper getMuleEventWithPayload(Object payload, EventWrapper rewriteEvent, Map properties) {
        return null
    }
}
