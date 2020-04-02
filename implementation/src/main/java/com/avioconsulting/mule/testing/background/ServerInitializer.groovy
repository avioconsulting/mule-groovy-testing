package com.avioconsulting.mule.testing.background

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.DelimiterBasedFrameDecoder
import io.netty.handler.codec.Delimiters
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder

class ServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        def pipeline = socketChannel.pipeline()
        pipeline.addLast(new DelimiterBasedFrameDecoder(500000,
                                                        Delimiters.lineDelimiter()))
        pipeline.addLast(new StringDecoder())
        pipeline.addLast(new StringEncoder())
        pipeline.addLast(new ServerHandler())
    }
}
