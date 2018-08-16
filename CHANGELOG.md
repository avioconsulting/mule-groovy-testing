# 1.0.18
* Remove dependency on MUnit libraries because MUnit was manipulating SOAP payloads and causing test problems AND because we can't depend on this for Mule 4.0 anyways

# 1.0.17
* Add ability to gain access to the Mule message within the same closure for XML/SOAP mocks (e.g. whenCalledWithMapAsXml, whenCalledWithGroovyXmlParser, etc.)

# 1.0.16
* Remove our CXF dependency and be dynamic about it such that projects can specify their own version
* Log SOAP invocation payloads

# 1.0.15
* Removed unnecessary maven-plugin-api dependency

# 1.0.14
* Remove SFDC dependency and use reflection to impose a lighter burden
  on test dependencies for consuming projects
* Remove DataWeave compile dependency for the same reason, only used it for testing THIS framework
  
# 1.0.13
* Added basic SOAP invoking support via JAXB

# 1.0.12
* Add raw capability to mock in case you don't want the payload be transformed

# 1.0.11
* Fix issue with subsequent HTTP mock invocations

# 1.0.10
* Add access to HTTP headers in mocks

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