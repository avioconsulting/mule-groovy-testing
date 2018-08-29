package com.avioconsulting.mule.testing.mulereplacements

import groovy.util.logging.Log4j2
import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import org.mule.api.AnnotatedObject
import org.mule.api.MuleEvent
import org.mule.api.MuleException
import org.mule.api.processor.MessageProcessor

import java.lang.reflect.Method

@Log4j2
class MockFactoryBeanInterceptor implements MethodInterceptor {
    private final MockingConfiguration mockingConfiguration

    MockFactoryBeanInterceptor(MockingConfiguration mockingConfiguration) {
        this.mockingConfiguration = mockingConfiguration
    }

    @Override
    Object intercept(Object obj,
                     Method method,
                     Object[] args,
                     MethodProxy proxy) throws Throwable {
        if (method.name == 'getObject' && obj.getObjectType() == MessageProcessor) {
            if (!(obj instanceof AnnotatedObject)) {
                log.info 'Processor/bean factory {} does not have annotations, so it will not be mockable',
                         obj
                return proxy.invokeSuper(obj, args)
            }
            def beanFactory = obj
            def actualMessageProcessor = proxy.invokeSuper(obj, args)
            return new MessageProcessor() {
                @Override
                MuleEvent process(MuleEvent event) throws MuleException {
                    def mockProcess = mockingConfiguration.getMockProcess(beanFactory)
                    if (mockProcess) {
                        return mockProcess.process(event,
                                                   actualMessageProcessor)
                    }
                    return actualMessageProcessor.process(event)
                }
            }
        }
        return proxy.invokeSuper(obj, args)
    }
}
