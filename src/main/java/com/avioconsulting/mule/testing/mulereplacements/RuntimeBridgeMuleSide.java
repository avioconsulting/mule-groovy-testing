package com.avioconsulting.mule.testing.mulereplacements;

import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.event.EventContext;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.core.api.construct.Flow;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.streaming.StreamingManager;
import org.mule.runtime.core.api.streaming.bytes.CursorStreamProviderFactory;
import org.mule.runtime.core.internal.event.DefaultEventContext;

import java.io.InputStream;
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

    public Object getMuleStreamCursor(Object muleEvent,
                                      InputStream stream) {
        // TODO: See if we can hold on to these and speed lookup?
        Optional<StreamingManager> streamingManagerOptional = registry.lookupByName("_muleStreamingManager");
        if (!streamingManagerOptional.isPresent()) {
            throw new RuntimeException("Cannot get streaming manager!");
        }
        StreamingManager mgr = streamingManagerOptional.get();
        CursorStreamProviderFactory factory = mgr.forBytes().getDefaultCursorProviderFactory();
        if (!factory.accepts(stream)) {
            throw new RuntimeException("Factory won't accept it!");
        }
        return factory.of((CoreEvent) muleEvent, stream);
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
