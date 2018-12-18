package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper

interface InvokerEventFactory {
    EventWrapper getMuleEventWithPayload(Object payload,
                                         String flowName)

    EventWrapper getMuleEventWithPayload(Object payload,
                                         String flowName,
                                         String mediaType)

    EventWrapper getMuleEventWithPayload(Object payload,
                                         String flowName,
                                         Map attributes)
}
