package com.avioconsulting.mule.testing.mulereplacements;

import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.config.custom.CustomizationService;
import org.mule.runtime.api.deployment.management.ComponentInitialStateManager;
import org.mule.runtime.module.deployment.api.DeploymentListener;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MuleRegistryListener implements DeploymentListener {
    // we share a single listener for the whole container so if we change apps
    // (e.g. we change our config files/properties/etc. we need to be able to differentiate)
    private final Map<String, RuntimeBridgeMuleSide> runtimeBridges;
    private final Map<String, Object> mockingConfigurations;

    public MuleRegistryListener() {
        this.runtimeBridges = new HashMap<>();
        this.mockingConfigurations = new HashMap<>();
    }

    // this will run after onArtifactCreated and will allow our test code to use
    // the registry to get ahold of certain Mule objects (like running flows, etc.)
    @Override
    public void onArtifactInitialised(String artifactName,
                                      Registry registry) {
        RuntimeBridgeMuleSide bridge = new RuntimeBridgeMuleSide(registry);
        this.runtimeBridges.put(artifactName, bridge);
        // it's handy for our mocking config to have access to the runtime bridge object
        Object mockingConfiguration = mockingConfigurations.get(artifactName);
        try {
            Class<?> mockingConfigClass = mockingConfiguration.getClass();
            Method setter = mockingConfigClass.getDeclaredMethod("setRuntimeBridgeMuleSide",
                                                                 Object.class);
            setter.invoke(mockingConfiguration,
                          bridge);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // this will run first before onArtifactInitialised does. we use it to tweak some services
    // e.g. inject our mock interceptor, etc. The mocking config will have already
    // been set by MuleEngineContainer before deployment happens
    @Override
    public void onArtifactCreated(String artifactName,
                                  CustomizationService custSvc) {
        Object mockingConfiguration = mockingConfigurations.get(artifactName);
        custSvc.registerCustomServiceImpl("muleGroovyTestingProcessorIntFactory",
                                          new MockingProcessorInterceptorFactory(mockingConfiguration));
        custSvc.overrideDefaultServiceImpl(ComponentInitialStateManager.SERVICE_ID,
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
