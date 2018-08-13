package com.avioconsulting.mule.testing.mulereplacements

import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import org.mule.api.AnnotatedObject
import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

import javax.xml.namespace.QName
import java.lang.reflect.Method

class MockHandler implements MethodInterceptor {
    private final MessageProcessor processorWeMightMock
    private final MockingConfiguration mockingConfiguration
    private static final QName processorName = new QName('http://www.mulesoft.org/schema/mule/documentation', 'name')

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
            def processorName = asAnnotated.annotations.get(processorName) as String
            def mock = mockingConfiguration.mocks[processorName]
            if (mock) {
                return mock.process(args[0],
                                    processorWeMightMock)
            }
        }
        // work around protected methods for now
        method.accessible = true
        return method.invoke(processorWeMightMock, args)
    }
}
