package com.avioconsulting.mule.testing

import org.mule.runtime.api.message.Message
import org.mule.runtime.core.api.MuleContext
import org.mule.runtime.core.api.event.CoreEvent

class EventFactoryImpl implements EventFactory {
    private final MuleContext muleContext

    EventFactoryImpl(MuleContext muleContext) {
        this.muleContext = muleContext
    }

    CoreEvent getMuleEvent(Message muleMessage,
                           String flowName,
                           Object messageExchangePattern) {
        def flowConstruct = muleContext.registry.lookupFlowConstruct(flowName)
        assert flowConstruct: "Flow with name '${flowName}' was not found. Are you using the right flow name?"
//        new DefaultMuleEvent(muleMessage,
//                             messageExchangePattern,
//                             flowConstruct)
    }

    @Override
    CoreEvent getMuleEvent(Message muleMessage,
                           CoreEvent rewriteEvent) {
//        new DefaultMuleEvent(muleMessage,
//                             rewriteEvent)
    }

    @Override
    CoreEvent getMuleEventWithPayload(Object payload,
                                      String flowName,
                                      Object messageExchangePattern) {
//        def message = new DefaultMuleMessage(payload,
//                                             null,
//                                             null,
//                                             null,
//                                             muleContext)
//        getMuleEvent(message,
//                     flowName,
//                     messageExchangePattern)
    }

    @Override
    CoreEvent getMuleEventWithPayload(Object payload,
                                      String flowName,
                                      Object messageExchangePattern,
                                      Map properties) {

//        def message = new DefaultMuleMessage(payload,
//                                             properties,
//                                             null,
//                                             null,
//                                             muleContext)
//        getMuleEvent(message,
//                     flowName,
//                     messageExchangePattern)
    }

    @Override
    CoreEvent getMuleEventWithPayload(Object payload,
                                      CoreEvent rewriteEvent) {
        getMuleEventWithPayload(payload,
                                rewriteEvent,
                                null)
    }

    @Override
    CoreEvent getMuleEventWithPayload(Object payload,
                                      CoreEvent rewriteEvent,
                                      Map messageProps) {
//        def message = new DefaultMuleMessage(payload,
//                                             messageProps,
//                                             null,
//                                             null,
//                                             muleContext)
//        getMuleEvent(message, rewriteEvent)
    }
}
