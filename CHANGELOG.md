# 1.0.9
* Runs with Mule 3.9.1 by default
* Upgraded from 1.2.1 to 1.3.3 of base MUnit classes

# 1.0.8
* Support building testing framework on Windows

# 1.0.7
* Remove unnecessary EE license dependency

# 1.0.6
* Rename `waitForBatchSuccess` to `waitForBatchCompletion` to be more accurate

# 1.0.5
* Fixed issue where multiple invocations of batch job with the same name were not waited on properly

# 1.0.4
* Batch processor, by default the `runBatch` invoker and the `waitForBatchSuccess` helper methods that you can use to invoke batch processes will now wait for any batch process that has started to complete before returning control to test methods that call it. You can still override the list of processes. 
* You can also requests that the underlying exception from a failed processor in a step be thrown instead of the overall exception for the job failure.
* Sources JAR now included

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