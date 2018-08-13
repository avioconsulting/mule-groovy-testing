package com.avioconsulting.mule.testing.mulereplacements

import groovy.util.logging.Log4j2
import net.sf.cglib.proxy.Enhancer
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
            if (MessageProcessor.isAssignableFrom(beanKlass) && !noMocking.containsKey(beanKlass.name)) {
                return Enhancer.create(beanKlass,
                                       new MockHandler(this.mockingConfiguration))
            }
            return wrapped.instantiate(bd, beanName, owner)
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
                       Constructor<?> ctor, Object... args) throws BeansException {
        wrapped.instantiate(bd, beanName, owner, ctor, args)
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
