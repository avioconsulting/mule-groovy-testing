package com.avioconsulting.mule.testing.mulereplacements

import com.avioconsulting.mule.testing.mulereplacements.namespacefix.AnnotatedNamespaceHandlerResolver
import org.mule.api.MuleContext
import org.mule.config.ConfigResource
import org.mule.config.spring.MuleArtifactContext
import org.mule.config.spring.OptionalObjectsController
import org.mule.config.spring.util.LaxInstantiationStrategyWrapper
import org.springframework.beans.BeansException
import org.springframework.beans.factory.support.BeanDefinitionReader
import org.springframework.beans.factory.support.CglibSubclassingInstantiationStrategy
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader

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
        // allows us to change implementations w/ proxy objects that we can use for mocking
        def existInstantiationStrategy = new LaxInstantiationStrategyWrapper(new CglibSubclassingInstantiationStrategy(),
                                                                             optionalObjectsController)
        factory.instantiationStrategy = new OurProxyInstantiator(existInstantiationStrategy,
                                                                 this.mockingConfiguration)
        factory
    }

    @Override
    protected BeanDefinitionReader createBeanDefinitionReader(DefaultListableBeanFactory beanFactory) {
        def reader = super.createBeanDefinitionReader(beanFactory)
        assert reader instanceof XmlBeanDefinitionReader
        // need to fix annotations for mocking purposes because annotations on the connectors (e.g. doc:name from
        // xml) is the only way to uniquely identify the instance of the connector to mock
        reader.namespaceHandlerResolver = new AnnotatedNamespaceHandlerResolver(reader.namespaceHandlerResolver)
        reader
    }
}
