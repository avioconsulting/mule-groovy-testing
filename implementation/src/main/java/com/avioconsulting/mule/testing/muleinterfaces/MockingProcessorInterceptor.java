package com.avioconsulting.mule.testing.muleinterfaces;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.exception.ErrorTypeRepository;
import org.mule.runtime.api.interception.InterceptionAction;
import org.mule.runtime.api.interception.InterceptionEvent;
import org.mule.runtime.api.interception.ProcessorInterceptor;
import org.mule.runtime.api.interception.ProcessorParameterValue;
import org.mule.runtime.api.message.ErrorType;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.internal.exception.MessagingException;
import org.mule.runtime.core.internal.interception.DefaultInterceptionEvent;
import org.mule.runtime.core.internal.processor.TryScope;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mule.runtime.core.api.util.ClassUtils.withContextClassLoader;

public class MockingProcessorInterceptor implements ProcessorInterceptor {
    private static final String PARAMETER_CONNECTOR_NAME = "doc:name";
    private static final String PARAMETER_MODULE_NAME = "moduleName";
    private static final QName SOURCE_ELEMENT = new QName("http://www.mulesoft.org/schema/mule/documentation",
                                                          "sourceElement");
    private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    private static final DocumentBuilder builder;
    private static final Logger logger = LogManager.getLogger(MockingProcessorInterceptor.class);

    static {
        try {
            builder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private final Method isMockEnabledMethod;
    private final Method doMockInvocationMethod;
    private final Method getErrorTypeRepositoryMethod;
    private final Object mockingConfiguration;
    private final ClassLoader appClassLoader;
    // The assumption here is that our call to a module/Exchange wrapped API, since it's just a chain container
    // will be on the same thread, so we can "pass" the name of the connector down to the next invocation
    // of this interceptor w/ ThreadLocal. See usages in this class for more info
    private static ThreadLocal<String> moduleConnectorName = new ThreadLocal<>();

    MockingProcessorInterceptor(Object mockingConfiguration,
                                ClassLoader appClassLoader) {
        this.mockingConfiguration = mockingConfiguration;
        this.appClassLoader = appClassLoader;
        try {
            Class<?> mockingConfigClass = mockingConfiguration.getClass();
            this.isMockEnabledMethod = mockingConfigClass.getDeclaredMethod("isMocked",
                                                                            String.class);
            this.doMockInvocationMethod = mockingConfigClass.getDeclaredMethod("executeMock",
                                                                               String.class,
                                                                               Object.class,
                                                                               Object.class,
                                                                               Object.class);
            this.getErrorTypeRepositoryMethod = mockingConfigClass.getDeclaredMethod("getErrorTypeRepository");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isMockEnabled(String connectorName) {
        try {
            return (boolean) isMockEnabledMethod.invoke(mockingConfiguration,
                                                        connectorName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void executeMock(String connectorName,
                             ComponentLocation location,
                             InterceptionEvent event,
                             Map<String, ProcessorParameterValue> parameters) throws InvocationTargetException, IllegalAccessException {
        doMockInvocationMethod.invoke(mockingConfiguration,
                                      connectorName,
                                      location,
                                      event,
                                      parameters);
    }

    private ErrorTypeRepository getErrorTypeRepository() {
        try {
            return (ErrorTypeRepository) this.getErrorTypeRepositoryMethod.invoke(mockingConfiguration);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Processor getProcessor(InterceptionAction action) {
        // action is of type org.mule.runtime.core.internal.processor.interceptor.ReactiveInterceptionAction
        // ReactiveInterceptionAction.processor is a private field. Only way known to get ahold of this
        try {
            Field processorField = action.getClass().getDeclaredField("processor");
            processorField.setAccessible(true);
            return (Processor) processorField.get(action);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<InterceptionEvent> doProceed(InterceptionAction action) {
        // for some reason, org.mule.runtime.core.internal.processor.interceptor.ReactiveAroundInterceptorAdapter
        // in its doAround method changes the thread context classloader to the one that loaded the interceptor
        // class. The problem with that is it causes problems with non-mocked connectors that
        // have paged/streaming operations because they expect the app (or region, not sure about this) classloader
        // to be current context classloader when they actually execute. the problem manifests itself one way
        // in that loggers will 'forget' the log4j (impl and test) config of the app during this execution
        // and instead rely on the engine's .conf directory config, which effectively prevents debug logging
        // since that config file is not changeable by apps using this framework

        // One option would be to load this interceptor/interceptor factory with the app's classloader. the problem
        // there is it's in this framework (not in the app)

        // therefore we effectively reverse what ReactiveAroundInterceptorAdapter does here during our execution

        // we only need to do this when we are NOT mocking and are running the real connector
        // this will run the 'real' connector inside the app's classloader
        return withContextClassLoader(this.appClassLoader,
                                      action::proceed);
    }

    @Override
    public CompletableFuture<InterceptionEvent> around(ComponentLocation location,
                                                       Map<String, ProcessorParameterValue> parameters,
                                                       InterceptionEvent event,
                                                       InterceptionAction action) {
        String connectorName = getModuleCallName(parameters,
                                                 action);
        // when we are mocking an HTTP call inside an API module, the first call we see here will be the module
        // (e.g. yourapi:some_operation)
        // then inside that module is a "processor chain" / operation is the actual call
        if (connectorName != null) {
            logger.info("Preserving module name of '{}' for next call (a connector that might be mocked) since it may not have a name on it",
                        connectorName);
            moduleConnectorName.set(connectorName);
            // we can't access anything on the module call but the next call inside should give us info
            return doProceed(action);
        }
        if (!parameters.containsKey(PARAMETER_CONNECTOR_NAME) && moduleConnectorName.get() == null) {
            return doProceed(action);
        } else if (moduleConnectorName.get() != null) {
            // in this case, we found the actual HTTP connector name using the module we already have
            connectorName = moduleConnectorName.get();
            logger.info("Obtained the connector name of '{}' using previous module execution",
                        connectorName);
            // don't want this to continue past this execution
            moduleConnectorName.remove();
        } else {
            // the most normal case
            connectorName = parameters.get(PARAMETER_CONNECTOR_NAME).providedValue();
        }

        if (!isMockEnabled(connectorName)) {
            return doProceed(action);
        }

        try {
            executeMock(connectorName,
                        location,
                        event,
                        parameters);
            return action.skip();
        } catch (InvocationTargetException cause) {
            // need to unwrap our reflection based Mule exceptions
            Throwable actualException = cause.getTargetException();
            switch (actualException.getClass().getName()) {
                case "com.avioconsulting.mule.testing.muleinterfaces.wrappers.ModuleExceptionWrapper":
                    // not using action.fail(Throwable) because if you supply the raw exception, flow error handlers will not
                    // receive the error type. action.fail(ErrorType) would then prevent other useful exception
                    // details from being passed along. We have our own implementation that sets the error type
                    // and exception details properly, just like the real ModuleExceptionHandler would
                    return fail(event,
                                action,
                                actualException);
                case "com.avioconsulting.mule.testing.muleinterfaces.wrappers.CustomErrorWrapperException":
                    // using this for same reason as above, except our mocking code already built out the
                    // error codes for us
                    return failWithCustomerErrorWrapper(event,
                                                        action,
                                                        actualException);
                default:
                    return action.fail(actualException);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Should not have had a reflection problem but did",
                                       e);
        }
    }

    private String getModuleCallName(Map<String, ProcessorParameterValue> parameters,
                                     InterceptionAction action) {
        Processor processor = getProcessor(action);
        try {
            // if we're calling an API using Exchange "derived" XML SDK calls, this will be a processor chain
            // but we can't get the name of the connector when the actual mock w/ params/vars runs.
            // so we map the connector name to the processor chain here, then when this runs again with the processor
            // chain, we know the name of the 'mock'
            if (processor instanceof TryScope) {
                return getNameFromTryScope((TryScope) processor);
            }
            if (!parameters.containsKey(PARAMETER_MODULE_NAME)) {
                return null;
            }
            if (!(processor instanceof Component)) {
                return null;
            }
            String sourceElement = (String) ((Component) processor).getAnnotation(SOURCE_ELEMENT);
            if (sourceElement == null) {
                return null;
            }
            Document doc = builder.parse(new ByteArrayInputStream(sourceElement.getBytes()));
            return doc.getDocumentElement().getAttribute(PARAMETER_CONNECTOR_NAME);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getNameFromTryScope(TryScope processor) throws SAXException, IOException {
        // the try scope works differently. it can interfere with the usual module process
        String sourceElement = (String) processor.getAnnotation(SOURCE_ELEMENT);
        Document doc = builder.parse(new ByteArrayInputStream(sourceElement.getBytes()));
        Element tryElement = doc.getDocumentElement();
        NodeList childNodes = tryElement.getChildNodes();
        // <try>
        //  <module-hello:do-stuff-get doc:name="the name of our connector" inputParam="#[payload]"></module-hello:do-stuff-get>
        // </try>
        List<Element> nonErrorHandlerChildElements = new ArrayList<>();
        // get elements only (no text, etc.)
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node instanceof Element && !node.getNodeName().equals("error-handler")) {
                nonErrorHandlerChildElements.add((Element) node);
            }
        }
        // we should only need to do this for cases where the connector is the only item in the try scope
        // other cases work without doing this (see ApiMockTest)
        if (nonErrorHandlerChildElements.size() != 1) {
            return null;
        }
        return nonErrorHandlerChildElements.get(0).getAttribute(PARAMETER_CONNECTOR_NAME);
    }

    private String getNamespace(Throwable moduleExceptionWrapper) {
        try {
            // ModuleExceptionWrapper is Groovy from the test side, therefore reflection to avoid
            // classloader issues
            Method getNamespaceMethod = moduleExceptionWrapper.getClass().getDeclaredMethod("getNamespace");
            return (String) getNamespaceMethod.invoke(moduleExceptionWrapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<InterceptionEvent> fail(InterceptionEvent event,
                                                      InterceptionAction action,
                                                      Throwable moduleExceptionWrapper) {
        ModuleException moduleException = (ModuleException) moduleExceptionWrapper.getCause();
        ErrorTypeDefinition errorTypeDefinition = moduleException.getType();
        // see ModuleExceptionWrapper for why we get the namespace for the error this way
        String namespace = getNamespace(moduleExceptionWrapper);
        String errorTypeCode = errorTypeDefinition.getType();
        return fail(event,
                    action,
                    moduleException,
                    namespace,
                    errorTypeCode);
    }

    private CompletableFuture<InterceptionEvent> failWithCustomerErrorWrapper(InterceptionEvent event,
                                                                              InterceptionAction action,
                                                                              Throwable customErrorWrapperException) {
        try {
            // CustomErrorWrapperException is groovy,  therefore reflection
            Class<? extends Throwable> customErrorWrapperExceptionClass = customErrorWrapperException.getClass();
            Method namespaceMethod = customErrorWrapperExceptionClass.getDeclaredMethod("getNamespace");
            String namespace = (String) namespaceMethod.invoke(customErrorWrapperException);
            Method errorTypeMethod = customErrorWrapperExceptionClass.getDeclaredMethod("getErrorCode");
            String errorTypeCode = (String) errorTypeMethod.invoke(customErrorWrapperException);
            return fail(event,
                        action,
                        customErrorWrapperException.getCause(),
                        namespace,
                        errorTypeCode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<InterceptionEvent> fail(InterceptionEvent event,
                                                      InterceptionAction action,
                                                      Throwable exception,
                                                      String namespace,
                                                      String errorTypeCode) {
        ComponentIdentifier errorComponentIdentifier = ComponentIdentifier.builder()
                .namespace(namespace)
                .name(errorTypeCode)
                .build();
        ErrorTypeRepository repository = getErrorTypeRepository();
        Optional<ErrorType> errorType = repository.lookupErrorType(errorComponentIdentifier);
        if (!errorType.isPresent()) {
            throw new RuntimeException("Unable to lookup error type! " + errorComponentIdentifier);
        }
        DefaultInterceptionEvent eventImpl = (DefaultInterceptionEvent) event;
        eventImpl.setError(errorType.get(),
                           exception);
        Processor processor = getProcessor(action);
        if (!(processor instanceof Component)) {
            throw new RuntimeException("Expected processor to be an instance of Component but was: " + processor.getClass().getName());
        }
        Component component = (Component) processor;
        CompletableFuture<InterceptionEvent> completableFuture = new CompletableFuture<>();
        completableFuture.completeExceptionally(new MessagingException(eventImpl.resolve(),
                                                                       exception,
                                                                       component));
        return completableFuture;
    }
}
