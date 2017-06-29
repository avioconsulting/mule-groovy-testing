# NOTE

The documentation is out of date and needs to be updated. For now, examine the tests for the testing framework itself to see example usages. Here is what's currently supported:

* Invoking flows via JSON/Java
* Invoking APIKit flows via the apikit router
* Mocking SOAP connectors (WS consumer) and handling JAXB marshal/unmarshal
* Mocking RESTful HTTP request calls that either use XML or JSON and handling JAXB/Jackson respectively
* Mocking VM Puts
* Validate content type and HTTP status codes automatically
* Handle streaming payloads (vs. not)
* Limited HTTP connector usage validation (query params, path, verbs, URI params)

What hasn't been done yet/TODOs:

* Currently, you explicitly tell the tests which Mule config files to load as part of your test. It might make more sense to automatically read in all the files in mule-deploy.properties and then allow the test to remove/substitute certain files instead. That way the list of files in mule-deploy.properties is tested (in case you forgot to add one) but can be overridden
* Boilerplate code from queue-error-strategies, how to test that
* Also it might be useful to detect if a filter is used with a transactional listener and if so, fail a test if it's not a message filter and the filter does not use 'onUnAccepted' (acknowledged Mule bug)
* Compare maven dependencies with engine directory and spot loader overrides problems
* Invoking SOAP flows
* Mocking SalesForce/other connector types
* Mocking DB (you probably shouldn't do this anyways, better to spin up a DB in a Docker container if possible)

# Setting up your project


1. Get this dependency in your .m2 repository
2. Add Groovy plugins+dependencies to your pom to allow tests to be compiled:
```xml
<plugin>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.1</version>
    <configuration>
        <compilerId>groovy-eclipse-compiler</compilerId>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>org.codehaus.groovy</groupId>
            <artifactId>groovy-eclipse-compiler</artifactId>
            <version>2.9.2-01</version>
        </dependency>
        <dependency>
            <groupId>org.codehaus.groovy</groupId>
            <artifactId>groovy-eclipse-batch</artifactId>
            <version>2.4.3-01</version>
        </dependency>
    </dependencies>
</plugin>
...
<dependencies>
    <dependency>
        <groupId>org.codehaus.groovy</groupId>
        <artifactId>groovy-all</artifactId>
        <version>2.4.3</version>
        <!-- Doesn't need to be deployed in ZIP -->
        <scope>provided</scope>
    </dependency>
</dependencies>

```
3. Add dependencies on this test framework to your Mule project's pom.xml:
```xml
<dependency>
    <groupId>com.avioconsulting.mule</groupId>
    <artifactId>testing</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```
4. As you go, add respective jaxb2-maven-plugin or jsonschema2pojo-maven-plugin usages to generate sources to use during tests
 
5. Create your test classes (see below for options)

# Test options

All tests should:
* Extend `com.avioconsulting.muletesting.BaseTest` and then implement the traits noted below.
* Implement the `List<String> getConfigResourcesList()` method. This is how the test knows which Mule config files (and thus which flows) to load for your test.
* You can choose to override `getPropertyMap` by returning a Groovy map which includes properties to set. `mule-app.properties` will be implicitly included so this overrides those values.

## Testing a SOAP (APIKit) service

1. Add jaxb2-maven-plugin plugins to your pom.xml to generate sources for requests/responses
2. Implement the `XmlTesting` trait on your JUnit test case, which will require you to provide a JAXB context (for the packages the jaxb2-maven-plugin generates) and a getMockResourcePath implementation for where mock replies to called SOAP services can be found (see below)
3. Run the `runMuleFlowWithXml` method to call the flow (e.g. `operation1:/SOAPTestService/SOAPTestService/api-config`) in your test, passing the JAXBElement object from the ObjectFactory of your request

## Testing a RESTful service

### Direct JSON

For simple tests, this route might be OK. Simply call this from your test:

```groovy
httpPost url: url,
         payload: json,
         contentType: 'application/json; charset=utf-8'
```

### Using objects

TBD: Document how to do this

## Mocking a SOAP service

1. Ensure the jaxb2-maven-plugin in your pom generates Java classes for the SOAP services you are consuming
2. Implement the `XmlTesting` trait on your JUnit test case, which will require you to provide a JAXB context (for the packages the jaxb2-maven-plugin generates) and a getMockResourcePath implementation for where mock replies to called SOAP services can be found
3. Assuming ws-consumer is used and the connector's name is 'Submit to BBG', mock the SOAP call like this before calling the flow under test (if you're not using until-successful, you will need to call mockSoapReply('connector name', boolean untilSuccessful = false):
```groovy
mockSoapReply('Submit to BBG') { message ->
    // message will already be unmarshalled into a Java object
    def casted = (SubmitGetDataRequest) message
    // this is a variable you can assert against later in your test
    uploadedMessages << casted
    // this will be the SOAP reply (from inside the body) relative to the path from the getMockResourcePath method
    return 'filename.xml'
}
```

## Mocking a RESTful service

### That returns JSON

Use `mockRESTPostReply` or `mockRESTGetReply` in a similar fashion to SOAP above.

```groovy
// mockRESTPostReply(String name, Class expectedRequestJsonClass, YieldType yieldType = YieldType.Map, testClosure)
mockRESTPostReply('Create Node in Drupal', DrupalCreateNodeExample) { map ->
    // in this mode, this will be a map representing the JSON
    // if you set yieldType to DeserializedObject, then Jackson will be used to
    // unmarshall the JSON into the object
    uploadedMessages << map
    // This could be improved but for now, this is how you'd do this
    String json = groovy.json.JsonOutput.toJson(mapToReturn)
    def outboundProps = null
    def attachments = null
    def messageProps = [
                    'content-type': 'application/json; charset=utf-8',
                    'http.status': 200
    ]    
    return new DefaultMuleMessage(new org.mule.module.json(json),
                                          messageProps,
                                          outboundProps,
                                          attachments,
                                          this.muleContext) 
}
```

### That returns XML

mockRESTPostReply or GetReply should return something like this:

```groovy
mockRESTPostReply('Create Node in Drupal', DrupalCreateNodeExample) { map ->
    BufferedInputStream stream = getResource(replyFile)
    getXmlMessage(stream, httpStatus)
}
```

## Mocking a VM PUT

TBD: Document this