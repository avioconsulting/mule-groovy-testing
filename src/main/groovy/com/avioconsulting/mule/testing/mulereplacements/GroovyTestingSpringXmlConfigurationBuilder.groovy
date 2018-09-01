package com.avioconsulting.mule.testing.mulereplacements

import org.mule.runtime.config.internal.OptionalObjectsController
import org.mule.runtime.config.internal.SpringXmlConfigurationBuilder
import org.mule.runtime.core.api.MuleContext
import org.mule.runtime.core.api.config.ConfigResource
import org.mule.runtime.core.api.config.ConfigurationException
import org.springframework.context.ApplicationContext

// main purpose of this class is to override the application context with one
// that inserts proxy objects that we can use for mocking. See GroovyTestingArtifactContext
class GroovyTestingSpringXmlConfigurationBuilder extends SpringXmlConfigurationBuilder {
    private final MockingConfiguration mockingConfiguration

    GroovyTestingSpringXmlConfigurationBuilder(String configResources,
                                               MockingConfiguration mockingConfiguration) throws ConfigurationException {
        super(configResources)
        this.mockingConfiguration = mockingConfiguration
    }

    // TODO: protected/private status has changed, will need to tweak this
    //@Override
    protected ApplicationContext doCreateApplicationContext(MuleContext muleContext,
                                                            ConfigResource[] configResources,
                                                            OptionalObjectsController optionalObjectsController) {
        // we have to override MuleArtifactContext (that's the only difference)
        new GroovyTestingArtifactContext(muleContext,
                                         configResources,
                                         optionalObjectsController,
                                         mockingConfiguration)
    }
}
