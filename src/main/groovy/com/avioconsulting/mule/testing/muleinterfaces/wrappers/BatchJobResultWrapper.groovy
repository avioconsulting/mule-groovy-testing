package com.avioconsulting.mule.testing.muleinterfaces.wrappers

class BatchJobResultWrapper {
    /**
     *
     * @param batchJobResult - com.mulesoft.mule.runtime.module.batch.api.BatchJobResult but we have
     * reflection to worry about
     */
    private final Object batchJobResult

    BatchJobResultWrapper(Object batchJobResult) {
        this.batchJobResult = batchJobResult
    }

    long getFailedRecords() {
        this.batchJobResult.getFailedRecords()
    }

    boolean isFailedOnCompletePhase() {
        this.batchJobResult.isFailedOnCompletePhase()
    }
}
