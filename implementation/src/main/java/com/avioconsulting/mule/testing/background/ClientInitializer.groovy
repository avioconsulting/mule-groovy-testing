package com.avioconsulting.mule.testing.background

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.DelimiterBasedFrameDecoder
import io.netty.handler.codec.Delimiters
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder

class ClientInitializer extends ChannelInitializer<SocketChannel> {
    private final ClientHandler clientHandler

    ClientInitializer(ClientHandler clientHandler) {
        this.clientHandler = clientHandler
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        def pipeline = socketChannel.pipeline()
        pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()))
        pipeline.addLast(new StringDecoder())
        pipeline.addLast(new StringEncoder())
        pipeline.addLast(clientHandler)
    }
}
