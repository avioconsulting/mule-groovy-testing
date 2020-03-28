package com.avioconsulting.mule.testing.background

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

class ServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        println 'got a message!'
        (msg as ByteBuf).release()
    }

    @Override
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace()
        ctx.close()
    }
}
