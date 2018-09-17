package com.avioconsulting.mule.testing.mulereplacements;

import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.interception.InterceptionAction;
import org.mule.runtime.api.interception.InterceptionEvent;
import org.mule.runtime.api.interception.ProcessorInterceptor;
import org.mule.runtime.api.interception.ProcessorParameterValue;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

// TODO: Better name
public class OurInt implements ProcessorInterceptor {
    private static final String CONNECTOR_NAME_PARAMETER = "doc:name";
    private final Method isMockEnabledMethod;
    private final Method doMockInvocationMethod;
    private final Object mockingConfiguration;

    OurInt(Object mockingConfiguration) {
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
            executeMock(location,
                        event,
                        parameters);
            // TODO: Cover error cases w/ a catch
            return action.skip();
        }
        return action.proceed();
    }
}
