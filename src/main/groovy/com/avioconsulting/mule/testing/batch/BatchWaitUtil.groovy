package com.avioconsulting.mule.testing.batch

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.mule.api.MuleContext

class BatchWaitUtil {
    private final MuleContext muleContext
    protected static final Logger logger = LogManager.getLogger(BatchWaitUtil)

    BatchWaitUtil(MuleContext muleContext) {
        this.muleContext = muleContext
    }

    def waitFor(List<String> requestedJobsToWaitFor = null,
                boolean throwExceptionOnFailedBatchJob,
                Closure closure) {
        // need to wait for batch thread to finish
        def batchListener = new BatchCompletionListener(requestedJobsToWaitFor)
        def jobsToWaitFor = batchListener.jobsToWaitFor
        muleContext.registerListener(batchListener)
        try {
            closure()
            def getIncompletes = {
                jobsToWaitFor - batchListener.results.keySet()
            }
            while (getIncompletes().any()) {
                logger.info "Still waiting for batch jobs ${getIncompletes()} to finish"
                synchronized (batchListener.results) {
                    // wait 60 seconds
                    batchListener.results.wait() //(60 * 1000)
                }
            }
            def failedJobs = batchListener.results.findAll { ignore, result ->
                result.failedRecords > 0 || result.failedOnCompletePhase
            }.collect { name, result ->
                "Job: ${name}, failed records: ${result.failedRecords} onComplete fail: ${result.failedOnCompletePhase}"
            }
            if (!throwExceptionOnFailedBatchJob) {
                logger.info "Ignoring failed jobs: ${failedJobs} per request"
                return
            }
            assert failedJobs.isEmpty(): "Expected no failed job instances but got ${failedJobs}"
        }
        finally {
            muleContext.unregisterListener(batchListener)
        }
    }
}
