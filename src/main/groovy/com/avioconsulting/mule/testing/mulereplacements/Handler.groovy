package com.avioconsulting.mule.testing.mulereplacements

import groovy.transform.Canonical
import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy

import java.lang.reflect.Method

@Canonical
class Handler implements MethodInterceptor {
    Object original

    @Override
    Object intercept(Object obj,
                     Method method,
                     Object[] args,
                     MethodProxy proxy) throws Throwable {
        return proxy.invoke(original, args)
    }
}
