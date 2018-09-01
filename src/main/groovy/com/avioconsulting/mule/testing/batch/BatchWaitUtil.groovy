package com.avioconsulting.mule.testing.batch

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.mule.runtime.core.api.MuleContext

class BatchWaitUtil {
    private final MuleContext muleContext
    protected static final Logger logger = LogManager.getLogger(BatchWaitUtil)

    BatchWaitUtil(MuleContext muleContext) {
        this.muleContext = muleContext
    }

    // useUnderlyingExceptions will ignore the "outer failures" from the batch job and will
    // instead produce the underlying exception that caused the job to fail

    def waitFor(List<String> requestedJobsToWaitFor,
                boolean throwUnderlyingException,
                Closure closure) {
        // need to wait for batch thread to finish
        def batchListener = new BatchCompletionListener(requestedJobsToWaitFor,
                                                        throwUnderlyingException)
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
            if (throwUnderlyingException) {
                assert failedJobs.any(): 'Expected job to fail since throwUnderlyingException=true but it did not!'
                def exceptions = batchListener.exceptions
                assert exceptions.any(): 'Expected to have encountered some exceptions but did not!'
                def exception = exceptions[0]
                if (exceptions.size() > 1) {
                    def others = exceptions[1..-1].collect { e -> e.message }
                    logger.warn "Throwing 1st exception (${exception.message}) but there was more than 1: ${others}"
                }
                throw exception
            }
            assert failedJobs.isEmpty(): "Expected no failed job instances but got ${failedJobs}"
        }
        finally {
            muleContext.unregisterListener(batchListener)
        }
    }
}
