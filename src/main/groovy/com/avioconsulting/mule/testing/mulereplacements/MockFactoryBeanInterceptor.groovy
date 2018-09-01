package com.avioconsulting.mule.testing.mulereplacements

import groovy.util.logging.Log4j2
import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import org.mule.runtime.api.exception.MuleException
import org.mule.runtime.core.api.event.CoreEvent
import org.mule.runtime.core.api.processor.Processor

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
        if (method.name == 'getObject' && obj.getObjectType() == Processor) {
            assert false : 'annotatedobject?'
            if (!(obj instanceof Object)) {
                log.info 'Processor/bean factory {} does not have annotations, so it will not be mockable',
                         obj
                return proxy.invokeSuper(obj, args)
            }
            def beanFactory = obj
            def actualMessageProcessor = proxy.invokeSuper(obj, args)
            return new Processor() {
                @Override
                CoreEvent process(CoreEvent event) throws MuleException {
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
