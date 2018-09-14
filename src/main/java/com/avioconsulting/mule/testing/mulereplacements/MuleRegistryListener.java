package com.avioconsulting.mule.testing.mulereplacements;

import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.config.custom.CustomizationService;
import org.mule.runtime.module.deployment.api.DeploymentListener;

public class MuleRegistryListener implements DeploymentListener {
    private Registry registry;

    @Override
    public void onArtifactInitialised(String artifactName, Registry registry) {
        this.registry = registry;
    }

    @Override
    public void onArtifactCreated(String artifactName, CustomizationService customizationService) {
        customizationService.registerCustomServiceClass("muleGroovyTestingProcessorIntFactory",
                                                        ProcIntFact.class);
    }

    public Registry getRegistry() {
        return this.registry;
    }
}
