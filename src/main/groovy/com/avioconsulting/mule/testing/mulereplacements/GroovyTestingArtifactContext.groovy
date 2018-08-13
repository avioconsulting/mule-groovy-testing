package com.avioconsulting.mule.testing.mulereplacements

import org.mule.api.MuleContext
import org.mule.config.ConfigResource
import org.mule.config.spring.MuleArtifactContext
import org.mule.config.spring.OptionalObjectsController
import org.mule.config.spring.util.LaxInstantiationStrategyWrapper
import org.springframework.beans.BeansException
import org.springframework.beans.factory.support.CglibSubclassingInstantiationStrategy
import org.springframework.beans.factory.support.DefaultListableBeanFactory

// uses Spring to insert proxy objects, see ConnectorReplacerProcessor
class GroovyTestingArtifactContext extends MuleArtifactContext {
    private final MockingConfiguration mockingConfiguration

    GroovyTestingArtifactContext(MuleContext muleContext,
                                 ConfigResource[] configResources,
                                 OptionalObjectsController optionalObjectsController,
                                 MockingConfiguration mockingConfiguration) throws BeansException {
        super(muleContext,
              configResources,
              optionalObjectsController)
        this.mockingConfiguration = mockingConfiguration
    }

    @Override
    protected DefaultListableBeanFactory createBeanFactory() {
        def factory = super.createBeanFactory()
        def existInstantiationStrategy = new LaxInstantiationStrategyWrapper(new CglibSubclassingInstantiationStrategy(),
                                                                             optionalObjectsController)
        factory.instantiationStrategy = new OurProxyInstantiator(existInstantiationStrategy,
                                                                 this.mockingConfiguration)
        factory
    }
}
