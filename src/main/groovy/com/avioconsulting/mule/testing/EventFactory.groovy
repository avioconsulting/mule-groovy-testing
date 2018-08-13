package com.avioconsulting.mule.testing

import org.mule.MessageExchangePattern
import org.mule.api.MuleEvent
import org.mule.api.MuleMessage

interface EventFactory {
    MuleEvent getMuleEvent(MuleMessage muleMessage,
                           MessageExchangePattern messageExchangePattern)
}
