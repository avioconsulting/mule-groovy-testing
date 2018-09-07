package com.avioconsulting.mule.testing.mulereplacements

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.MessageWrapper

class RuntimeBridgeTestSide implements EventFactory {
    private final Object runtimeBridgeMuleSide

    RuntimeBridgeTestSide(Object runtimeBridgeMuleSide) {
        this.runtimeBridgeMuleSide = runtimeBridgeMuleSide
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
