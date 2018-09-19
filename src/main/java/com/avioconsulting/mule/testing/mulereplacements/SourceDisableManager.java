package com.avioconsulting.mule.testing.mulereplacements;

import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.deployment.management.ComponentInitialStateManager;

public class SourceDisableManager implements ComponentInitialStateManager {
    @Override
    public boolean mustStartMessageSource(Component component) {
        return false;
    }
}
