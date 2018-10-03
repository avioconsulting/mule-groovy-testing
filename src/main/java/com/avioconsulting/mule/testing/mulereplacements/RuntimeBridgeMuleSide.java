package com.avioconsulting.mule.testing.mulereplacements;

import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.event.EventContext;
import org.mule.runtime.api.exception.ErrorTypeRepository;
import org.mule.runtime.api.message.ErrorType;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.construct.Flow;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.streaming.StreamingManager;
import org.mule.runtime.core.api.streaming.bytes.CursorStreamProviderFactory;
import org.mule.runtime.core.internal.event.DefaultEventContext;
import org.mule.runtime.core.internal.interception.DefaultInterceptionEvent;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RuntimeBridgeMuleSide {
    private final Registry registry;

    private final List<CompletableFuture<Void>> streamCompletionCallbacks = new ArrayList<>();

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
        return factory.of(getCoreEvent(muleEvent), stream);
    }

    private static CoreEvent getCoreEvent(Object muleEvent) {
        if (muleEvent instanceof CoreEvent) {
            return (CoreEvent) muleEvent;
        }
        if (muleEvent instanceof DefaultInterceptionEvent) {
            // We have to find a way to bridge these 2 so that we can create streams from mocks
            return new CoreInterceptionEvent((DefaultInterceptionEvent) muleEvent);
        }
        throw new RuntimeException("Do not know how to handle " + muleEvent.getClass().getName());
    }

    // called from RuntimeBridgeTestSide
    public void dispose() {
        // just to clean up after ourselves
        for (CompletableFuture<Void> callback : streamCompletionCallbacks) {
            callback.complete(null);
        }
        streamCompletionCallbacks.clear();
    }

    public Object getNewEvent(Object muleMessage,
                              String flowName) {
        Optional<Flow> flowOpt = (Optional<Flow>) lookupByName(flowName);
        if (!flowOpt.isPresent()) {
            throw new RuntimeException("Flow not present! " + flowName);
        }
        Flow flow = flowOpt.get();
        CompletableFuture<Void> externalCompletionCallback = new CompletableFuture<>();
        this.streamCompletionCallbacks.add(externalCompletionCallback);
        // without the completion callback, any streams in the payload will be closed when the flow under test 'completes'
        // which will make it impossible to get at the payload
        EventContext context = new DefaultEventContext(flow,
                                                       (ComponentLocation) null,
                                                       null,
                                                       Optional.of(externalCompletionCallback));
        return CoreEvent.builder(context)
                .message((Message) muleMessage)
                .build();
    }

    private MuleContext getMuleContext() {
        Optional<MuleContext> lookedUp = registry.lookupByType(MuleContext.class);
        if (!lookedUp.isPresent()) {
            throw new RuntimeException("Only way we know how to get the app's classloader currently is through its MuleContext");
        }
        return lookedUp.get();
    }

    public ClassLoader getAppClassloader() {
        return getMuleContext().getExecutionClassLoader();
    }

    public Object lookupErrorType(ComponentIdentifier id,
                                  String errorType) {
        ErrorTypeRepository errorTypeRepo = getMuleContext().getErrorTypeRepository();
        ComponentIdentifier errorTypeId = ComponentIdentifier.builder()
                .namespace(id.getNamespace().toUpperCase())
                .name(errorType.toUpperCase())
                .build();
        Optional<ErrorType> value = errorTypeRepo.getErrorType(errorTypeId);
        if (!value.isPresent()) {
            throw new RuntimeException("Unable to lookup error type! "+errorTypeId.toString());
        }
        return value.get();
    }
}
