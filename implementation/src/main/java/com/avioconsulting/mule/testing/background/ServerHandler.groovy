package com.avioconsulting.mule.testing.background

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j2
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.junit.runners.model.FrameworkMethod

@Log4j2
class ServerHandler extends SimpleChannelInboundHandler<String> {
    private final Map<String, Object> testClasses = [:]

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
        def future = ctx.write('done\r\n')
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
