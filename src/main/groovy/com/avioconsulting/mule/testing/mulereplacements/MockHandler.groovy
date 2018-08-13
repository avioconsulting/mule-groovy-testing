package com.avioconsulting.mule.testing.mulereplacements

import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import org.mule.api.AnnotatedObject
import org.mule.api.MuleEvent

import java.lang.reflect.Method

class MockHandler implements MethodInterceptor {
    private final MockingConfiguration mockingConfiguration

    MockHandler(MockingConfiguration mockingConfiguration) {
        this.mockingConfiguration = mockingConfiguration
    }

    @Override
    Object intercept(Object obj,
                     Method method,
                     Object[] args,
                     MethodProxy proxy) throws Throwable {
        // TODO: More efficient comparison, also 'cache' the processor name
        if (method.name == 'process' &&
                method.parameterTypes.size() == 1 &&
                method.parameterTypes[0] == MuleEvent) {
            def asAnnotated = obj as AnnotatedObject
            def mock = mockingConfiguration.getMockProcess(asAnnotated)
            def muleEvent = args[0] as MuleEvent
            if (mock) {
                return mock.process(muleEvent,
                                    obj)
            }
        }
        return proxy.invokeSuper(obj, args)
    }
}
