package com.avioconsulting.mule.testing

import org.mule.runtime.api.message.Message
import org.mule.runtime.core.api.event.CoreEvent

interface EventFactory {
    // TODO: Try and avoid building messages everywhere too
    @Deprecated
    CoreEvent getMuleEvent(Message muleMessage,
                           String flowName,
                           Object messageExchangePattern)

    // TODO: Try and avoid building messages everywhere too
    @Deprecated
    CoreEvent getMuleEvent(Message muleMessage,
                           CoreEvent rewriteEvent)

    CoreEvent getMuleEventWithPayload(Object payload,
                                      String flowName,
                                      Object messageExchangePattern)

    CoreEvent getMuleEventWithPayload(Object payload,
                                      String flowName,
                                      Object messageExchangePattern,
                                      Map properties)

    CoreEvent getMuleEventWithPayload(Object payload,
                                      CoreEvent rewriteEvent)

    CoreEvent getMuleEventWithPayload(Object payload,
                                      CoreEvent rewriteEvent,
                                      Map properties)
}
