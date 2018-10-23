# Summary

This testing framework is a more powerful approach than MUnit.

Here is what's currently supported:

* Invoking flows via JSON/Java
* Invoking SOAP and REST APIKit flows using the router
* Mocking SOAP connectors (WS consumer) and handling JAXB marshal/unmarshal
* Mocking RESTful HTTP request calls that either use XML or JSON and handling JAXB/Jackson respectively
* Mocking VM Puts
* Validate HTTP status codes automatically
* Call flows with non-repeatable streams and instruct HTTP request mocks with JSON to use non-repeatable streams
* Limited HTTP connector usage validation (query params, path, verbs, URI params)
* Automatically loads Mule config files from from the artifact descriptor (mule-artifact.json) that Mule derives, but allows substituting

Differences from MUnit:
* Full power of Groovy/Java language
* Allow validation of HTTP query parameters, path names
* Allow validation of DQL based queries

What hasn't been done yet/TODOs:

* Automatically detect whether a flow being invoked has an HTTP listener with non-repeatable streams turned on and use a non repeatable stream in that case
* Automatically detect whether an HTTP requester being mocked has non-repeatable streams turned on and use a non repeatable stream in that case
* Invoking SalesForce upsert and query (DQL not supported in Studio 7 yet)
* Easily mock any DQL/Devkit based connector
* Boilerplate code from queue-error-strategies, how to test that
* Mocking DB (you probably shouldn't do this anyways, better to spin up a DB in a Docker container if possible)

# Setting up your project


1. Get this dependency in your .m2 repository
2. Add Groovy plugins+dependencies to your pom to allow tests to be compiled:
```xml
<properties>
...
    <groovy.compiler.version>2.4.15</groovy.compiler.version>
</properties>
...
<plugin>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.6.1</version>
    <configuration>
        <compilerId>groovy-eclipse-compiler</compilerId>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>org.codehaus.groovy</groupId>
            <artifactId>groovy-eclipse-compiler</artifactId>
            <version>3.0.0-01</version>
        </dependency>
        <dependency>
            <groupId>org.codehaus.groovy</groupId>
            <artifactId>groovy-eclipse-batch</artifactId>
            <version>${groovy.compiler.version}-02</version>
        </dependency>
    </dependencies>
</plugin>
<!-- Since Mule 4.x has a different classloading model, this will resolve all of the Mule 4 engine dependencies
needed to run tests outside of the project AND outside of the testing framework itself. Will use
${app.runtime} from this project by default -->
<plugin>
    <groupId>com.avioconsulting.mule</groupId>
    <artifactId>dependency-resolver-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <id>generate-dep-graph</id>
            <goals>
                <goal>resolve</goal>
            </goals>
            <phase>generate-test-resources</phase>
        </execution>
    </executions>
</plugin>
...
<dependencies>
    <dependency>
        <groupId>org.codehaus.groovy</groupId>
        <artifactId>${groovy.compiler.version}</artifactId>
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
    <version>2.0.0</version>
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

You probably will want to create a base class for your project that extends the `BaseJunitTest` class from this project. If you want to exclude any Mule config files from mule-deploy.properties, you should override the `getConfigResourceSubstitutes` method like shown below.

```groovy
@Override
Map<String, String> getConfigResourceSubstitutes() {
    // this example will prevent global.xml from loading and load global-test.xml instead
    // it will also prevent foo.xml from loading and will load nothing in its place
    ['global.xml': 'global-test.xml',
     'foo.xml'   : null]
}
```

The philosophy here is to rely on the Mule artifact descriptor, which should have the right entries (that's what matters for the real engine) and have test code modify it rather than creating a new set of files to import.

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

Have your tests extend from `BaseApiKitTest` instead of `BaseJunitTest`. This will require you implement some abstract methods for API name, version, etc.

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