package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

interface TransformingEventFactory {
    @Deprecated
    EventWrapper getMuleEventWithPayload(Object payload,
                                         String mediaType,
                                         EventWrapper rewriteEvent)

    @Deprecated
    EventWrapper getMuleEventWithAttributes(EventWrapper rewriteEvent,
                                            Map attributes)

    @Deprecated
    EventWrapper getMuleEventWithPayload(Object payload,
                                         EventWrapper rewriteEvent,
                                         String mediaType,
                                         Map attributes)

    @Deprecated
    EventWrapper getStreamedMuleEventWithPayload(String payload,
                                                 EventWrapper rewriteEvent,
                                                 String mediaType,
                                                 Map attributes)
}
