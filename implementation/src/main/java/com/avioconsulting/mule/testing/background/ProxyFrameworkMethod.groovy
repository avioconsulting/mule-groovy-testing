package com.avioconsulting.mule.testing.background

import com.fasterxml.jackson.databind.ObjectMapper
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
            def guid = asMap.guid
            def inSection = false
            def lineCount = new File('.mule/wrapper/logs/linecount.txt')
            def startingLineNumber = lineCount.exists() ? Integer.valueOf(lineCount.text) : 0
            println "starting with line ${startingLineNumber}"
            def totalLines = 0
            new File('.mule/wrapper/logs/wrapper.log').eachLine(startingLineNumber) { line ->
                totalLines++
                if (line.contains("---begin log guid ${guid}---")) {
                    inSection = true
                } else if (inSection) {
                    if (line.contains("---end log guid ${guid}---")) {
                        inSection = false
                    } else {
                        println line
                    }
                }
            }
            lineCount.text = (totalLines - 1).toString()
            def exception = asMap.exception
            if (exception) {
                new ByteArrayInputStream(Base64.decoder.decode(exception)).withCloseable { bis ->
                    new ObjectInputStream(bis).withCloseable { ois ->
                        def e = ois.readObject() as Throwable
                        throw e
                    }
                }
            }
        }
        return null
    }
}
