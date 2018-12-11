# 2.0
(for Mule 4.1)

# 2.0.8
* Allow accessing events (for flowVars/attributes) from invoke exception wrapper

# 2.0.7
* Fixes for SOAP/WS-Consumer 1.2.1
* Consistent logging
* Standardize closure currying for XML/REST and SOAP to allow getting connector info in the mock closure
* Add ability to get access to SOAP headers when mocking SOAP calls
* Wrap exceptions thrown from flow invocations to make it easier to assert message contents

# 2.0.6
* Add support for `target` and ensure mocked connectors that have it configured have mocked output sent to the appropriate flow variable. NOTE: `targetValue` is not supported yet
* Add support for HTTP request and WS Consumer's `body` attribute that allows supplying a request from something besides payload
* Allow mocking HTTP request connectors that are hidden behind Exchange/Mule 4 modules.

# 2.0.5
* To avoid possible state issues with the .mule directory, just clear it out with every test run

# 2.0.4
* Windows issue with app names

# 2.0.3
* Be more lenient about Maven paths (if we can resolve via a system mvn path, try that)
* Ensure the mule dependency JSON file for the engine makes it into test-classes if its generated for the first time during our Maven run to build the artifact/classloader descriptor
* Reuse the .mule directory/engine setup when possible to reduce repetitive file copying
* Use simpler REST APIKit invocation convention. The previous one was based on a convention that sort of works against Design Center defaults.
* REST APIKit in Mule 4.x does not require an actual HTTP listener bind port, so removed all of the open port locating code
* Fixed issue that mock interceptor was causing with classloaders that manifested itself on paged connectors

# 2.0.2
* Fix issue with domains (For now, will 'remove' the domain from the app when loading)

# 2.0.1
* Fix issue with no connector projects
* Add ability to stop apps
* Allow changing the artifact name

# 2.0.0
* First release

# 1.0
(for Mule 3.x)

# 1.0.26
* Tolerate an unaltered SOAP request payload (which is javax.xml.stream.XMLStreamReader, was preventing a test from executing successfully)

# 1.0.25
* Fix issue with optional Spring objects when running tests from Studio, which results in the engine's total classpath being available during an Eclipse run

# 1.0.24
* Support mocking flow refs and other factory bean instantiated stuff

# 1.0.23
* Add generic method to mock connectors that do not yet have helper methods

# 1.0.22
* Don't assume anything about the structure of a SOAP fault, let the test user define that

# 1.0.21
* Add support for throwing SOAP faults from SOAP mocks
* Restructure test code to use a simplified mocking model under the hood
* Simplified how query params, path, verb are passed into HTTP request mock closures
* When mocking using XML and JAXB objects (either SOAP or via the HTTP request connector), the unmarshalled/marshalled XML going from the actual Mule flow into the flow is logged

# 1.0.20
* Add convenience code to invoke APIKit router flows from the listener/api-main flow level

# 1.0.19
* Move .mule directory removal to when context is actually created

# 1.0.18
* Remove dependency on MUnit libraries because MUnit was manipulating SOAP payloads and causing test problems AND because we can't depend on this for Mule 4.0 anyways
* Have tests reuse Mule context if possible (speeds up execution)

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
