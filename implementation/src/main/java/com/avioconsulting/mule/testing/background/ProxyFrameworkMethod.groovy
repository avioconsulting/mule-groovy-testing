package com.avioconsulting.mule.testing.background

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonOutput
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
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
                klass : method.declaringClass.name,
                method: method.name
        ]) + '\r\n'
        modifiedTestClass.channel.writeAndFlush(msg).sync()
        def clientHandler = modifiedTestClass.clientHandler
        synchronized (clientHandler.result) {
            clientHandler.result.wait()
            def response = clientHandler.result.remove(0)
            def objectMapper = new ObjectMapper()
            def asMap = objectMapper.readValue(response,
                                               Map)
            asMap.logs.each { log ->
                def level = Level.toLevel(log.level as String)
                def logger = LogManager.getLogger(log.logger as String)
                logger.log(level,
                           log.message as String)
            }
        }
    }
}
