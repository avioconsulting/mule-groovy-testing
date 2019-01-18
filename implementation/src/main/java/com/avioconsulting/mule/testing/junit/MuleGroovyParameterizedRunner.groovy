package com.avioconsulting.mule.testing.junit

import org.junit.runner.Runner
import org.junit.runner.notification.RunNotifier
import org.junit.runners.Parameterized

class MuleGroovyParameterizedRunner extends
        Parameterized {
    MuleGroovyParameterizedRunner(Class<?> klass) throws Throwable {
        super(klass)
    }

    @Override
    protected void runChild(Runner runner, RunNotifier notifier) {
        // this method is called for every test, so only do this once
        if (!MuleGroovyJunitRunner.listenerSetup) {
            MuleGroovyJunitRunner.listenerSetup = true
            notifier.addListener(new MuleGroovyShutdownListener())
        }
        super.runChild(runner,
                       notifier)
    }
}
