package com.avioconsulting.mule.testing.junit

import groovy.util.logging.Log4j2
import org.junit.runner.Result
import org.junit.runner.notification.RunListener

@Log4j2
class MuleGroovyShutdownListener extends
        RunListener {
    @Override
    void testRunFinished(Result result) throws Exception {
        log.info 'End of test run, shutting down Mule'
        BaseJunitTest.testState.shutdownMule()
    }
}
