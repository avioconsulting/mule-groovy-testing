package com.avioconsulting.mule.testing.background

import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class ServerHandler extends SimpleChannelInboundHandler<String> {
    @Override
    void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.write('hello from the server side\\r\\n')
        ctx.flush()
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                String request) throws Exception {
        def close = false
        String response
        if (request.isEmpty()) {
            response = 'please type something\r\n'
        } else if (request == 'bye') {
            response = 'see ya\r\n'
            close = true
        } else {
            response = 'nope\r\n'
        }
        def future = ctx.write(response)
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE)
        }
    }

    @Override
    void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush()
    }
}
