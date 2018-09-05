package com.avioconsulting.mule.testing

import org.mule.runtime.api.message.Message
import org.mule.runtime.core.api.event.CoreEvent

interface EventFactory {
    // TODO: Try and avoid building messages everywhere too
    @Deprecated
    CoreEvent getMuleEvent(Message muleMessage,
                           String flowName)

    // TODO: Try and avoid building messages everywhere too
    @Deprecated
    CoreEvent getMuleEvent(Message muleMessage,
                           CoreEvent rewriteEvent)

    CoreEvent getMuleEventWithPayload(Object payload,
                                      String flowName)

    CoreEvent getMuleEventWithPayload(Object payload,
                                      String flowName,
                                      Map properties)

    CoreEvent getMuleEventWithPayload(Object payload,
                                      CoreEvent rewriteEvent)

    CoreEvent getMuleEventWithPayload(Object payload,
                                      CoreEvent rewriteEvent,
                                      Map properties)
}
