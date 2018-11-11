package com.avioconsulting.mule.testing.muleinterfaces;

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
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mule.runtime.core.api.util.ClassUtils.withContextClassLoader;

public class MockingProcessorInterceptor implements ProcessorInterceptor {
    private static final String CONNECTOR_NAME_PARAMETER = "doc:name";
    private final Method isMockEnabledMethod;
    private final Method doMockInvocationMethod;
    private final Method getErrorTypeRepositoryMethod;
    private final Object mockingConfiguration;
    private final ClassLoader appClassLoader;

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

    @Override
    public CompletableFuture<InterceptionEvent> around(ComponentLocation location,
                                                       Map<String, ProcessorParameterValue> parameters,
                                                       InterceptionEvent event,
                                                       InterceptionAction action) {
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
        return withContextClassLoader(this.appClassLoader,
                                      () -> doAround(location,
                                                     parameters,
                                                     event,
                                                     action));
    }

    private CompletableFuture<InterceptionEvent> doAround(ComponentLocation location,
                                                          Map<String, ProcessorParameterValue> parameters,
                                                          InterceptionEvent event,
                                                          InterceptionAction action) {
        if (!parameters.containsKey(CONNECTOR_NAME_PARAMETER)) {
            return action.proceed();
        }

        String connectorName = parameters.get(CONNECTOR_NAME_PARAMETER).providedValue();

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
