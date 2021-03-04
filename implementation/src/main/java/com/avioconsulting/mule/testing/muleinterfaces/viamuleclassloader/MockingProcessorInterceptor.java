package com.avioconsulting.mule.testing.muleinterfaces.viamuleclassloader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.api.exception.ErrorTypeRepository;
import org.mule.runtime.api.interception.InterceptionAction;
import org.mule.runtime.api.interception.InterceptionEvent;
import org.mule.runtime.api.interception.ProcessorInterceptor;
import org.mule.runtime.api.interception.ProcessorParameterValue;
import org.mule.runtime.api.message.ErrorType;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.internal.exception.MessagingException;
import org.mule.runtime.core.internal.interception.DefaultInterceptionEvent;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.internal.processor.ModuleOperationMessageProcessor;

import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MockingProcessorInterceptor implements ProcessorInterceptor {
    private static final String PARAMETER_CONNECTOR_NAME = "doc:name";
    private static final String PARAMETER_MODULE_NAME = "moduleName";
    private static final QName SOURCE_ELEMENT = new QName("http://www.mulesoft.org/schema/mule/documentation",
            "sourceElement");
    private static final QName DOC_NAME = new QName("http://www.mulesoft.org/schema/mule/documentation",
            "name");
    private static final Logger logger = LogManager.getLogger(MockingProcessorInterceptor.class);
    private final Method isMockEnabledMethod;
    private final Method doMockInvocationMethod;
    private final Method getErrorTypeRepositoryMethod;
    private final Object mockingConfiguration;
    private final Method locatorMethod;
    // The assumption here is that our call to a module/Exchange wrapped API, since it's just a chain container
    // will be on the same thread, so we can "pass" the name of the connector down to the next invocation
    // of this interceptor w/ ThreadLocal. See usages in this class for more info
    private static ThreadLocal<String> moduleConnectorName = new ThreadLocal<>();

    MockingProcessorInterceptor(Object mockingConfiguration) {
        this.mockingConfiguration = mockingConfiguration;
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
            this.locatorMethod = mockingConfigClass.getDeclaredMethod("getLocator");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ConfigurationComponentLocator getLocator() {
        try {
            return (ConfigurationComponentLocator) this.locatorMethod.invoke(mockingConfiguration);
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
            return action.proceed();
        }
        if (!parameters.containsKey(PARAMETER_CONNECTOR_NAME) && moduleConnectorName.get() == null) {
            return action.proceed();
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
            return action.proceed();
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
        if (processor instanceof ModuleOperationMessageProcessor) {
            Object annotation = ((Component) processor).getAnnotation(DOC_NAME);
            if (annotation == null) {
                return null;
            }
            return (String) annotation;
        }
        return null;
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

    // not using action.fail(Throwable) because if you supply the raw exception, flow error handlers will not
    // receive the error type. action.fail(ErrorType) would then prevent other useful exception
    // details from being passed along. We have our own implementation that sets the error type
    // and exception details properly, just like the real ModuleExceptionHandler would
    private CompletableFuture<InterceptionEvent> fail(InterceptionEvent event,
                                                      InterceptionAction action,
                                                      Throwable exception,
                                                      String namespace,
                                                      String errorTypeCode) {
        // we need errorType to set the error on the event and we need the identifier to do that
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
        // if we're going to set an error, we need to set the non-error payload/message (meaning the request)
        // before our error is thrown BEFORE setting the error. if we don't, the payload will be lost
        // with the interceptedInput.
        // this should "synchronize" the output interception event with the input before we add our error in
        eventImpl.message(eventImpl.getMessage());
        Map<String, Object> variables = new HashMap<>(eventImpl.getVariables());
        eventImpl.variables(variables);
        eventImpl.session(eventImpl.getSession());
        // now our original stuff will be preserved, we can add the error on top
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
