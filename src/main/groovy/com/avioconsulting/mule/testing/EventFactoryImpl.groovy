package com.avioconsulting.mule.testing

import org.mule.DefaultMuleEvent
import org.mule.DefaultMuleMessage
import org.mule.MessageExchangePattern
import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.api.MuleMessage

class EventFactoryImpl implements EventFactory {
    private final MuleContext muleContext

    EventFactoryImpl(MuleContext muleContext) {
        this.muleContext = muleContext
    }

    MuleEvent getMuleEvent(MuleMessage muleMessage,
                           String flowName,
                           MessageExchangePattern messageExchangePattern) {
        def flowConstruct = muleContext.registry.lookupFlowConstruct(flowName)
        assert flowConstruct: "Flow with name '${flowName}' was not found. Are you using the right flow name?"
        new DefaultMuleEvent(muleMessage,
                             messageExchangePattern,
                             flowConstruct)
    }

    @Override
    MuleEvent getMuleEvent(MuleMessage muleMessage,
                           MuleEvent rewriteEvent) {
        new DefaultMuleEvent(muleMessage,
                             rewriteEvent)
    }

    @Override
    MuleEvent getMuleEventWithPayload(Object payload,
                                      String flowName,
                                      MessageExchangePattern messageExchangePattern) {
        def message = new DefaultMuleMessage(payload,
                                             null,
                                             null,
                                             null,
                                             muleContext)
        getMuleEvent(message,
                     flowName,
                     messageExchangePattern)
    }

    @Override
    MuleEvent getMuleEventWithPayload(Object payload,
                                      MuleEvent rewriteEvent) {
        getMuleEventWithPayload(payload,
                                rewriteEvent,
                                null)
    }

    @Override
    MuleEvent getMuleEventWithPayload(Object payload,
                                      MuleEvent rewriteEvent,
                                      Map messageProps) {
        def message = new DefaultMuleMessage(payload,
                                             messageProps,
                                             null,
                                             null,
                                             muleContext)
        getMuleEvent(message, rewriteEvent)
    }
}
