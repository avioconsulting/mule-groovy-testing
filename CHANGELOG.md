# 1.0.4 (unreleased)
* Batch processor, by default the `runBatch` invoker and the `waitForBatchSuccess` helper methods that you can use to invoke batch processes will now wait for any batch process that has started to complete before returning control to test methods that call it. You can still override the list of processes. 
* You can also requests that the underlying exception from a failed processor in a step be thrown instead of the overall exception for the job failure.

# 1.0.3
* Improve batch testing support (wait for X jobs to complete) 

# 1.0.2
* Instead of requiring you to get the Mule config file list right, derive it from mule-deploy.properties but allow substituting files
* Validate how many records are attempted to be upserted using SFDC connector
* Add basic batch job support

# 1.0.1
* SalesForce testing fixes (upserts are done with lists, add error convenience code)

# 1.0.0

* Initial release