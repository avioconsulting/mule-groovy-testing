package com.avioconsulting.mule.testing.mulereplacements

import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import org.mule.api.AnnotatedObject
import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

import java.lang.reflect.Method

class MockHandler implements MethodInterceptor {
    private final MessageProcessor processorWeMightMock
    private final MockingConfiguration mockingConfiguration

    MockHandler(MessageProcessor processorWeMightMock,
                MockingConfiguration mockingConfiguration) {
        this.processorWeMightMock = processorWeMightMock
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
            def asAnnotated = processorWeMightMock as AnnotatedObject
            def mock = mockingConfiguration.getMockProcess(asAnnotated)
            def muleEvent = args[0] as MuleEvent
            if (mock) {
                return mock.process(muleEvent,
                                    processorWeMightMock)
            }
        }
        // work around protected methods for now
        method.accessible = true
        return method.invoke(processorWeMightMock, args)
    }
}
