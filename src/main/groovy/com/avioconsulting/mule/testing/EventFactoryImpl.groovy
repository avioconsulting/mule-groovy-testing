package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.mulereplacements.ContainerContainer
import org.mule.runtime.api.component.location.ComponentLocation
import org.mule.runtime.api.message.Message
import org.mule.runtime.core.api.construct.Flow
import org.mule.runtime.core.api.construct.FlowConstruct
import org.mule.runtime.core.api.event.CoreEvent
import org.mule.runtime.core.internal.event.DefaultEventContext

class EventFactoryImpl implements EventFactory {
    private final ContainerContainer muleContext

    EventFactoryImpl(ContainerContainer muleContext) {
        this.muleContext = muleContext
    }

    CoreEvent getMuleEvent(Message muleMessage,
                           String flowName) {
        def flowOpt = muleContext.registry.lookupByName(flowName) as Optional<Flow>
        assert flowOpt.present: "Flow with name '${flowName}' was not found. Are you using the right flow name?"
        def flowConstruct = flowOpt.get() as FlowConstruct
        def context = new DefaultEventContext(flowConstruct,
                                              (ComponentLocation) null,
                                              null,
                                              Optional.empty())
        CoreEvent.builder(context)
                .message(muleMessage)
                .build()
    }

    @Override
    CoreEvent getMuleEvent(Message muleMessage,
                           CoreEvent rewriteEvent) {
        CoreEvent.builder(rewriteEvent)
                .message(muleMessage)
                .build()
    }

    @Override
    CoreEvent getMuleEventWithPayload(Object payload,
                                      String flowName) {
        def message = Message.builder()
                .value(payload)
                .build()
        getMuleEvent(message,
                     flowName)
    }

    @Override
    CoreEvent getMuleEventWithPayload(Object payload,
                                      String flowName,
                                      Map properties) {
        def message = Message.builder()
                .value(payload)
                .attributes(properties)
                .build()
        getMuleEvent(message,
                     flowName)
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
        def message = Message.builder()
                .value(payload)
                .attributes(properties)
                .build()
        getMuleEvent(message,
                     rewriteEvent)
    }
}
