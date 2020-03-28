package com.avioconsulting.mule.testing.background

import groovy.json.JsonOutput
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
        def msg = JsonOutput.toJson([
                klass: method.declaringClass.name,
                method: method.name
        ]) + '\r\n'
        modifiedTestClass.channel.writeAndFlush(msg).sync()
        def clientHandler = modifiedTestClass.clientHandler
        synchronized (clientHandler.result) {
            clientHandler.result.wait()
            def response = clientHandler.result.remove(0)
            println "got response ${response}"
        }
    }
}
