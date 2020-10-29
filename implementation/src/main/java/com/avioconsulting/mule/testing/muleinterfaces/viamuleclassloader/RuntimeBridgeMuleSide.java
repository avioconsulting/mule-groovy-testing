package com.avioconsulting.mule.testing.muleinterfaces.viamuleclassloader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.component.TypedComponentIdentifier;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.api.dsl.DslResolvingContext;
import org.mule.runtime.api.event.EventContext;
import org.mule.runtime.api.exception.ErrorTypeRepository;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.meta.model.ExtensionModel;
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
import org.mule.runtime.dsl.api.component.config.DefaultComponentLocation;
import org.mule.runtime.module.extension.internal.capability.xml.schema.DefaultExtensionSchemaGenerator;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.util.*;
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
    private final ConfigurationComponentLocator locator;

    RuntimeBridgeMuleSide(Registry registry) {
        this.registry = registry;
        Optional<StreamingManager> streamingManagerOptional = registry.lookupByType(StreamingManager.class);
        if (!streamingManagerOptional.isPresent()) {
            throw new RuntimeException("Cannot get streaming manager!");
        }
        StreamingManager streamingManager = streamingManagerOptional.get();
        cursorStreamProviderFactory = streamingManager.forBytes().getDefaultCursorProviderFactory();
        locator = registry.lookupByType(ConfigurationComponentLocator.class).get();
    }

    public ConfigurationComponentLocator getLocator() {
        return locator;
    }

    public Optional<Object> lookupByName(String name) {
        return this.registry.lookupByName(name);
    }

    public Object lookupByName(String flowName,
                               boolean lazyInitEnabled) {
        Optional<Object> flow = this.registry.lookupByName(flowName);
        // if we don't have lazy init on, we won't proceed further anyways
        if (flow.isPresent() || !lazyInitEnabled) {
            return flow;
        }
        // see lazyInit property (currently in BaseMuleGroovyTrait) for why we have to lazy load
        // when we do lazy load, no flows will be started or initialized by default. so every time we want
        // to run a new one, we need to initialize it
        LazyComponentInitializer init = this.registry.lookupByType(LazyComponentInitializer.class).get();
        init.initializeComponents(componentLocation -> {
            if (componentLocation.getComponentIdentifier().getType().equals(TypedComponentIdentifier.ComponentType.FLOW)) {
                assert componentLocation instanceof DefaultComponentLocation;
                Optional<String> theName = ((DefaultComponentLocation) componentLocation).getName();
                if (theName.isPresent() && theName.get().equals(flowName)) {
                    logger.info("Flow '{}' has not been lazily loaded yet, forcing load",
                            flowName);
                    return true;
                }
            }
            return false;
        });
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

    public Map<String, String> getExtensionSchemas() {
        Set<ExtensionModel> extensions = getMuleContext().getExtensionManager().getExtensions();
        DslResolvingContext resolvingContext = DslResolvingContext.getDefault(extensions);
        DefaultExtensionSchemaGenerator schemaGenerator = new DefaultExtensionSchemaGenerator();
        Map<String, String> map = new HashMap<>();
        for (ExtensionModel model : extensions) {
            String schema = schemaGenerator.generate(model, resolvingContext);
            String filename = model.getXmlDslModel().getXsdFileName();
            map.put(filename, schema);
        }
        return map;
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
