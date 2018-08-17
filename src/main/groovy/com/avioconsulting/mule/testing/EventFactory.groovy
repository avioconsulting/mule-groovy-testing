package com.avioconsulting.mule.testing

import org.mule.MessageExchangePattern
import org.mule.api.MuleEvent
import org.mule.api.MuleMessage

interface EventFactory {
    MuleEvent getMuleEvent(MuleMessage muleMessage,
                           String flowName,
                           MessageExchangePattern messageExchangePattern)

    MuleEvent getMuleEvent(MuleMessage muleMessage,
                           MuleEvent rewriteEvent)
}
