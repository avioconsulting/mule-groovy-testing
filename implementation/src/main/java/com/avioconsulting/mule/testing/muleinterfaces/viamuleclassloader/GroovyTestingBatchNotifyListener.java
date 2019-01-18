package com.avioconsulting.mule.testing.muleinterfaces.viamuleclassloader;

import com.mulesoft.mule.runtime.module.batch.api.BatchJobInstance;
import com.mulesoft.mule.runtime.module.batch.api.BatchJobResult;
import com.mulesoft.mule.runtime.module.batch.api.notification.BatchNotification;
import com.mulesoft.mule.runtime.module.batch.api.notification.BatchNotificationListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mule.runtime.api.notification.CustomNotification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroovyTestingBatchNotifyListener implements BatchNotificationListener {
    private static final Logger logger = LogManager.getLogger(GroovyTestingBatchNotifyListener.class);
    private List<String> jobsToWaitFor;
    private final Map<String, BatchJobResult> batchJobResults = new HashMap<>();
    private boolean throwUnderlyingException;
    private boolean enabled;
    private List<Throwable> exceptions;
    private static final List<Integer> recordFailedActions;
    private static final List<Integer> jobCompletedActions;
    private Map<String, Map<String, String>> idMap;
    private boolean waitForAllStartedJobs;

    static {
        recordFailedActions = new ArrayList<>();
        recordFailedActions.add(BatchNotification.STEP_RECORD_FAILED);
        jobCompletedActions = new ArrayList<>();
        jobCompletedActions.add(BatchNotification.ON_COMPLETE_END);
        jobCompletedActions.add(BatchNotification.ON_COMPLETE_FAILED);
    }

    public void begin(List<String> jobsToWaitFor,
                      boolean throwUnderlyingException) {
        this.throwUnderlyingException = throwUnderlyingException;
        this.enabled = true;
        this.exceptions = new ArrayList<>();
        this.batchJobResults.clear();
        this.idMap = new HashMap<>();
        this.waitForAllStartedJobs = jobsToWaitFor == null;
        this.jobsToWaitFor = jobsToWaitFor == null ? new ArrayList<>() : jobsToWaitFor;
    }

    public void end() {
        this.enabled = false;
    }

    public List<String> getJobsToWaitFor() {
        return jobsToWaitFor;
    }

    public Map<String, BatchJobResult> getBatchJobResults() {
        return batchJobResults;
    }

    public List<Throwable> getExceptions() {
        return exceptions;
    }

    @Override
    public void onNotification(CustomNotification serverNotification) {
        if (!enabled) {
            return;
        }
        BatchNotification batchNotification = (BatchNotification) serverNotification;
        int actionId = batchNotification.getAction().getActionId();
        if (throwUnderlyingException && recordFailedActions.contains(actionId)) {
            // batchNotification.exception is a BatchException containing the real cause
            this.exceptions.add(batchNotification.getException().getCause());
            return;
        }
        BatchJobInstance jobInstance = batchNotification.getJobInstance();
        String jobDescription = jobInstance.getOwnerJobName();
        if (waitForAllStartedJobs) {
            idMap.putIfAbsent(jobDescription, new HashMap<>());
            // if a batch job is called multiple times, we need to able to distinguish each invocation
            Map<String, String> mapForThisJobName = idMap.get(jobDescription);
            String jobInstanceId = jobInstance.getId();
            mapForThisJobName.putIfAbsent(jobInstanceId,
                                          String.format("%s (invocation %d)",
                                                        jobDescription,
                                                        mapForThisJobName.size()));
            jobDescription = mapForThisJobName.get(jobInstanceId);
            if (actionId == BatchNotification.LOAD_PHASE_BEGIN) {
                logger.info("Adding '{}' to list of batch jobs (${jobsToWaitFor}) we will wait for...",
                            jobDescription);
                jobsToWaitFor.add(jobDescription);
                return;
            }
        }
        if (jobCompletedActions.contains(actionId)) {
            synchronized (batchJobResults) {
                batchJobResults.put(jobDescription, jobInstance.getResult());
                batchJobResults.notify();
            }
        }
    }
}
