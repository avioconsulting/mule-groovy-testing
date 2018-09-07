package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.MessageWrapper

interface EventFactory {
    // TODO: Try and avoid building messages everywhere too
    @Deprecated
    EventWrapper getMuleEvent(MessageWrapper muleMessage,
                              String flowName)

    // TODO: Try and avoid building messages everywhere too
    @Deprecated
    EventWrapper getMuleEvent(MessageWrapper muleMessage,
                              Object rewriteEvent)

    EventWrapper getMuleEventWithPayload(Object payload,
                                         String flowName)

    EventWrapper getMuleEventWithPayload(Object payload,
                                         String flowName,
                                         Map properties)

    EventWrapper getMuleEventWithPayload(Object payload,
                                         EventWrapper rewriteEvent)

    EventWrapper getMuleEventWithPayload(Object payload,
                                         EventWrapper rewriteEvent,
                                         Map properties)
}
