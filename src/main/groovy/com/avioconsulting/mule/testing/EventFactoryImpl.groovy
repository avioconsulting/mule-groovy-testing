package com.avioconsulting.mule.testing

import org.mule.DefaultMuleEvent
import org.mule.MessageExchangePattern
import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.api.MuleMessage
import org.mule.api.construct.FlowConstruct

class EventFactoryImpl implements EventFactory {
    private final FlowConstruct flowConstruct

    EventFactoryImpl(MuleContext muleContext,
                     String flowName) {
        this.flowConstruct = muleContext.registry.lookupFlowConstruct(flowName)
    }

    MuleEvent getMuleEvent(MuleMessage muleMessage,
                           MessageExchangePattern messageExchangePattern) {
        new DefaultMuleEvent(muleMessage,
                             messageExchangePattern,
                             flowConstruct)
    }
}
