package com.avioconsulting.mule.testing.mulereplacements;

import org.mule.runtime.api.service.ServiceRepository;
import org.mule.runtime.deployment.model.internal.plugin.PluginDependenciesResolver;
import org.mule.runtime.module.artifact.api.classloader.ClassLoaderRepository;
import org.mule.runtime.module.deployment.impl.internal.application.ApplicationClassLoaderBuilderFactory;
import org.mule.runtime.module.deployment.impl.internal.application.ApplicationDescriptorFactory;
import org.mule.runtime.module.deployment.impl.internal.application.DefaultApplicationFactory;
import org.mule.runtime.module.deployment.impl.internal.domain.DomainRepository;
import org.mule.runtime.module.deployment.impl.internal.plugin.ArtifactPluginDescriptorLoader;
import org.mule.runtime.module.deployment.impl.internal.policy.PolicyTemplateClassLoaderBuilderFactory;
import org.mule.runtime.module.extension.internal.loader.ExtensionModelLoaderRepository;
import org.mule.runtime.module.license.api.LicenseValidator;

public class MockingFriendlyAppFactory extends DefaultApplicationFactory {
    public MockingFriendlyAppFactory(DefaultApplicationFactory existing) {
        super(applicationClassLoaderBuilderFactory, applicationDescriptorFactory, domainRepository, serviceRepository, extensionModelLoaderRepository, classLoaderRepository, policyTemplateClassLoaderBuilderFactory, pluginDependenciesResolver, artifactPluginDescriptorLoader, licenseValidator);

    }
}
