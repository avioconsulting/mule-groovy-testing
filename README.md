# Summary

This testing framework is a more powerful approach than MUnit but sits on top of the base Mule MUnit/Java test classes.

Here is what's currently supported:

* Invoking flows via JSON/Java
* Invoking APIKit flows via the apikit router
* Mocking SOAP connectors (WS consumer) and handling JAXB marshal/unmarshal
* Mocking RESTful HTTP request calls that either use XML or JSON and handling JAXB/Jackson respectively
* Mocking VM Puts
* Validate content type and HTTP status codes automatically
* Handle streaming payloads (vs. not)
* Limited HTTP connector usage validation (query params, path, verbs, URI params)
* Invoking SalesForce upsert and query
* Easily mock any DQL/Devkit based connector
* Automatically loads Mule config files from mule-deploy.properties but allows substituting

Differences from MUnit:
* Full power of Groovy language
* Allow validation of HTTP query parameters, path names
* Allow validation of DQL based queries

What hasn't been done yet/TODOs:

* Boilerplate code from queue-error-strategies, how to test that
* Also it might be useful to detect if a filter is used with a transactional listener and if so, fail a test if it's not a message filter and the filter does not use 'onUnAccepted' (acknowledged Mule bug)
* Compare maven dependencies with engine directory and spot loader overrides problems
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
    <version>1.0.13</version>
    <scope>test</scope>
</dependency>
```
4. As you go, add respective jaxb2-maven-plugin or jsonschema2pojo-maven-plugin usages to generate sources to use during tests
 
5. Create your test classes

**NOTE:** If you do much SOAP work in your project, XMLBeans, which WS-Consumer/Mule uses, can have performance issues with some schema/WSDL combinations when JVM assertions are enabled. This problem does not show up with pure MUnit because MUnit does not rely on JUnit test runs via Surefire but this approach does. You might want to tell Surefire to not enable assertions (which it does by default) using the following plugin snippet:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>2.22.0</version>
    <configuration>
        <!-- XMLBeans and some schemas have performance issues when running tests with assertions enabled -->
        <enableAssertions>false</enableAssertions>
    </configuration>
</plugin>
```

# Test Classes

## Loading

You probably will want to create a base class for your project that extends the `BaseTest` class from this project. If you want to exclude any Mule config files from mule-deploy.properties, you should override the `getConfigResourceSubstitutes` method like shown below.

```groovy
@Override
Map<String, String> getConfigResourceSubstitutes() {
    // this example will prevent global.xml from loading and load global-test.xml instead
    // it will also prevent foo.xml from loading and will load nothing in its place
    ['global.xml': 'global-test.xml',
     'foo.xml'   : null]
}
```

The philosophy here is to 'test' `mule-deploy.properties` to ensure it has the right entries (that's what matters for the real engine) and have test code modify it rather than creating a new set of files to import.

## Actual tests

This needs to be more clearly documented but for now, it might be easiest to look at tests for this project to get an idea of what all you can do in terms of mocks and invocations. Everything supported by the testing framework is tested itself in this project.

# Apikit

## Overview

The framework also is APIkit friendly. The reasons you might want to test this are:

1. Ensures your RAML is valid (apikit router will attempt to use it)
2. Ensures generated flows (e.g. post:/.....) exist like you think they do
3. Provides an "end to end" test

The value add of this framework to achieve that is:

1. An active HTTP listener needs to "exist" in order for the APIKit router to work. This test framework will automatically find an open port and configure the listener.
2. Sets properties that the HTTP listener normally would set (like HTTP host, path, etc.) such that the APIKit router can route a flow invocation properly
3. Invoked the "main" flow containing the router

## Assumptions

1. Your HTTP listeners' config-ref is set to `${http.listener.config}`
1. HTTP listener config is in `global.xml`. If not, this can be overriden (see `getConfigResourceSubstitutes` in `BaseApiKitTest`)
1. HTTP listener path like this: `/app-name/api/v1/*` where `app-name` is the value from `getApiNameUnderTest` (you supply this when you extend `BaseApiKitTest`) and `v1` is the value of `getApiVersionUnderTest`.
1. A main flow name (Studio derives this from the RAML filename) like this: `api-app-name-v1-main`. 

## Usage

Have your tests extend from `BaseApiKitTest` instead of `BaseTest`. This will require you implement some abstract methods for API name, version, etc.

Invoke like this:
```groovy
runApiKitFlow('PATCH', '/mappings') {
    json {
        inputOnly(mappings)
    }
    withOutputHttpStatus { Integer actualStatus ->
        httpStatus = actualStatus
    }
}
```