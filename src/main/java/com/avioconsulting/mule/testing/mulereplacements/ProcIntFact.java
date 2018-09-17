package com.avioconsulting.mule.testing.mulereplacements;

import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.interception.ProcessorInterceptor;
import org.mule.runtime.api.interception.ProcessorInterceptorFactory;

// TODO: Better name
public class ProcIntFact implements ProcessorInterceptorFactory {
    private final Object mockingConfiguration;

    ProcIntFact(Object mockingConfiguration) {
        this.mockingConfiguration = mockingConfiguration;
    }

    @Override
    public ProcessorInterceptor get() {
        return new OurInt(mockingConfiguration);
    }

    @Override
    public boolean intercept(ComponentLocation location) {
        return true;
    }
}
