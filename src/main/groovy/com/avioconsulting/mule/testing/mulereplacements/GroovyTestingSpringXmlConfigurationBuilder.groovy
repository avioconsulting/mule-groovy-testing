package com.avioconsulting.mule.testing.mulereplacements

import org.mule.runtime.app.declaration.api.ArtifactDeclaration
import org.mule.runtime.config.internal.*
import org.mule.runtime.core.api.MuleContext
import org.mule.runtime.core.api.config.ConfigurationException
import org.mule.runtime.core.api.config.bootstrap.ArtifactType
import org.mule.runtime.core.internal.context.DefaultMuleContext
import org.mule.runtime.core.internal.context.MuleContextWithRegistries
import org.mule.runtime.module.extension.api.manager.DefaultExtensionManagerFactory

import static org.mule.runtime.deployment.model.internal.application.MuleApplicationClassLoader.resolveContextArtifactPluginClassLoaders

// main purpose of this class is to override the application context with one
// that inserts proxy objects that we can use for mocking. See GroovyTestingArtifactContext
class GroovyTestingSpringXmlConfigurationBuilder extends SpringXmlConfigurationBuilder {
    private final MockingConfiguration mockingConfiguration

    GroovyTestingSpringXmlConfigurationBuilder(String configResources,
                                               MockingConfiguration mockingConfiguration) throws ConfigurationException {
        super(configResources)
        this.mockingConfiguration = mockingConfiguration
    }

    @Override
    protected void doConfigure(MuleContext muleContext) throws Exception {
        new DefaultExtensionManagerFactory().create(muleContext)
        def muleArtifactContext = createApplicationContext(muleContext)
        def registry = new SpringRegistry(muleArtifactContext,
                                          muleContext,
                                          muleArtifactContext.getDependencyResolver(),
                                          ((DefaultMuleContext) muleContext).getLifecycleInterceptor())
        ((MuleContextWithRegistries) muleContext).addRegistry(registry)
    }

    private MuleArtifactContext createApplicationContext(MuleContext muleContext) throws Exception {
        def applicationObjectcontroller = new DefaultOptionalObjectsController()
        def muleArtifactContext = doCreateApplicationContext(muleContext,
                                                             new ArtifactDeclaration(),
                                                             applicationObjectcontroller)
        serviceConfigurators.each { c ->
            c.configure(muleContext.customizationService)
        }
        muleArtifactContext
    }


    private MuleArtifactContext doCreateApplicationContext(MuleContext muleContext,
                                                           ArtifactDeclaration artifactDeclaration,
                                                           OptionalObjectsController optionalObjectsController) {
        // we have to override MuleArtifactContext (that's the only difference)
        new GroovyTestingArtifactContext(muleContext,
                                         artifactConfigResources,
                                         artifactDeclaration,
                                         optionalObjectsController,
                                         getArtifactProperties(),
                                         ArtifactType.APP,
                                         resolveContextArtifactPluginClassLoaders(),
                                         Optional.empty(),
                                         Optional.empty(),
                                         false,
                                         this.mockingConfiguration)
    }
}
