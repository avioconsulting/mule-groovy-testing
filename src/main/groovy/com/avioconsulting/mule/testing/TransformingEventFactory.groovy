package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

interface TransformingEventFactory {
    EventWrapper getMuleEventWithPayload(Object payload,
                                         EventWrapper rewriteEvent)

    EventWrapper getMuleEventWithPayload(Object payload,
                                         EventWrapper rewriteEvent,
                                         Map properties)
}
