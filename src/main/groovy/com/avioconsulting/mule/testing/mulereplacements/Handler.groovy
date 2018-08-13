package com.avioconsulting.mule.testing.mulereplacements


import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import org.mule.api.processor.MessageProcessor

import java.lang.reflect.Method

class Handler implements MethodInterceptor {
    private final MessageProcessor processorWeMightMock
    private final MockingConfiguration mockingConfiguration

    Handler(MessageProcessor processorWeMightMock,
            MockingConfiguration mockingConfiguration) {
        this.processorWeMightMock = processorWeMightMock
        this.mockingConfiguration = mockingConfiguration
    }

    @Override
    Object intercept(Object obj,
                     Method method,
                     Object[] args,
                     MethodProxy proxy) throws Throwable {
        // work around protected methods for now
        method.accessible = true
        return method.invoke(processorWeMightMock, args)
    }
}
