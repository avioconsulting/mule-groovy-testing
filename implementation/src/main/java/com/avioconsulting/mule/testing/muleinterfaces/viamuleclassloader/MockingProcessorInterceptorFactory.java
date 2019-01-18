package com.avioconsulting.mule.testing.muleinterfaces.viamuleclassloader;

import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.interception.ProcessorInterceptor;
import org.mule.runtime.api.interception.ProcessorInterceptorFactory;

public class MockingProcessorInterceptorFactory implements ProcessorInterceptorFactory {
    private final Object mockingConfiguration;
    private final ClassLoader appClassLoader;

    MockingProcessorInterceptorFactory(Object mockingConfiguration,
                                       ClassLoader appClassLoader) {
        this.mockingConfiguration = mockingConfiguration;
        this.appClassLoader = appClassLoader;
    }

    @Override
    public ProcessorInterceptor get() {
        return new MockingProcessorInterceptor(mockingConfiguration,
                                               appClassLoader);
    }

    @Override
    public boolean intercept(ComponentLocation location) {
        return true;
    }
}
