package com.avioconsulting.mule.testing.junit

import com.avioconsulting.mule.testing.dsl.invokers.BatchRunner
import org.junit.After

abstract class BaseBatchJunitTest extends BaseJunitTest {
    @After
    void ensureCleanBatchState() {
        if (muleContext) {
            getLogger().info 'Shutting down existing Mule context because batch tests have issues when sharing a context'
            // batch stuff doesn't seem to work well if you reuse contexts
            shutdownMule()
        }
    }

    def runBatch(String batchName,
                 List<String> jobsToWaitFor = null,
                 boolean throwUnderlyingException = false,
                 @DelegatesTo(BatchRunner) Closure closure) {
        runBatch(muleContext,
                 batchName,
                 jobsToWaitFor,
                 throwUnderlyingException,
                 closure)
    }
}
