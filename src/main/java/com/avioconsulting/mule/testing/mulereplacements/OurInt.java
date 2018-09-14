package com.avioconsulting.mule.testing.mulereplacements;

import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.interception.InterceptionEvent;
import org.mule.runtime.api.interception.ProcessorInterceptor;
import org.mule.runtime.api.interception.ProcessorParameterValue;

import java.util.Map;

public class OurInt implements ProcessorInterceptor {
    // TODO: Implement around, we can skip there and doc:name is in parameters. See what we can we do with manipulating InterceptionEvent
    @Override
    public void before(ComponentLocation location, Map<String, ProcessorParameterValue> parameters, InterceptionEvent event) {
        System.out.println("here we are!");
    }
}
