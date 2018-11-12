package com.avioconsulting.mule.testing.junit

import com.avioconsulting.mule.testing.EnvironmentDetector
import groovy.util.logging.Log4j2
import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.InitializationError

@Log4j2
class MuleGroovyJunitRunner extends
        BlockJUnit4ClassRunner implements EnvironmentDetector {
    static boolean listenerSetup = false

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
}
