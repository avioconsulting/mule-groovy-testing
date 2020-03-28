package com.avioconsulting.mule.testing.background

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel

class BackgroundProcess {
    static void main(String[] args) {
        new BackgroundProcess()
    }

    BackgroundProcess() {
        println 'starting netty event loop with 1 thread'
        def bossGroup = new NioEventLoopGroup(1)
        def workerGroup = new NioEventLoopGroup()
        try {
            def bootstrap = new ServerBootstrap()
            bootstrap.group(bossGroup,
                            workerGroup)
                    .channel(NioServerSocketChannel)
                    .childHandler(new ServerInitializer())
            def channelFuture = bootstrap.bind('localhost',
                                               8888).sync()
            channelFuture.channel().closeFuture().sync()
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
        }
    }
}
