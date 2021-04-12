# Summary

This testing framework is a more powerful approach than MUnit. It's opinionated:
* Prefers programming languages over XML for tests
* Test runs from IDEs
* JUnit style approach
* Minimal engine laziness/startup tweaking during test run (just get it over with)
* Mocks should not have to know about target variables (that's implementation)
* Light opinion: `doc:name` is the way to say what you're mocking
* Light opinion: Putting anything besides property loads in `global.xml` is an anti-pattern.

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

Other differences from MUnit:
* Allow validation of HTTP query parameters, path names

What hasn't been done yet/TODOs:

* Calling `sub-flow` elements. Right now if you want to invoke a subflow, you have to create a dummy `<flow>` in `src/test/resources`, import that file, and then invoke the dummy flow (MGTF-10).
* Adding or removing a Mule XML file currently requires re-running `mvn clean test-compile` to generate a new artifact descriptor
* Daemon (under development) - Figure out how to keep engine/app running in the background to speed test execution. Probably the simplest way that could work would be to create a 'gray line' in the middle of the JUnit runner. If it's the background process, it would run the test method. If it's the front end, it would relay the command to run the test method to the backend process. Both sides would need to know about the test method but this is probably simpler than trying to serialize Mulesoft's objects. Would require some class reloading in the daemon process.
* Deal with the style of patch with the infamous July 2019 security issue which involves a runtime patch containing other patches
* `targetValue` support (MGTF-9)
* Automatically detect whether a flow being invoked has an HTTP listener with non-repeatable streams turned on and use a non repeatable stream in that case
* Automatically detect whether an HTTP requester being mocked has non-repeatable streams turned on and use a non repeatable stream in that case
* Invoking SalesForce upsert and query (DQL not supported in Studio 7/Mule 4)
* Easily mock any DQL/Devkit based connector
* Boilerplate code from queue-error-strategies, how to test that
* Mocking DB (you probably shouldn't do this anyways, better to spin up a DB in a Docker container if possible)
* Mocking SFTP processors (NOT listeners) or anything that uses a connection. The underlying Mule interceptor code this framework relies on doesn't prevent Mule from trying to open a connection and if that fails, the mock code is never reached.

# Requirements
* Mule >= 4.3.0
* `mule-apikit-modules` versions from somewhere around 1.2.x up to around 1.3.16 do not work. Framework is tested and works against 1.3.19
* Modern HTTP connector version (>= 1.5.0) if you want to use APIKit invocation

# Setting up your project

1. Get this dependency in your .m2 repository
2. Add Groovy plugins+dependencies to your pom to allow tests to be compiled:
```xml
<properties>
...
    <groovy.compiler.version>3.0.7</groovy.compiler.version>
</properties>
...
<build>
    <!-- This resources section isn't required but it will help IntelliJ, if you choose
         to use IntelliJ, immediately understand the src/main/mule needs to be
        "compiled" to target/classes when it builds the project -->
    <resources>
        <resource>
            <directory>${project.basedir}/src/main/resources</directory>
        </resource>
        <resource>
            <directory>${project.basedir}/src/main/mule</directory>
        </resource>
    </resources>
    <plugins>
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.8.1</version>
            <configuration>
                <compilerId>groovy-eclipse-compiler</compilerId>
            </configuration>
            <dependencies>
                <dependency>
                    <groupId>org.codehaus.groovy</groupId>
                    <artifactId>groovy-eclipse-compiler</artifactId>
                    <version>3.7.0</version>
                </dependency>
                <dependency>
                    <groupId>org.codehaus.groovy</groupId>
                    <artifactId>groovy-eclipse-batch</artifactId>
                    <version>${groovy.compiler.version}-03</version>
                </dependency>
            </dependencies>
        </plugin>
    </plugins>
</build>
<!-- Since Mule 4.x has a different classloading model where the engine's
dependencies are not included in Mule applications, this will resolve all
 of the Mule 4 engine dependencies needed to run tests and put their locations in a file called `mule4_dependencies.json` that the testing
framework will use to load the engine. Will use ${app.runtime} from this project by default to determine what Mule version to use. -->
<plugin>
    <groupId>com.avioconsulting.mule</groupId>
    <artifactId>dependency-resolver-maven-plugin</artifactId>
    <version>1.0.4</version>
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
        <artifactId>groovy-all</artifactId>
        <version>${groovy.compiler.version}</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

3. Add dependencies on this test framework to your Mule project's pom.xml:
```xml
<dependency>
    <groupId>com.avioconsulting.mule</groupId>
    <artifactId>testing</artifactId>
    <version>2.0.47</version>
    <scope>test</scope>
</dependency>
```

4. Add repositories

```xml
        <repositories>
            <!-- some repository that includes this testing lib -->
            <repository>
                <id>avio-releases</id>
                <name>AVIO Releases Repository</name>
                <url>https://devops.avioconsulting.com/nexus/repository/avio-releases/</url>
            </repository>
            <repository>
                <id>avio-mule-ee-releases</id>
                <name>AVIO MuleEE Releases Repository</name>
                <url>https://devops.avioconsulting.com/nexus/repository/mulesoft-ee-releases/</url>
            </repository>
        </repositories>

        <pluginRepositories>
            <!-- some repository that includes the dependency resolver plugin -->
            <pluginRepository>
                <id>avio-releases</id>
                <name>AVIO Releases Repository</name>
                <url>https://devops.avioconsulting.com/nexus/repository/avio-releases/</url>
                <layout>default</layout>
            </pluginRepository>
            <pluginRepository>
                <id>groovy</id>
                <name>Groovy</name>
                <layout>default</layout>
                <url>https://groovy.jfrog.io/artifactory/libs-release-local</url>
                <snapshots>
                    <enabled>false</enabled>
                </snapshots>
            </pluginRepository>
        </pluginRepositories>
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

6. Running - You can use Studio/Eclipse (with Groovy's Eclipse plugin) or IntellIJ. IntelliJ will provide the better experience. If you do use IntelliJ, under Preferences->Build/Execution/Deployment->Build Tools->Maven->Importing, change `Phase to be used for folders update` to `generate-test-resources`. Then whenever you update Maven folders, this will ensure that any generated code AND the `mule4_dependencies.json` file are created inside the `target` directory. 

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

// this will provide a way to control which property values are set
// for your test. non-encrypted and encrypted property elements are not
// automatically excluded by the framework. A common pattern is
// to put all the property loads in 1 XML file (global.xml)
// and then sub out that file as shown above
@Override
Map getStartUpProperties() {
    // you can also load a file here if you want
    [
            'some.username': 'foo',
            'secure::password': 'somepassword'
    ]
}
```

The philosophy here is to rely on the Mule artifact descriptor, which should have the right entries (that's what matters for the real engine) and have test code modify it rather than creating a new set of files to import.

## Actual tests

This needs to be more clearly documented but for now, it might be easiest to look at tests for this project to get an idea of what all you can do in terms of mocks and invocations. Everything supported by the testing framework is tested itself in this project.

A simple flow invocation and mock looks like this:

```groovy
class SomeTest extends BaseJunitTest {
    @Test
    void mockViaMap() {
        // arrange
        def stuff = null
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { Map incoming ->
                    stuff = incoming
                    [reply: 456]
                }
            }
        }

        // act
        def result = runFlow('restRequest') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assertThat stuff,
                   is(equalTo([key: 123]))
        assertThat result,
                   is(equalTo([reply_key: 457]))
    }
}
```

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

1. HTTP listener path like this: `/app-name/api/v1/*` where `app-name` is the value from `getApiNameUnderTest` (you supply this when you extend `BaseApiKitTest`) and `v1` is the value of `getApiVersionUnderTest`.
1. A main flow name (Studio derives this from the RAML filename) like this: `app-name-main`. 

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

# Runtime Patches

You might find yourself in a situation where the runtime itself has a problem that prevents your tests from running properly and Mulesoft has issued a patch for it. To handle that, follow this process:

NOTE: "Meta-patches" like the July 2019 security issue are not supported yet. It would not be hard to adapt to them but just have not spent the time.

1. Download the patch from the Mule website/support case/etc.
2. Deploy the patch to your own Nexus/Artifactory server using a command like this:
`mvn deploy:deploy-file -Dfile=SE-10506-4.1.5.jar -DrepositoryId=avio-releases -Durl=https://devops.avioconsulting.com/nexus/repository/avio-releases -DgroupId=org.mule.patches -DartifactId=SE-10506-4.1.5 -Dversion=1.0`.
`repositoryId` needs to match the `server` ID in `~/.m2/settings.xml` for credential purposes.
3. Ensure the `dependency-resolver-maven-plugin` configuration looks like below. The syntax is `groupId:artifactId:version` you used to deploy in step 2.

```xml
<plugin>
    <groupId>com.avioconsulting.mule</groupId>
    <artifactId>dependency-resolver-maven-plugin</artifactId>
    <version>1.0.2</version>
    <executions>
        <execution>
            <id>generate-dep-graph</id>
            <goals>
                <goal>resolve</goal>
            </goals>
            <phase>generate-test-resources</phase>
            <!-- mulePatches is optional -->
            <configuration>
                <mulePatches>
                    <patch>org.mule.patches:SE-9559-4.1.5:1.0</patch>
                </mulePatches>
            </configuration>
        </execution>
    </executions>
</plugin>
```
