package com.avioconsulting.mule.testing

import org.mule.MessageExchangePattern
import org.mule.api.MuleEvent
import org.mule.api.MuleMessage

interface EventFactory {
    // TODO: Try and avoid building messages everywhere too
    @Deprecated
    MuleEvent getMuleEvent(MuleMessage muleMessage,
                           String flowName,
                           MessageExchangePattern messageExchangePattern)

    // TODO: Try and avoid building messages everywhere too
    @Deprecated
    MuleEvent getMuleEvent(MuleMessage muleMessage,
                           MuleEvent rewriteEvent)

    MuleEvent getMuleEventWithPayload(Object payload,
                                      MuleEvent rewriteEvent)

    MuleEvent getMuleEventWithPayload(Object payload,
                                      MuleEvent rewriteEvent,
                                      Map properties)
}
