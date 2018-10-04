package com.avioconsulting.mule.testing.mulereplacements;

import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.deployment.management.ComponentInitialStateManager;

import java.lang.reflect.Method;

public class SourceDisableManager implements ComponentInitialStateManager {
    private final Object mockingConfiguration;
    private final Method shouldFlowListenerBeEnabledMethod;

    SourceDisableManager(Object mockingConfiguration) {
        this.mockingConfiguration = mockingConfiguration;
        Class<?> klass = mockingConfiguration.getClass();
        try {
            this.shouldFlowListenerBeEnabledMethod = klass.getDeclaredMethod("shouldFlowListenerBeEnabled",
                                                                             String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean shouldFlowListenerBeEnabledMethod(String flowName) {
        try {
            return (boolean) shouldFlowListenerBeEnabledMethod.invoke(mockingConfiguration,
                                                                      flowName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean mustStartMessageSource(Component component) {
        String flowName = component.getLocation().getRootContainerName();
        return shouldFlowListenerBeEnabledMethod(flowName);
    }
}
