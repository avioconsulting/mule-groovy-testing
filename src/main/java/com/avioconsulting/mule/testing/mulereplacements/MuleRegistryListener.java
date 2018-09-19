package com.avioconsulting.mule.testing.mulereplacements;

import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.config.custom.CustomizationService;
import org.mule.runtime.module.deployment.api.DeploymentListener;

public class MuleRegistryListener implements DeploymentListener {
    private RuntimeBridgeMuleSide runtimeBridge;
    private Object mockingConfiguration;

    @Override
    public void onArtifactInitialised(String artifactName,
                                      Registry registry) {
        this.runtimeBridge = new RuntimeBridgeMuleSide(registry);
    }

    @Override
    public void onArtifactCreated(String artifactName,
                                  CustomizationService customizationService) {
        customizationService.registerCustomServiceImpl("muleGroovyTestingProcessorIntFactory",
                                                       new MockingProcessorInterceptorFactory(mockingConfiguration));
    }

    public RuntimeBridgeMuleSide getRuntimeBridge() {
        return runtimeBridge;
    }

    public Object getMockingConfiguration() {
        return mockingConfiguration;
    }

    public void setMockingConfiguration(Object mockingConfiguration) {
        this.mockingConfiguration = mockingConfiguration;
    }
}
