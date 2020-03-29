package com.avioconsulting.mule.testing.background

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j2
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.apache.logging.log4j.core.Logger
import org.junit.runners.model.FrameworkMethod

@Log4j2
class ServerHandler extends SimpleChannelInboundHandler<String> {
    private final Map<String, Object> testClasses = [:]
    private final ObjectMapper objectMapper = new ObjectMapper()

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                String request) throws Exception {
        def close = false
        def parsedRequest = new JsonSlurper().parseText(request)
        if (log.debugEnabled) {
            log.debug "Received request from client: ${JsonOutput.prettyPrint(request)}"
        }
        def klassName = parsedRequest.klass as String
        Object testObject
        def actualLogger = log as Logger
        def testAppender = actualLogger.appenders[CaptureAppender.TEST_APPENDER] as CaptureAppender
        if (!testAppender) {
            log.info 'Adding test appender so we can ship logs to the client'
            testAppender = new CaptureAppender()
            actualLogger.addAppender(testAppender)
        }
        if (testClasses.containsKey(klassName)) {
            testObject = testClasses[klassName]
        } else {
            def testKlass = Class.forName(klassName)
            testObject = testKlass.newInstance()
        }
        def testMethod = testObject.class.getMethod(parsedRequest.method)
        log.info "Invoking ${testMethod} on behalf of the client"
        def frameworkMethod = new FrameworkMethod(testMethod)
        frameworkMethod.invokeExplosively(testObject)
        def responseMap = [
                logs: testAppender.allLogEvents.collect { e ->
                    [
                            level  : e.level.name(),
                            message: e.message.formattedMessage,
                            logger : e.loggerName
                    ]
                }
        ]
        def response = objectMapper.writeValueAsString(responseMap)
        def future = ctx.write(response + '\r\n')
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE)
        }
    }

    @Override
    void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush()
    }

    @Override
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace()
        ctx.close()
    }
}
