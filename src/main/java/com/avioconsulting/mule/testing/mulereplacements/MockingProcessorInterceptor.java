package com.avioconsulting.mule.testing.mulereplacements;

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

public class MockingProcessorInterceptor implements ProcessorInterceptor {
    private static final String CONNECTOR_NAME_PARAMETER = "doc:name";
    private final Method isMockEnabledMethod;
    private final Method doMockInvocationMethod;
    private final Method getErrorTypeRepositoryMethod;
    private final Object mockingConfiguration;

    MockingProcessorInterceptor(Object mockingConfiguration) {
        this.mockingConfiguration = mockingConfiguration;
        try {
            Class<?> mockingConfigClass = mockingConfiguration.getClass();
            this.isMockEnabledMethod = mockingConfigClass.getDeclaredMethod("isMocked",
                                                                            String.class);
            this.doMockInvocationMethod = mockingConfigClass.getDeclaredMethod("executeMock",
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

    private void executeMock(ComponentLocation location,
                             InterceptionEvent event,
                             Map<String, ProcessorParameterValue> parameters) throws InvocationTargetException, IllegalAccessException {
        doMockInvocationMethod.invoke(mockingConfiguration,
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
        if (parameters.containsKey(CONNECTOR_NAME_PARAMETER) &&
                isMockEnabled(parameters.get(CONNECTOR_NAME_PARAMETER).providedValue())) {
            try {
                executeMock(location,
                            event,
                            parameters);
                return action.skip();
            } catch (InvocationTargetException cause) {
                // need to unwrap our reflection based Mule exceptions
                Throwable actualException = cause.getTargetException();
                if (actualException.getClass().getName().equals("com.avioconsulting.mule.testing.mulereplacements.wrappers.ModuleExceptionWrapper")) {
                    // not using action.fail(Throwable) because if you supply the raw exception, flow error handlers will not
                    // receive the error type. action.fail(ErrorType) would then prevent other useful exception
                    // details from being passed along. We have our own implementation that sets the error type
                    // and exception details properly, just like the real ModuleExceptionHandler would
                    return fail((DefaultInterceptionEvent) event,
                                action,
                                actualException);
                }
                return action.fail(actualException);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Should not have had a reflection problem but did",
                                           e);
            }
        }
        return action.proceed();
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

    private CompletableFuture<InterceptionEvent> fail(DefaultInterceptionEvent event,
                                                      InterceptionAction action,
                                                      Throwable moduleExceptionWrapper) {
        CompletableFuture<InterceptionEvent> completableFuture = new CompletableFuture<>();
        ModuleException moduleException = (ModuleException) moduleExceptionWrapper.getCause();
        ErrorTypeDefinition errorTypeDefinition = moduleException.getType();
        ErrorTypeRepository repository = getErrorTypeRepository();
        // see ModuleExceptionWrapper for why we get the namespace for the error this way
        String namespace = getNamespace(moduleExceptionWrapper);
        ComponentIdentifier errorComponentIdentifier = ComponentIdentifier.builder()
                .namespace(namespace)
                .name(errorTypeDefinition.getType())
                .build();
        Optional<ErrorType> errorType = repository.lookupErrorType(errorComponentIdentifier);
        if (!errorType.isPresent()) {
            throw new RuntimeException("Unable to lookup error type! " + errorComponentIdentifier);
        }
        event.setError(errorType.get(),
                       moduleException);
        Processor processor = getProcessor(action);
        if (!(processor instanceof Component)) {
            throw new RuntimeException("Expected processor to be an instance of Component but was: " + processor.getClass().getName());
        }
        Component component = (Component) processor;

        completableFuture.completeExceptionally(new MessagingException(event.resolve(),
                                                                       moduleException,
                                                                       component));
        return completableFuture;
    }
}
