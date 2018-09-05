package com.avioconsulting.mule.testing.mulereplacements

import org.mule.runtime.api.artifact.Registry
import org.mule.runtime.module.deployment.api.DeploymentListener

class MuleRegistryListener implements DeploymentListener {
    private Registry registry

    @Override
    void onArtifactStarted(String artifactName,
                           Registry registry) {
        this.registry = registry
    }

    Registry getRegistry() {
        assert registry: 'onArtifactStarted has not set the registry yet!'
        this.registry
    }
}
