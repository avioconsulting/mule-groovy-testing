package com.avioconsulting.mule.testing.junit

import org.junit.runner.Result
import org.junit.runner.notification.RunListener

class MuleGroovyShutdownListener extends
        RunListener {
    @Override
    void testRunFinished(Result result) throws Exception {
        MuleGroovyJunitRunner.shutdownMule()
    }
}
