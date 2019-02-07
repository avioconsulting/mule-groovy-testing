package com.avioconsulting.mule.testing.junit

import com.avioconsulting.mule.testing.muleinterfaces.containers.BaseEngineConfig
import groovy.transform.Immutable

// Mainly exists just to speed up Mule test execution if we already have a context in place
@Immutable
class TestingConfiguration {
    Map startupProperties, classLoaderModel, artifactModel
    // mavenProfiles isn't directly used but since we're using Groovy immutable, it will control the state
    // of the 'deployed app' and cause an undeployment if they change, which is what we want.
    List<String> keepListenersOnForTheseFlows, mavenProfiles
    List<File> outputDirsToCopy
    BaseEngineConfig engineConfig
    String mavenPomPath, repositoryDirectory

    Properties getStartupPropertiesAsJavaUtilProps() {
        // Mule needs every value to be a string
        def allStrings = startupProperties.collectEntries { key, value ->
            [key.toString(), value.toString()]
        }
        new Properties(allStrings)
    }
}
