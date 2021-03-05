package com.avioconsulting.mule.testing.muleinterfaces.viamuleclassloader;

import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.interception.ProcessorInterceptor;
import org.mule.runtime.api.interception.ProcessorInterceptorFactory;

public class MockingProcessorInterceptorFactory implements ProcessorInterceptorFactory {
    private final Object mockingConfiguration;

    MockingProcessorInterceptorFactory(Object mockingConfiguration) {
        this.mockingConfiguration = mockingConfiguration;
    }

    @Override
    public ProcessorInterceptor get() {
        return new MockingProcessorInterceptor(mockingConfiguration);
    }

    @Override
    public boolean intercept(ComponentLocation location) {
        return true;
    }
}
