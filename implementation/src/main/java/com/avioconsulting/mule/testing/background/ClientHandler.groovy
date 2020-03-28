package com.avioconsulting.mule.testing.background

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class ClientHandler extends SimpleChannelInboundHandler<String> {
    private final List<String> result = []

    List<String> getResult() {
        this.result
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                String response) throws Exception {
        println "received ${response}"
        synchronized(this.result) {
            this.result << response
            this.result.notify()
        }
    }

    @Override
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace()
        ctx.close()
    }
}
