package com.avioconsulting.mule.testing.mulereplacements

import com.avioconsulting.mule.testing.mulereplacements.endpoints.OverrideEndpointFactory
import com.avioconsulting.mule.testing.mulereplacements.namespacefix.WrappedNamespaceHandler
import groovy.util.logging.Log4j2
import net.sf.cglib.proxy.Enhancer
import org.mule.api.AnnotatedObject
import org.mule.api.endpoint.EndpointFactory
import org.mule.api.processor.MessageProcessor
import org.mule.construct.Flow
import org.mule.processor.chain.InterceptingChainLifecycleWrapper
import org.springframework.beans.BeansException
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.support.InstantiationStrategy
import org.springframework.beans.factory.support.RootBeanDefinition

import java.lang.reflect.Constructor
import java.lang.reflect.Method

@Log4j2
class OurProxyInstantiator implements InstantiationStrategy {
    private static final Map<String, Integer> noMocking = [
            'com.mulesoft.weave.mule.WeaveMessageProcessor': 1,
            (Flow.name)                                    : 1,
            (InterceptingChainLifecycleWrapper.name)       : 1
    ]

    private final InstantiationStrategy wrapped
    private final MockingConfiguration mockingConfiguration

    OurProxyInstantiator(InstantiationStrategy wrapped,
                         MockingConfiguration mockingConfiguration) {
        this.wrapped = wrapped
        this.mockingConfiguration = mockingConfiguration
    }

    @Override
    Object instantiate(RootBeanDefinition bd,
                       String beanName,
                       BeanFactory owner) throws BeansException {
        def beanKlass = bd.beanClass
        try {
            // need to change the endpoint factory for VMs, etc.
            if (EndpointFactory.isAssignableFrom(beanKlass)) {
                def underlying = wrapped.instantiate(bd, beanName, owner)
                assert underlying instanceof EndpointFactory
                return new OverrideEndpointFactory(underlying,
                                                   mockingConfiguration)
            }
            if (MessageProcessor.isAssignableFrom(beanKlass) && !noMocking.containsKey(beanKlass.name)) {
                def missingConnectorName = AnnotatedObject.isAssignableFrom(beanKlass) ? null :
                        bd.getAttribute(WrappedNamespaceHandler.ANNOTATION_NAME_ATTRIBUTE) as String
                return Enhancer.create(beanKlass, new MockMethodInterceptor(this.mockingConfiguration,
                                                                            missingConnectorName))
            }
            return wrapped.instantiate(bd,
                                       beanName,
                                       owner)
        }
        catch (e) {
            log.error("While intercepting bean class ${beanKlass.name}/bean ${beanName}",
                      e)
            throw e
        }
    }

    @Override
    Object instantiate(RootBeanDefinition bd,
                       String beanName,
                       BeanFactory owner,
                       Constructor<?> ctor,
                       Object... args) throws BeansException {
        def beanKlass = bd.beanClass
        if (beanKlass == Flow) {
            log.info "Disabling listener for flow '{}'",
                     beanName
            bd.propertyValues.removePropertyValue('messageSource')
        }
        if (MessageProcessor.isAssignableFrom(beanKlass) && !noMocking.containsKey(beanKlass.name)) {
            def missingConnectorName = AnnotatedObject.isAssignableFrom(beanKlass) ? null :
                    bd.getAttribute(WrappedNamespaceHandler.ANNOTATION_NAME_ATTRIBUTE) as String
            return new Enhancer().with {
                superclass = beanKlass
                callback = new MockMethodInterceptor(this.mockingConfiguration,
                                                     missingConnectorName)
                create(ctor.parameterTypes,
                       args)
            }
        }
        return wrapped.instantiate(bd,
                                   beanName,
                                   owner,
                                   ctor,
                                   args)
    }

    @Override
    Object instantiate(RootBeanDefinition bd,
                       String beanName,
                       BeanFactory owner,
                       Object factoryBean,
                       Method factoryMethod, Object... args) throws BeansException {
        wrapped.instantiate(bd, beanName, owner, factoryBean, factoryMethod, args)
    }
}
