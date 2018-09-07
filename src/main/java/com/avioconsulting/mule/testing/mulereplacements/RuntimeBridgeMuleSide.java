package com.avioconsulting.mule.testing.mulereplacements;

import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.event.EventContext;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.construct.Flow;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.internal.event.DefaultEventContext;

import java.util.Optional;

public class RuntimeBridgeMuleSide {
    private final Registry registry;

    public RuntimeBridgeMuleSide(Registry registry) {
        this.registry = registry;
    }

    public Object lookupByName(String flowName) {
        Optional<Object> optional = this.registry.lookupByName(flowName);
        if (!optional.isPresent()) {
            throw new RuntimeException("name " + flowName + " not found!");
        }
        return optional.get();
    }

    public Object getMessageBuilder() {
        return Message.builder();
    }

    public Object getNewEvent(Object muleMessage,
                              String flowName) {
        Flow flow = (Flow) lookupByName(flowName);
        EventContext context = new DefaultEventContext(flow,
                                                       (ComponentLocation) null,
                                                       null,
                                                       Optional.empty());
        return CoreEvent.builder(context)
                .message((Message) muleMessage)
                .build();
    }
}
