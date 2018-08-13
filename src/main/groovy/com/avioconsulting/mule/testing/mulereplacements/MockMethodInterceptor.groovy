package com.avioconsulting.mule.testing.mulereplacements

import groovy.util.logging.Log4j2
import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import org.mule.api.AnnotatedObject
import org.mule.api.MuleEvent

import javax.xml.namespace.QName
import java.lang.reflect.Method

@Log4j2
class MockMethodInterceptor implements MethodInterceptor, AnnotatedObject {
    private final MockingConfiguration mockingConfiguration
    Map<QName, Object> annotations
    private final boolean doAnnotations

    MockMethodInterceptor(MockingConfiguration mockingConfiguration,
                          boolean doAnnotations) {
        this.doAnnotations = doAnnotations
        this.mockingConfiguration = mockingConfiguration
    }

    @Override
    Object intercept(Object obj,
                     Method method,
                     Object[] args,
                     MethodProxy proxy) throws Throwable {
        if (doAnnotations) {
            switch(method.name) {
                // some objects miss annotations, we basically implement annotation storage on this class
                // since each object has its own MockMethodInterceptor
                case 'setAnnotations':
                    setAnnotations(args[0])
                    return
                case 'getAnnotations':
                    return getAnnotations()
                case 'getAnnotation':
                    return getAnnotation(args[0])
            }
        }
        // TODO: More efficient comparison, also 'cache' the processor name
        if (method.name == 'process' &&
                method.parameterTypes.size() == 1 &&
                method.parameterTypes[0] == MuleEvent) {
            if (!(obj instanceof AnnotatedObject)) {
                log.warn 'Processor {} does not implement AnnotatedObject so we cannot locate it for mocking',
                         obj
            } else if ((obj as AnnotatedObject).annotations.isEmpty()) {
                log.warn 'Processor {} has zero annotations so we cannot locate it for mocking',
                         obj
            } else {
                def asAnnotated = obj as AnnotatedObject
                def mock = mockingConfiguration.getMockProcess(asAnnotated)
                def muleEvent = args[0] as MuleEvent
                if (mock) {
                    return mock.process(muleEvent,
                                        obj)
                }
            }
        }
        return proxy.invokeSuper(obj, args)
    }

    @Override
    Object getAnnotation(QName name) {
        this.annotations[name]
    }
}
