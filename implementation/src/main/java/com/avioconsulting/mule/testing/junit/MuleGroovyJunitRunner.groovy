package com.avioconsulting.mule.testing.junit

import com.avioconsulting.mule.testing.EnvironmentDetector
import com.avioconsulting.mule.testing.background.ClientHandler
import com.avioconsulting.mule.testing.background.ClientInitializer
import com.avioconsulting.mule.testing.background.ModifiedTestClass
import groovy.util.logging.Log4j2
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import org.apache.commons.io.FileUtils
import org.apache.maven.shared.utils.Os
import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.InitializationError
import org.junit.runners.model.TestClass

import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import java.util.regex.Pattern

@Log4j2
class MuleGroovyJunitRunner extends
        BlockJUnit4ClassRunner implements EnvironmentDetector {
    static boolean listenerSetup = false
    static NioEventLoopGroup eventLoopGroup
    static Channel channel
    static ClientHandler clientHandler

    static boolean isRemoteClient() {
        System.getenv('RUN_REMOTE') == '1'
    }

    static {
        // TODO: Separate method (or even class)
        if (isRemoteClient()) {
            def wrapperDir = new File('.mule',
                                      'wrapper')
            def binDir = new File(wrapperDir,
                                  'bin')
            def logsDir = new File(wrapperDir,
                                   'logs')
            if (!wrapperDir.exists()) {
                log.info "Wrapper does not yet exist at ${wrapperDir.absolutePath}, building"
                def resource = MuleGroovyJunitRunner.getResource('/muleTestEngine')
                assert resource: 'Have you run the app assembler build?'
                wrapperDir.mkdirs()
                FileUtils.copyDirectory(new File(resource.toURI()),
                                        wrapperDir)
                // wrapper won't run without this
                logsDir.mkdirs()
                // for some reason the script/wrapper files is not already marked as executable
                ['muleTestEngine',
                 'wrapper-macosx-universal-64',
                 'wrapper-linux-x86-64'].each { fname ->
                    def script = new File(binDir,
                                          fname)
                    def permissions = PosixFilePermissions.fromString('rwxr-xr-x')
                    Files.setPosixFilePermissions(script.toPath(),
                                                  permissions)
                }
                def classpaths = System.getProperty('java.class.path').split(File.pathSeparator)
                def withWrapper = ['.mule/wrapper/lib/wrapper.jar'] + classpaths.toList()
                def lines = withWrapper.withIndex().collect { cp, index ->
                    "wrapper.java.classpath.${index + 1}=${cp}"
                }
                def confDir = new File(wrapperDir,
                                       'etc')
                def wrapperConfFile = new File(confDir,
                                               'wrapper.conf')
                // we'll use our classpath now (not when this lib was built) to avoid .m2 resolution issues, etc.
                def newWrapperConfText = wrapperConfFile.text
                // need to use a different directory to find all of our test stuff
                        .replace('wrapper.working.dir=..',
                                 'wrapper.working.dir=../../..')
                        .replace('wrapper.java.library.path.1=lib',
                                 'wrapper.java.library.path.1=.mule/wrapper/lib')
                        .replace('wrapper.logfile=../logs/wrapper.log',
                                 'wrapper.logfile=.mule/wrapper/logs/wrapper.log')
                        .replaceAll(Pattern.compile(/wrapper\.java\.classpath\.\d+=.*/),
                                    '')
                newWrapperConfText += lines.join('\n')
                wrapperConfFile.text = newWrapperConfText
                log.info 'Wrapper setup complete'
            }
            def pidFile = new File(logsDir,
                                   'muleTestEngine.pid')
            if (pidFile.exists()) {
                log.info "Wrapper already running at PID ${pidFile.text}"
            } else {
                def launch = new File(binDir,
                                      Os.isFamily(Os.FAMILY_WINDOWS) ? 'muleTestEngine.bat' : 'muleTestEngine')
                log.info "Launching wrapper using ${launch.absolutePath}"
                def processBuilder = new ProcessBuilder(launch.absolutePath,
                                                        'start')
                // we don't want the background process to inherit this from us because we need it to start the engine
                processBuilder.environment().remove('RUN_REMOTE')
                def process = processBuilder.redirectErrorStream(true)
                        .start()
                process.inputStream.eachLine { log.info it }
                assert process.waitFor() == 0
                log.info 'Wrapper launched successfully'
            }
            Throwable exception = null
            10.times {
                if (channel != null && exception == null) {
                    return
                }
                try {
                    eventLoopGroup = new NioEventLoopGroup()
                    clientHandler = new ClientHandler()
                    def b = new Bootstrap()
                    b.group(eventLoopGroup)
                            .channel(NioSocketChannel)
                            .handler(new ClientInitializer(clientHandler))
                    channel = b.connect('localhost',
                                        8888).sync().channel()
                    exception = null
                } catch (e) {
                    exception = e
                    log.info "Server not up yet, waiting 100 ms and retrying"
                    Thread.sleep(100)
                }
            }
            if (exception) {
                throw exception
            }
        }
    }

    MuleGroovyJunitRunner(Class<?> klass) throws InitializationError {
        super(klass)
    }

    @Override
    protected void runChild(FrameworkMethod method,
                            RunNotifier notifier) {
        // this method is called for every test, so only do this once
        if (!listenerSetup) {
            listenerSetup = true
            if (isEclipse()) {
                log.info 'Since tests are being run via Eclipse, have to use JVM shutdown hook to shutdown Mule. Eclipse Junit runner otherwise will shutdown Mule after every test class'
                Runtime.runtime.addShutdownHook(new Thread() {
                    @Override
                    void run() {
                        BaseJunitTest.testState.shutdownMule()
                    }
                })
            } else {
                log.info 'Using JUnit testRunFinished to shut down Mule (on IntellIj or Maven)'
                notifier.addListener(new MuleGroovyShutdownListener())
            }
        }

        super.runChild(method,
                       notifier)
    }

    @Override
    protected TestClass createTestClass(Class<?> testClass) {
        remoteClient ? new ModifiedTestClass(testClass,
                                             channel,
                                             clientHandler) : super.createTestClass(testClass)
    }
}
