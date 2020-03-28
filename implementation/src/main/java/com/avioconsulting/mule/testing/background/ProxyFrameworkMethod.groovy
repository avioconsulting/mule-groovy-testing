package com.avioconsulting.mule.testing.background

import org.junit.internal.runners.model.ReflectiveCallable
import org.junit.runners.model.FrameworkMethod

import java.lang.reflect.Method

class ProxyFrameworkMethod extends FrameworkMethod {
    /**
     * Returns a new {@code FrameworkMethod} for {@code method}
     */
    ProxyFrameworkMethod(Method method) {
        super(method)
    }

    @Override
    Object invokeExplosively(Object target,
                             Object... params) throws Throwable {
        // TODO: Invoke remotely
        return new ReflectiveCallable() {
            @Override
            protected Object runReflectiveCall() throws Throwable {
                return method.invoke(target, params);
            }
        }.run()
    }
}
