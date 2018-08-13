package com.avioconsulting.mule.testing

import org.mule.DefaultMuleEvent
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
}
