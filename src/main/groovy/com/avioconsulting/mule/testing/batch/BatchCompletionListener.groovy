package com.avioconsulting.mule.testing.batch

import com.mulesoft.module.batch.api.BatchJobResult
import com.mulesoft.module.batch.api.notification.BatchNotification
import com.mulesoft.module.batch.api.notification.BatchNotificationListener
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.mule.api.context.notification.ServerNotification

class BatchCompletionListener implements BatchNotificationListener {
    protected static final Logger logger = LogManager.getLogger(BatchCompletionListener)
    private final boolean waitForAllStartedJobs
    private final List<String> jobsToWaitFor
    private final Map<String, BatchJobResult> batchJobResults = [:]
    private final boolean throwUnderlyingException
    private final List<Throwable> exceptions = []

    BatchCompletionListener(List<String> jobsToWaitFor,
                            boolean throwUnderlyingException) {
        this.throwUnderlyingException = throwUnderlyingException
        this.waitForAllStartedJobs = jobsToWaitFor == null
        logger.info waitForAllStartedJobs ? 'Will wait for all started batch jobs before returning control' :
                            "Will wait for these specific batch jobs to finish before returning control: ${jobsToWaitFor}"
        this.jobsToWaitFor = jobsToWaitFor ?: []
    }

    Map<String, BatchJobResult> getResults() {
        batchJobResults
    }

    List<String> getJobsToWaitFor() {
        this.jobsToWaitFor
    }

    List<Exception> getExceptions() {
        this.exceptions
    }

    @Override
    void onNotification(ServerNotification serverNotification) {
        def batchNotification = serverNotification as BatchNotification
        if (batchNotification.action == BatchNotification.STEP_RECORD_FAILED && throwUnderlyingException) {
            // batchNotification.exception is a BatchException containing the real cause
            this.exceptions << batchNotification.exception.cause
            return
        }
        if (batchNotification.action == BatchNotification.INPUT_PHASE_BEGIN && waitForAllStartedJobs) {
            def jobName = batchNotification.jobInstance.ownerJobName
            logger.info "Adding '${jobName}' to list of batch jobs we will wait for..."
            jobsToWaitFor << jobName
            return
        }
        if (batchNotification.action == BatchNotification.ON_COMPLETE_END ||
                batchNotification.action == BatchNotification.ON_COMPLETE_FAILED) {
            synchronized (batchJobResults) {
                def jobInstance = batchNotification.jobInstance
                batchJobResults[jobInstance.ownerJobName] = jobInstance.result
                batchJobResults.notify()
            }
        }
    }
}
