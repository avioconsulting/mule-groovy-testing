# 2.0
(for Mule 4.x)

# 2.0.48 (under developmet)
* 4.3.0 compatibility - note that mocking operations XML SDK (e.g. generated Exchange API connectors) has problems right now (see ApiMockTest)
* Another problem with any 2.0.x version of the testing framework right now is it does not work properly with the newer versions of the HTTP connector (1.5) and APIKit module (e.g. 1.3.7). Newer HTTP connector versions changed the attributes class which causes problems when invoking the APIKit router flow using `BaseApikitTest`. APIKit has introduced a `ramlHandler` concept and something is going wrong in the init cycle whether the apikit `Configuration` class is not having its `initialise` method called and then when the `Router.initialize` method runs, the `getType()` call to config fails on an NPE.

# 2.0.47
* Fixed issue with try scope and quotes in XML
* Fixed foreach/XML SDK issue

# 2.0.46
* Using this on lean/non apikit projects was missing a `commons-io` dependency. fixed that.

# 2.0.45
* Removed call to Maven to generate classloader model/artifact descriptor. Framework now assumes you're either running tests in Maven already OR if you're in an IDE, you've "bootstrapped" the IDE by running `mvn clean test-compile` first.

# 2.0.44
* BREAKING change: The `uri` property on `HttpRequesterInfo` now only returns the path, not the host and port
* Prevent descriptor generation from failing due to Maven snapshot stubbornness

# 2.0.43
* Previous changes went too far and disabled output event support too
* Fix issue with tests continuing to run when we have a showstopper type failure in the mvn/descriptor generate code (e.g. maven auth problems)

# 2.0.42
* Fix issue where using `runApiKitFlow` in `BaseApiKitTest` did not allow you to customize headers

# 2.0.41 (limited release)
* Fix regression with soap mocking in 4.2.2

# 2.0.40
* Allow specifying Maven settings for classloader model/artifact descriptor generation

# 2.0.39
* Fix issues with schema gen

# 2.0.38
* Allow turning on XML schema generation without changing code using `-Davio.groovy.test.generate.xml.schemas=true`

# 2.0.37
* BETA of add `isGenerateXmlSchemas` (false/off by default) that will dump generated XML schemas from Mule extensions and core into `.mule/schemas_from_testing_framework`

# 2.0.36
* Upgrade `slf4j-api` dependency to 1.7.5 from 1.7.25 and explicitly specify a log4j2-slf4j-impl version that works with our version of log4j2. Should reduce collisions in projects

# 2.0.35
* Fix issue with `ReturnWrapper` and raw

# 2.0.34
* 4.2.0 compatibility

# 2.0.33
* Fix how errors are handled in terms of error payload and original payload when mocks throw errors
* Streamline HTTP/SOAP mock error handling
* When setting status code in HTTP request mocks, `setHttpReturnCode` has now become `setHttpStatusCode`

# 2.0.32
* Fix HTTP mock response attribute support

# 2.0.31
* Expose flow `start()` and `stop()` methods

# 2.0.30
* When invoking flows with `java {}` and no MIME type is provided, use `application/java` as a default value

# 2.0.29
* Fix issue where SOAPKit style invocation responses that include `body: {} write "application/xml"` were not handled properly. Now that style is encouraged and a warning is issued if you don't do that

# 2.0.28
* Fix issue with mocking with `raw` and media/data types
* When figuring out the mocked connector type, use a more robust approach (component identifier) rather than hints
* Fix how non apikit `soap` invocations work to match the same nested `body` attribute that apikit puts things under
* Fix SOAP invocation/mocking to use repeatable streams and work if accessed twice

# 2.0.27
* Don't choke when mocking connectors that use connections

# 2.0.26
* Add ability to apply patches supplied via the `mulePatches` configuration setting in the `dependency-resolver-maven-plugin` Maven plugin
# 2.0.25
* Added `runSoapApikitFlowJaxbResultBody` method for unmarshalling SOAP body in response when invoking SOAP apikit flows using SOAPAction

# 2.0.24
* In case Maven profiles are not enough, add overrideable `getDependenciesToFilter()` method that allows you to filter dependencies from the classloader model during testing

# 2.0.23
* Display configured properties when app under test is deployed

# 2.0.22
* Allow changing the simulated HTTP host header when invoking a SOAP apikit flow
* Convenience method (`instantiateJavaClassWithAppClassLoader`) for instantiating Java classes using the app's classloader

# 2.0.21
* Add `correlationId` getter support to `EventWrapper`
* Allow controlling Mule startup better from `BaseJunitTest`

# 2.0.20
* Make `OpenPortLocator` a shareable trait
* Allow testing mime types set on HTTP requester mocks

# 2.0.19
* Fix SOAP apikit invocation problem with WSDL location
* Carve out `StreamUtils.withCursorAsList` helper
* Make `raw` formatting with mocks more consistent
* Fix HTTP requester `body` support with raw mock formatting
* Batch/convenience code for waiting for completion without running directly

# 2.0.18
* Add ability to specify Maven profiles when the testing framework generates a classloader model. This is useful if you need to add a dependency that's solely used during testing

# 2.0.17
* Display testing framework version on startup

# 2.0.16
* SOAP mock now behaves likes real engine when custom transports are used. HTTP 500 messages will now 'hide' the SOAP fault and present themselves more like an HTTP exception.

# 2.0.15
* Fix typo in `StreamUtils.withCursorAsText` and improve code reuse there

# 2.0.14
* Fix issue with try scope, API module, and error handler

# 2.0.13
* Fix issue with try scope and API modules

# 2.0.12
* Fix issue with HTTP requestor validation (mimic actual Mule behavior of validating against 200/201 when None is selected)

# 2.0.11
* Added ability to add additional config resources without using awkward sub setup - see `getAdditionalConfigResources()`
* Add ability to flow invoke with a media type with a Java payload
* Add `StreamUtils` for working with cursors

# 2.0.10
* REQUIRES `dependency-resolver-maven-plugin` version >= 1.0.2
* Further decouple testing framework from Maven be removing ~/.m2 coupling. Let `dependency-resolver-maven-plugin` handle that.

# 2.0.9
* SOAP fault mocking fixes

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
