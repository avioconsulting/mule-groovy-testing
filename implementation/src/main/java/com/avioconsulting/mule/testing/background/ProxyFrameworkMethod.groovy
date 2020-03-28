package com.avioconsulting.mule.testing.background


import org.junit.internal.runners.model.ReflectiveCallable
import org.junit.runners.model.FrameworkMethod

import java.lang.reflect.Method

class ProxyFrameworkMethod extends FrameworkMethod {
    /**
     * Returns a new {@code FrameworkMethod} for {@code method}
     */
    private final ModifiedTestClass modifiedTestClass

    ProxyFrameworkMethod(Method method,
                         ModifiedTestClass modifiedTestClass) {
        super(method)
        this.modifiedTestClass = modifiedTestClass
    }

    @Override
    Object invokeExplosively(Object target,
                             Object... params) throws Throwable {
        def msg = "test method: ${method}\r\n".toString()
        println "firing off msg to channel: ${msg}"
        modifiedTestClass.channel.writeAndFlush(msg).sync()
    }
}
