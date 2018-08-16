package com.avioconsulting.mule.testing.mulereplacements

import groovy.util.logging.Log4j2
import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import org.mule.api.AnnotatedObject
import org.mule.api.MuleEvent

import java.lang.reflect.Method

@Log4j2
class MockMethodInterceptor implements MethodInterceptor {
    private final MockingConfiguration mockingConfiguration
    private String connectorName

    MockMethodInterceptor(MockingConfiguration mockingConfiguration,
                          String missingConnectorName) {
        this.mockingConfiguration = mockingConfiguration
        this.connectorName = missingConnectorName
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
            MockProcess mockProcess = null
            if (obj instanceof AnnotatedObject) {
                mockProcess = mockingConfiguration.getMockProcess(obj)
            } else if (connectorName) {
                mockProcess = mockingConfiguration.getMockProcess(connectorName)
            } else {
                log.warn "Processor {} does not implement AnnotatedObject and we didn't see annotations in the XML so we cannot locate it for mocking",
                         obj
            }
            if (mockProcess) {
                def muleEvent = args[0] as MuleEvent
                return mockProcess.process(muleEvent,
                                           obj)
            }
        }
        return proxy.invokeSuper(obj, args)
    }
}
