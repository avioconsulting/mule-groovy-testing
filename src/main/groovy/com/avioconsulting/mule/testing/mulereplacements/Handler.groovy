package com.avioconsulting.mule.testing.mulereplacements

import groovy.transform.Canonical
import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import org.mule.api.processor.MessageProcessor

import java.lang.reflect.Method

@Canonical
class Handler implements MethodInterceptor {
    MessageProcessor processorWeMightMock

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
