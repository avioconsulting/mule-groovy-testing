package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.dsl.invokers.FlowRunner
import groovy.util.logging.Log4j2
import org.apache.logging.log4j.Logger
import org.junit.AfterClass
import org.junit.Before
import org.mule.api.MuleContext

@Log4j2
class BaseJunitTest implements BaseMuleGroovyTrait {
    protected static MuleContext muleContext

    @Override
    Logger getLogger() {
        this.log
    }

    @Before
    void startMule() {
        if (!muleContext) {
            muleContext = createMuleContext()
            muleContext.start()
        }
    }

    @AfterClass
    static void shutdownMule() {
        if (muleContext.started) {
            muleContext.stop()
            assert muleContext.stopped
            muleContext.dispose()
            assert muleContext.disposed
        }
        muleContext = null
    }

    def runFlow(MuleContext muleContext,
                String flowName,
                @DelegatesTo(FlowRunner) Closure closure) {
        runFlow(muleContext,
                flowName,
                closure)
    }
}
