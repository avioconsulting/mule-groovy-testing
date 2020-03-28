package com.avioconsulting.mule.testing.background

import com.avioconsulting.mule.testing.junit.BaseJunitTest
import com.avioconsulting.mule.testing.junit.TestState
import com.avioconsulting.mule.testing.muleinterfaces.containers.BaseEngineConfig
import com.avioconsulting.mule.testing.muleinterfaces.containers.MuleEngineContainer
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel

class BackgroundProcess {
    static void main(String[] args) {
        new BackgroundProcess()
    }

    private TestState testState

    BackgroundProcess() {
        def config = new BaseEngineConfig(BaseEngineConfig.defaultFilters,
                                          true)
        def container = new MuleEngineContainer(config)
        this.testState = new TestState(container)
        // ensure any tests we run already see our established state
        BaseJunitTest.testState = testState
        println 'starting netty event loop'
        def bossGroup = new NioEventLoopGroup()
        def workerGroup = new NioEventLoopGroup()
        try {
            def bootstrap = new ServerBootstrap()
            bootstrap.group(bossGroup,
                            workerGroup)
                    .channel(NioServerSocketChannel)
                    .childHandler(new ChannelInitializer() {
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline().addLast(new ServerHandler())
                        }
                    }).option(ChannelOption.SO_BACKLOG,
                              128)
                    .childOption(ChannelOption.SO_KEEPALIVE,
                                 true)
            def channelFuture = bootstrap.bind(8888).sync()
            channelFuture.channel().closeFuture().sync()
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
        }
    }
}
