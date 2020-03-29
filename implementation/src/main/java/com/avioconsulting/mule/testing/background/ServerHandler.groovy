package com.avioconsulting.mule.testing.background

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j2
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.junit.runners.model.FrameworkMethod

@Log4j2
class ServerHandler extends SimpleChannelInboundHandler<String> {
    private final Map<String, Object> testClasses = [:]
    private final ObjectMapper objectMapper = new ObjectMapper()
    private static CaptureAppender captureAppender

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
        if (!captureAppender) {
            log.info 'Creating test appender so we can ship logs to the client'
            captureAppender = new CaptureAppender()
            def context = LogManager.getContext(false) as LoggerContext
            context.configuration.addAppender(captureAppender)
            context.configuration.getRootLogger().addAppender(captureAppender,
                                                              Level.INFO,
                                                              null)
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
                logs: captureAppender.allLogEvents
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
