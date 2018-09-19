package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.MessageWrapper

interface InvokerEventFactory {
    @Deprecated
    EventWrapper getMuleEvent(MessageWrapper muleMessage,
                              String flowName)

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
