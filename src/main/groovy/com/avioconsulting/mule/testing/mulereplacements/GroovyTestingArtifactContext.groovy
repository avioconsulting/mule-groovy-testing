package com.avioconsulting.mule.testing.mulereplacements

import com.avioconsulting.mule.testing.mulereplacements.namespacefix.AnnotatedNamespaceHandlerResolver
import org.mule.runtime.api.component.ConfigurationProperties
import org.mule.runtime.app.declaration.api.ArtifactDeclaration
import org.mule.runtime.config.internal.ComponentModelInitializer
import org.mule.runtime.config.internal.LazyMuleArtifactContext
import org.mule.runtime.config.internal.OptionalObjectsController
import org.mule.runtime.config.internal.util.LaxInstantiationStrategyWrapper
import org.mule.runtime.core.api.MuleContext
import org.mule.runtime.core.api.config.ConfigResource
import org.mule.runtime.core.api.config.bootstrap.ArtifactType
import org.springframework.beans.BeansException
import org.springframework.beans.factory.support.BeanDefinitionReader
import org.springframework.beans.factory.support.CglibSubclassingInstantiationStrategy
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader

// uses Spring to insert proxy objects, see ConnectorReplacerProcessor
class GroovyTestingArtifactContext extends LazyMuleArtifactContext {
    private final MockingConfiguration mockingConfiguration

    GroovyTestingArtifactContext(MuleContext muleContext,
                                 ConfigResource[] artifactConfigResources,
                                 ArtifactDeclaration artifactDeclaration,
                                 OptionalObjectsController optionalObjectsController,
                                 Map<String, String> artifactProperties,
                                 ArtifactType artifactType,
                                 List<ClassLoader> pluginsClassLoaders,
                                 Optional<ComponentModelInitializer> parentComponentModelInitializer,
                                 Optional<ConfigurationProperties> parentConfigurationProperties,
                                 boolean disableXmlValidations,
                                 MockingConfiguration mockingConfiguration) throws BeansException {
        super(muleContext,
              artifactConfigResources,
              artifactDeclaration,
              optionalObjectsController,
              artifactProperties,
              artifactType,
              pluginsClassLoaders,
              parentComponentModelInitializer,
              parentConfigurationProperties,
              disableXmlValidations)
        this.mockingConfiguration = mockingConfiguration
    }

    @Override
    protected DefaultListableBeanFactory createBeanFactory() {
        def factory = super.createBeanFactory()
        def ourStrategy = new MockProxyInstantiationStrategy(new CglibSubclassingInstantiationStrategy(),
                                                             this.mockingConfiguration)
        // allows us to change implementations w/ proxy objects that we can use for mocking
        // it's important to have LaxInstantiationStrategyWrapper sit outside our wrapper because
        // it facilitates the optional objects process if necessary. See the catch statements in
        // MockProxyInstantiationStrategy for more details
        factory.instantiationStrategy = new LaxInstantiationStrategyWrapper(ourStrategy,
                                                                            optionalObjectsController)
        factory
    }

    //@Override
    protected BeanDefinitionReader createBeanDefinitionReader(DefaultListableBeanFactory beanFactory) {
        def reader = super.createBeanDefinitionReader(beanFactory)
        assert reader instanceof XmlBeanDefinitionReader
        // need to fix annotations for mocking purposes because annotations on the connectors (e.g. doc:name from
        // xml) is the only way to uniquely identify the instance of the connector to mock
        reader.namespaceHandlerResolver = new AnnotatedNamespaceHandlerResolver(reader.namespaceHandlerResolver)
        reader
    }
}
