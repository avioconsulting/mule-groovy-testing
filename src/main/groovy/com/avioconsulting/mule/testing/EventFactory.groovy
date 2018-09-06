package com.avioconsulting.mule.testing

interface EventFactory {
    // TODO: Try and avoid building messages everywhere too
    @Deprecated
    Object getMuleEvent(Object muleMessage,
                        String flowName)

    // TODO: Try and avoid building messages everywhere too
    @Deprecated
    Object getMuleEvent(Object muleMessage,
                        Object rewriteEvent)

    Object getMuleEventWithPayload(Object payload,
                                   String flowName)

    Object getMuleEventWithPayload(Object payload,
                                   String flowName,
                                   Map properties)

    Object getMuleEventWithPayload(Object payload,
                                   Object rewriteEvent)

    Object getMuleEventWithPayload(Object payload,
                                   Object rewriteEvent,
                                   Map properties)
}
