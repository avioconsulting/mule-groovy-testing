package com.avioconsulting.mule.testing.mulereplacements;

import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.interception.InterceptionAction;
import org.mule.runtime.api.interception.InterceptionEvent;
import org.mule.runtime.api.interception.ProcessorInterceptor;
import org.mule.runtime.api.interception.ProcessorParameterValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MockingProcessorInterceptor implements ProcessorInterceptor {
    private static final String CONNECTOR_NAME_PARAMETER = "doc:name";
    private final Method isMockEnabledMethod;
    private final Method doMockInvocationMethod;
    private final Object mockingConfiguration;

    MockingProcessorInterceptor(Object mockingConfiguration) {
        this.mockingConfiguration = mockingConfiguration;
        try {
            this.isMockEnabledMethod = mockingConfiguration.getClass().getDeclaredMethod("isMocked",
                                                                                         String.class);
            this.doMockInvocationMethod = mockingConfiguration.getClass().getDeclaredMethod("executeMock",
                                                                                            Object.class,
                                                                                            Object.class,
                                                                                            Object.class);
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
                             Map<String, ProcessorParameterValue> parameters) {
        try {
            doMockInvocationMethod.invoke(mockingConfiguration,
                                          location,
                                          event,
                                          parameters);
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
            } catch (Throwable t) {
                // need to unwrap our reflection based Mule exceptions
                Throwable cause = t.getCause();
                if (!(cause instanceof InvocationTargetException)) {
                    return action.fail(new Exception("Expected exception cause to be a InvocationTargetException but it was: " + cause.getClass().getName()));
                }
                Throwable actualException = ((InvocationTargetException) cause).getTargetException();
                return action.fail(actualException);
            }
        }
        return action.proceed();
    }
}
