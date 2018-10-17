package com.avioconsulting.mule.testing.junit

import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.InitializationError

class MuleGroovyJunitRunner extends
        BlockJUnit4ClassRunner {
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
            notifier.addListener(new MuleGroovyShutdownListener())
        }
        super.runChild(method, notifier)
    }
}
