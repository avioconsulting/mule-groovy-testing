package com.avioconsulting.mule.testing.junit

import com.avioconsulting.mule.testing.EnvironmentDetector
import com.avioconsulting.mule.testing.background.ModifiedTestClass
import groovy.util.logging.Log4j2
import org.apache.commons.io.FileUtils
import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.InitializationError
import org.junit.runners.model.TestClass

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermissions

@Log4j2
class MuleGroovyJunitRunner extends
        BlockJUnit4ClassRunner implements EnvironmentDetector {
    static boolean listenerSetup = false
    private boolean remoteClient

    MuleGroovyJunitRunner(Class<?> klass) throws InitializationError {
        super(klass)
        remoteClient = System.getenv('RUN_REMOTE') == '1'
        if (remoteClient) {
            def resource = MuleGroovyJunitRunner.getResource('/muleTestEngine')
            assert resource : 'Have you run the app assembler build?'
            def wrapperDir = new File('.mule',
                                      'wrapper')
            if (!wrapperDir.exists()) {
                wrapperDir.mkdirs()
                FileUtils.copyDirectory(new File(resource.toURI()),
                                        wrapperDir)
                // wrapper won't run without this
                new File(wrapperDir,
                         'logs').mkdirs()
                def binDir = new File(wrapperDir,
                                      'bin')
                def script = new File(binDir,
                                      'muleTestEngine')
                // for some reason the script is not already marked as executable
                def permissions = PosixFilePermissions.fromString('rwxr-xr-x')
                Files.setPosixFilePermissions(script.toPath(),
                                              permissions)
            }
            println 'hi'
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
