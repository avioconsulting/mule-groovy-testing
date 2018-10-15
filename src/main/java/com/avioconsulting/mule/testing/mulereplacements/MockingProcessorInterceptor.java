package com.avioconsulting.mule.testing.mulereplacements;

import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.exception.ErrorTypeRepository;
import org.mule.runtime.api.interception.InterceptionAction;
import org.mule.runtime.api.interception.InterceptionEvent;
import org.mule.runtime.api.interception.ProcessorInterceptor;
import org.mule.runtime.api.interception.ProcessorParameterValue;
import org.mule.runtime.api.message.ErrorType;
import org.mule.runtime.core.internal.exception.MessagingException;
import org.mule.runtime.core.internal.interception.DefaultInterceptionEvent;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.exception.ModuleException;

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
                if (actualException instanceof ModuleException) {
                    CompletableFuture<InterceptionEvent> completableFuture = new CompletableFuture<>();
                    DefaultInterceptionEvent realEvent = (DefaultInterceptionEvent) event;
                    ModuleException moduleException = (ModuleException) actualException;
                    ErrorTypeDefinition theType = moduleException.getType();
                    ComponentIdentifier identifier = location.getComponentIdentifier().getIdentifier();
                    ErrorTypeRepository errorTypeRepo = getErrorTypeRepository();
                    ComponentIdentifier errorComponentIdentifier = ComponentIdentifier.builder()
                            // the component's namespace is lcased, it worked this way but being conservative here
                            .namespace(identifier.getNamespace().toUpperCase())
                            .name(theType.getType())
                            .build();
                    Optional<ErrorType> errorType = errorTypeRepo.lookupErrorType(errorComponentIdentifier);
                    if (!errorType.isPresent()) {
                        throw new RuntimeException("Unable to lookup error type! " + errorComponentIdentifier);
                    }
                    realEvent.setError(errorType.get(),
                                       actualException);
                    //processorField = c.class.getDeclaredField("ReactiveInterceptionAction")
                    completableFuture.completeExceptionally(new MessagingException(realEvent.resolve(),
                                                                                   actualException,
                                                                                   null));
                    return completableFuture;
                }
                return action.fail(actualException);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Should not have had a reflection problem but did",
                                           e);
            }
        }
        return action.proceed();
    }


}
