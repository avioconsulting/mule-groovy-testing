package com.avioconsulting.mule.testing.batch

import com.avioconsulting.mule.testing.mulereplacements.RuntimeBridgeTestSide
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class BatchWaitUtil {
    private final RuntimeBridgeTestSide bridge
    protected static final Logger logger = LogManager.getLogger(BatchWaitUtil)

    BatchWaitUtil(RuntimeBridgeTestSide bridge) {
        this.bridge = bridge
    }

    // useUnderlyingExceptions will ignore the "outer failures" from the batch job and will
    // instead produce the underlying exception that caused the job to fail

    def waitFor(List<String> requestedJobsToWaitFor,
                boolean throwUnderlyingException,
                Closure closure) {
        // need to wait for batch thread to finish
        def batchListener = bridge.batchNotifyListener
        batchListener.begin(requestedJobsToWaitFor,
                              throwUnderlyingException)
        def jobsToWaitFor = batchListener.jobsToWaitFor
        try {
            closure()
            def getIncompletes = {
                jobsToWaitFor - batchListener.batchJobResults.keySet()
            }
            while (getIncompletes().any()) {
                logger.info "Still waiting for batch jobs ${getIncompletes()} to finish"
                synchronized (batchListener.batchJobResults) {
                    // wait 60 seconds
                    batchListener.batchJobResults.wait() //(60 * 1000)
                }
            }
            def failedJobs = batchListener.batchJobResults.findAll { ignore, result ->
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
            batchListener.end()
        }
    }
}
