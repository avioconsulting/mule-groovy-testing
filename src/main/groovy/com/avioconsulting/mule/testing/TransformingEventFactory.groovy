package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

interface TransformingEventFactory {
    EventWrapper getMuleEventWithPayload(Object payload,
                                         EventWrapper rewriteEvent)

    EventWrapper getMuleEventWithPayload(Object payload,
                                         EventWrapper rewriteEvent,
                                         Map attributes)

    EventWrapper getMuleEventWithPayload(Object payload,
                                         EventWrapper rewriteEvent,
                                         String mediaType,
                                         Map attributes)

    EventWrapper getStreamedMuleEventWithPayload(String payload,
                                                 EventWrapper rewriteEvent,
                                                 String mediaType,
                                                 Map attributes)
}
