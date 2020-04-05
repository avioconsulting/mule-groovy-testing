package com.avioconsulting.mule.testing.background

import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.InvokeExceptionWrapper
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
        Map responseMap
        Throwable e = null
        def bridge = testObject.getRuntimeBridge() as RuntimeBridgeTestSide
        try {
            frameworkMethod.invokeExplosively(testObject)
            def logEvents = captureAppender.allLogEvents + bridge.allLogEvents
            responseMap = [
                    logs: logEvents
            ]
        } catch (InvokeExceptionWrapper ew) {
            // message and event are not serializable (but we don't need them here anyways)
            e = new InvokeExceptionWrapper(ew.cause as Exception,
                                           null,
                                           null)
        }
        if (e) {
            // avoid issues with serializing events in Mule exceptions
            def cleanerException = new Exception(e.message)
            new ByteArrayOutputStream().withCloseable { bos ->
                new ObjectOutputStream(bos).withCloseable { oos ->
                    oos.flush()
                    oos.writeObject(cleanerException)
                    def logEvents = captureAppender.allLogEvents + bridge.allLogEvents
                    responseMap = [
                            exception: Base64.encoder.encodeToString(bos.toByteArray()),
                            logs     : logEvents
                    ]
                }
            }
        }
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
