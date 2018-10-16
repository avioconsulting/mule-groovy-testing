package com.avioconsulting.mule.testing.batch

import com.mulesoft.mule.runtime.module.batch.api.BatchJobResult
import com.mulesoft.mule.runtime.module.batch.api.notification.BatchNotification
import com.mulesoft.mule.runtime.module.batch.api.notification.BatchNotificationListener
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.mule.runtime.api.notification.CustomNotification

class BatchCompletionListener implements
        BatchNotificationListener {
    protected static final Logger logger = LogManager.getLogger(BatchCompletionListener)
    private final boolean waitForAllStartedJobs
    private final List<String> jobsToWaitFor
    private final Map<String, BatchJobResult> batchJobResults = [:]
    private final boolean throwUnderlyingException
    private final List<Throwable> exceptions = []
    private static final List<Integer> recordFailedActions = [BatchNotification.STEP_RECORD_FAILED]
    private static final List<Integer> jobCompletedActions = [BatchNotification.ON_COMPLETE_END,
                                                              BatchNotification.ON_COMPLETE_FAILED]
    private final Map<String, Map<String, String>> idMap = [:]

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
    void onNotification(CustomNotification serverNotification) {
        def batchNotification = serverNotification as BatchNotification
        if (throwUnderlyingException && recordFailedActions.contains(batchNotification.action)) {
            // batchNotification.exception is a BatchException containing the real cause
            this.exceptions << batchNotification.exception.cause
            return
        }
        def jobInstance = batchNotification.jobInstance
        def jobDescription = jobInstance.ownerJobName
        if (waitForAllStartedJobs) {
            // if a batch job is called multiple times, we need to able to distinguish each invocation
            def mapForThisJobName = idMap.containsKey(jobDescription) ? idMap[jobDescription] : (idMap[jobDescription] = [:])
            if (!mapForThisJobName.containsKey(jobInstance.id)) {
                mapForThisJobName[jobInstance.id] = "${jobDescription} (invocation ${mapForThisJobName.size()})".toString()
            }
            jobDescription = mapForThisJobName[jobInstance.id]

            if (batchNotification.action.actionId == BatchNotification.LOAD_PHASE_BEGIN) {
                logger.info "Adding '${jobDescription}' to list of batch jobs (${jobsToWaitFor}) we will wait for..."
                jobsToWaitFor << jobDescription
                return
            }
        }
        if (jobCompletedActions.contains(batchNotification.action)) {
            synchronized (batchJobResults) {
                batchJobResults[jobDescription] = jobInstance.result
                batchJobResults.notify()
            }
        }
    }
}
