package com.avioconsulting.mule.testing.mulereplacements;

import org.mule.runtime.api.component.TypedComponentIdentifier;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.interception.ProcessorInterceptor;
import org.mule.runtime.api.interception.ProcessorInterceptorFactory;

public class ProcIntFact implements ProcessorInterceptorFactory {
    @Override
    public ProcessorInterceptor get() {
        System.out.println("**************||||||||||||hi brady!");
        return new OurInt();
    }

    @Override
    public boolean intercept(ComponentLocation location) {
        // TODO: We can probably just return true here because we can't get the connector's name yet
        TypedComponentIdentifier id = location.getComponentIdentifier();
        System.out.println("intercept name: "+id);
        return id.getIdentifier().getName().equals("request");
    }
}
