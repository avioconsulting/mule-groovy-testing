package com.avioconsulting.mule.testing.mulereplacements;

import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.event.EventContext;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.MediaType;
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
        return this.registry.lookupByName(flowName);
    }

    public Object getMessageBuilder() {
        return Message.builder();
    }

    public Object getMediaType(String mediaType) {
        return MediaType.parse(mediaType);
    }

    public Object getEventFromOldEvent(Object muleMessage,
                                       Object oldEvent) {
        return CoreEvent.builder((CoreEvent) oldEvent)
                .message((Message) muleMessage)
                .build();
    }

    public Object getNewEvent(Object muleMessage,
                              String flowName) {
        Optional<Flow> flowOpt = (Optional<Flow>) lookupByName(flowName);
        if (!flowOpt.isPresent()) {
            throw new RuntimeException("Flow not present! " + flowName);
        }
        Flow flow = flowOpt.get();
        EventContext context = new DefaultEventContext(flow,
                                                       (ComponentLocation) null,
                                                       null,
                                                       Optional.empty());
        return CoreEvent.builder(context)
                .message((Message) muleMessage)
                .build();
    }
}
