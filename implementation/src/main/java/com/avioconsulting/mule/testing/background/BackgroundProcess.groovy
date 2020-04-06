package com.avioconsulting.mule.testing.background

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel

import java.nio.file.*

import static java.nio.file.StandardWatchEventKinds.*

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
            bootstrap.bind('localhost',
                           8888).sync()
            WatchService watcher = FileSystems.getDefault().newWatchService()
            Files.walk(new File('src/main').toPath()).findAll { path ->
                Files.isDirectory(path)
            }.each { Path dir ->
                println "Watching for changes ${dir}"
                dir.register(watcher,
                             ENTRY_CREATE,
                             ENTRY_DELETE,
                             ENTRY_MODIFY)
            }
            for (; ;) {
                WatchKey watchKey = watcher.take()
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind()
                    if (kind == OVERFLOW) {
                        continue
                    }
                    def filename = event.context() as Path
                    println "file is ${filename}"
                }
                if (!watchKey.reset()) {
                    break
                }
            }
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
        }
    }
}
