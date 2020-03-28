package com.avioconsulting.mule.testing.junit

import com.avioconsulting.mule.testing.EnvironmentDetector
import com.avioconsulting.mule.testing.background.ModifiedTestClass
import groovy.util.logging.Log4j2
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
    private boolean remoteClient

    MuleGroovyJunitRunner(Class<?> klass) throws InitializationError {
        super(klass)
        remoteClient = System.getenv('RUN_REMOTE') == '1'
        // TODO: Separate method (or even class)
        if (remoteClient) {
            def wrapperDir = new File('.mule',
                                      'wrapper')
            def binDir = new File(wrapperDir,
                                  'bin')
            def logsDir = new File(wrapperDir,
                                   'logs')
            if (!wrapperDir.exists()) {
                println "Wrapper does not yet exist at ${wrapperDir.absolutePath}, building"
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
                def withWrapper = ['lib/wrapper.jar'] + classpaths.toList()
                def lines = withWrapper.withIndex().collect { cp, index ->
                    "wrapper.java.classpath.${index + 1}=${cp}"
                }
                def confDir = new File(wrapperDir,
                                       'etc')
                def wrapperConfFile = new File(confDir,
                                               'wrapper.conf')
                def newWrapperConfText = wrapperConfFile.text.replaceAll(Pattern.compile(/wrapper\.java\.classpath\.\d+=.*/),
                                                                         '')
                newWrapperConfText += lines.join('\n')
                wrapperConfFile.text = newWrapperConfText
                println 'Wrapper setup complete'
            }
            def pidFile = new File(logsDir,
                                   'muleTestEngine.pid')
            if (pidFile.exists()) {
                println "Wrapper already running at PID ${pidFile.text}"
            } else {
                def launch = new File(binDir,
                                      Os.isFamily(Os.FAMILY_WINDOWS) ? 'muleTestEngine.bat' : 'muleTestEngine')
                println "Launching wrapper using ${launch.absolutePath}"
                def process = new ProcessBuilder(launch.absolutePath,
                                                 'start')
                        .redirectErrorStream(true)
                        .start()
                process.inputStream.eachLine { log.info it }
                assert process.waitFor() == 0
                println 'Wrapper launched successfully'
            }
            println 'nope'
        }
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
        remoteClient ? new ModifiedTestClass(testClass) : super.createTestClass(testClass)
    }
}
