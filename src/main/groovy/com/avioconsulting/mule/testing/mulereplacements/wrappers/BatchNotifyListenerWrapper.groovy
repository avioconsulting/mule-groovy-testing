package com.avioconsulting.mule.testing.mulereplacements.wrappers

class BatchNotifyListenerWrapper {
    private final Object muleSideListener

    /**
     *
     * @param muleSideListener - com.avioconsulting.mule.testing.mulereplacements.GroovyTestingBatchNotifyListener but we have classpath
     * /reflection stuff to worry about
     */
    BatchNotifyListenerWrapper(Object muleSideListener) {
        this.muleSideListener = muleSideListener
    }

    void begin(List<String> jobsToWaitFor,
               boolean throwUnderlyingException) {
        muleSideListener.begin(jobsToWaitFor, throwUnderlyingException)
    }

    void end() {
        muleSideListener.end()
    }

    List<String> getJobsToWaitFor() {
        muleSideListener.getJobsToWaitFor()
    }

    Map<String, Object> getBatchJobResults() {
        muleSideListener.getBatchJobResults()
    }

    List<Throwable> getExceptions() {
        muleSideListener.getExceptions()
    }
}
