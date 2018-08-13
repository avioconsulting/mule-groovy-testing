package com.avioconsulting.mule.testing.mulereplacements

import org.mule.api.MuleContext
import org.mule.api.config.ConfigurationException
import org.mule.config.ConfigResource
import org.mule.config.spring.OptionalObjectsController
import org.mule.config.spring.SpringXmlConfigurationBuilder
import org.springframework.context.ApplicationContext

class GroovyTestingSpringXmlConfigurationBuilder extends SpringXmlConfigurationBuilder {
    private final MockingConfiguration mockingConfiguration

    GroovyTestingSpringXmlConfigurationBuilder(String configResources,
                                               MockingConfiguration mockingConfiguration) throws ConfigurationException {
        super(configResources)
        this.mockingConfiguration = mockingConfiguration
    }

    @Override
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
