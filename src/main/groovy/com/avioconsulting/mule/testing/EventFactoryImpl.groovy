package com.avioconsulting.mule.testing

class EventFactoryImpl implements EventFactory {
    private final Object muleContext

    EventFactoryImpl(Object muleContext) {
        this.muleContext = muleContext
    }

    Object getMuleEvent(Object muleMessage,
                        String flowName) {
        muleContext.getNewEvent(muleMessage, flowName)
    }

    @Override
    Object getMuleEvent(Object muleMessage,
                        Object rewriteEvent) {
        CoreEvent.builder(rewriteEvent)
                .message(muleMessage)
                .build()
    }

    @Override
    Object getMuleEventWithPayload(Object payload,
                                   String flowName) {
        def message = muleContext.messageBuilder
                .value(payload)
                .build()
        getMuleEvent(message,
                     flowName)
    }

    @Override
    Object getMuleEventWithPayload(Object payload,
                                   String flowName,
                                   Map properties) {
        def message = muleContext.messageBuilder
                .value(payload)
                .attributes(properties)
                .build()
        getMuleEvent(message,
                     flowName)
    }

    @Override
    Object getMuleEventWithPayload(Object payload,
                                   Object rewriteEvent) {
        getMuleEventWithPayload(payload,
                                rewriteEvent,
                                null)
    }

    @Override
    Object getMuleEventWithPayload(Object payload,
                                   Object rewriteEvent,
                                   Map messageProps) {
        def message = muleContext.messageBuilder
                .value(payload)
                .attributes(properties)
                .build()
        getMuleEvent(message,
                     rewriteEvent)
    }
}
