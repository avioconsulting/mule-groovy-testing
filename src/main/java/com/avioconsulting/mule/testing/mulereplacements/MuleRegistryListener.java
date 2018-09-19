package com.avioconsulting.mule.testing.mulereplacements;

import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.config.custom.CustomizationService;
import org.mule.runtime.module.deployment.api.DeploymentListener;

import java.util.HashMap;
import java.util.Map;

public class MuleRegistryListener implements DeploymentListener {
    private final Map<String, RuntimeBridgeMuleSide> runtimeBridges;
    private final Map<String, Object> mockingConfigurations;

    public MuleRegistryListener() {
        this.runtimeBridges = new HashMap<>();
        this.mockingConfigurations = new HashMap<>();
    }

    @Override
    public void onArtifactInitialised(String artifactName,
                                      Registry registry) {
        this.runtimeBridges.put(artifactName, new RuntimeBridgeMuleSide(registry));
    }

    @Override
    public void onArtifactCreated(String artifactName,
                                  CustomizationService custSvc) {
        Object mockingConfiguration = mockingConfigurations.get(artifactName);
        custSvc.registerCustomServiceImpl("muleGroovyTestingProcessorIntFactory",
                                          new MockingProcessorInterceptorFactory(mockingConfiguration));
        custSvc.registerCustomServiceImpl("muleGroovySourceDisableManager",
                                          new SourceDisableManager());
    }

    public RuntimeBridgeMuleSide getRuntimeBridge(String artifactName) {
        return this.runtimeBridges.get(artifactName);
    }

    public void setMockingConfiguration(String artifactName,
                                        Object mockingConfiguration) {
        this.mockingConfigurations.put(artifactName,
                                       mockingConfiguration);
    }
}
