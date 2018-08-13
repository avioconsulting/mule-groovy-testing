package com.avioconsulting.mule.testing.mulereplacements

import groovy.util.logging.Log4j2
import net.sf.cglib.proxy.Enhancer
import org.mule.api.processor.MessageProcessor
import org.mule.construct.Flow
import org.mule.processor.chain.InterceptingChainLifecycleWrapper
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.BeanPostProcessor

@Log4j2
class ConnectorReplacerProcessor implements BeanPostProcessor {
    private final MockingConfiguration mockingConfiguration

    ConnectorReplacerProcessor(MockingConfiguration mockingConfiguration) {
        this.mockingConfiguration = mockingConfiguration
    }

    private static final Map<String, Integer> noMocking = [
            'com.mulesoft.weave.mule.WeaveMessageProcessor': 1,
            (Flow.name)                                    : 1,
            (InterceptingChainLifecycleWrapper.name)       : 1
    ]

    @Override
    Object postProcessBeforeInitialization(Object bean,
                                           String beanName) throws BeansException {
        bean
    }

    @Override
    Object postProcessAfterInitialization(Object bean,
                                          String beanName) throws BeansException {
        def beanKlass = bean.class
        try {
            if (bean instanceof MessageProcessor && !noMocking.containsKey(beanKlass.name)) {
                return Enhancer.create(beanKlass, new MockHandler(bean,
                                                                  this.mockingConfiguration))
            }
            return bean
        }
        catch (e) {
            log.error("While intercepting bean class ${beanKlass.name}/bean ${beanName}",
                      e)
            throw e
        }
    }
}
