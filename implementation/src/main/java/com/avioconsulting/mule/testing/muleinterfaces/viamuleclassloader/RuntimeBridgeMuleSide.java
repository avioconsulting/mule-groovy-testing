package com.avioconsulting.mule.testing.muleinterfaces.viamuleclassloader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.component.TypedComponentIdentifier;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.api.event.EventContext;
import org.mule.runtime.api.exception.ErrorTypeRepository;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.config.api.LazyComponentInitializer;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.construct.Flow;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.streaming.StreamingManager;
import org.mule.runtime.core.api.streaming.bytes.CursorStreamProviderFactory;
import org.mule.runtime.core.internal.event.DefaultEventContext;
import org.mule.runtime.core.internal.interception.DefaultInterceptionEvent;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RuntimeBridgeMuleSide {
    // ideally this code would be shared with FlowWrapper but they are on different
    // sides of the classloader divide
    private static final QName COMPONENT_LOCATION = new QName("mule",
                                                              "COMPONENT_LOCATION");
    private static final Logger logger = LogManager.getLogger(RuntimeBridgeMuleSide.class);
    private final Registry registry;
    private final List<CompletableFuture<Void>> streamCompletionCallbacks = new ArrayList<>();
    private GroovyTestingBatchNotifyListener batchNotifyListener;
    private final CursorStreamProviderFactory cursorStreamProviderFactory;

    RuntimeBridgeMuleSide(Registry registry) {
        this.registry = registry;
        Optional<StreamingManager> streamingManagerOptional = registry.lookupByType(StreamingManager.class);
        if (!streamingManagerOptional.isPresent()) {
            throw new RuntimeException("Cannot get streaming manager!");
        }
        StreamingManager streamingManager = streamingManagerOptional.get();
        cursorStreamProviderFactory = streamingManager.forBytes().getDefaultCursorProviderFactory();
    }

    public Object lookupByName(String flowName) {
        LazyComponentInitializer init = this.registry.lookupByType(LazyComponentInitializer.class).get();
        ConfigurationComponentLocator locator = this.registry.lookupByType(ConfigurationComponentLocator.class).get();
        // TODO: This sort of works, we just need to 1) identify the right flow, 2) only call this if we can't get the flow from the registry, and 3) figure out the exception we get as is
        for (ComponentLocation loc : locator.findAllLocations()) {
            if (loc.getComponentIdentifier().getType().equals(TypedComponentIdentifier.ComponentType.FLOW)) {
                logger.info("Flow '{}' has not been lazily loaded yet, forcing load",
                            flowName);
                init.initializeComponents(new LazyComponentInitializer.ComponentLocationFilter() {
                    @Override
                    public boolean accept(ComponentLocation componentLocation) {
                        return componentLocation.equals(loc);
                    }
                });
                logger.info("Flow '{}' lazy load complete",
                            flowName);
            }
        }
        return this.registry.lookupByName(flowName);
    }

    public Object getMessageBuilder() {
        return Message.builder();
    }

    public Object getMediaType(String mediaType) {
        return MediaType.parse(mediaType);
    }

    public Object getDataType(Object value,
                              String mediaType) {
        return DataType.builder()
                .fromObject(value)
                .mediaType(mediaType)
                .build();
    }

    public Object getEventFromOldEvent(Object muleMessage,
                                       Object oldEvent) {
        return CoreEvent.builder((CoreEvent) oldEvent)
                .message((Message) muleMessage)
                .build();
    }

    public Object getEventFromOldEvent(Object muleMessage,
                                       Object oldEvent,
                                       String variableName,
                                       Object variableValue) {
        return CoreEvent.builder((CoreEvent) oldEvent)
                .addVariable(variableName, variableValue)
                .message((Message) muleMessage)
                .build();
    }

    public Object getEventFromOldEvent(Object muleMessage,
                                       Object oldEvent,
                                       String variableName,
                                       Object variableValue,
                                       String mediaType) {
        DataType dataType = (DataType) getMediaType(mediaType);
        return CoreEvent.builder((CoreEvent) oldEvent)
                .addVariable(variableName, variableValue, dataType)
                .message((Message) muleMessage)
                .build();
    }

    public Object getMuleStreamCursor(Object muleEvent,
                                      InputStream stream) {
        if (!cursorStreamProviderFactory.accepts(stream)) {
            throw new RuntimeException("Factory won't accept our stream!");
        }
        return cursorStreamProviderFactory.of(getCoreEvent(muleEvent),
                                              stream);
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
                              Object flowObj) {
        Flow flow = (Flow) flowObj;
        CompletableFuture<Void> externalCompletionCallback = new CompletableFuture<>();
        this.streamCompletionCallbacks.add(externalCompletionCallback);
        // without the completion callback, any streams in the payload will be closed when the flow under test 'completes'
        // which will make it impossible to get at the payload
        ComponentLocation location = (ComponentLocation) flow.getAnnotation(COMPONENT_LOCATION);
        EventContext context = new DefaultEventContext(flow,
                                                       location,
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

    public ErrorTypeRepository getErrorTypeRepository() {
        Optional<ErrorTypeRepository> optional = this.registry.lookupByType(ErrorTypeRepository.class);
        if (!optional.isPresent()) {
            throw new RuntimeException("Unable to get error type repository!");
        }
        return optional.get();

    }

    public ClassLoader getRuntimeClassLoader() {
        return CoreEvent.class.getClassLoader();
    }

    public <T> TypedValue<T> getSoapTypedValue(T soapOutputPayload) {
        return new TypedValue<T>(soapOutputPayload,
                                 DataType.XML_STRING);
    }

    public GroovyTestingBatchNotifyListener getBatchNotifyListener() {
        return batchNotifyListener;
    }

    public void setBatchNotifyListener(GroovyTestingBatchNotifyListener batchNotifyListener) {
        this.batchNotifyListener = batchNotifyListener;
    }
}
