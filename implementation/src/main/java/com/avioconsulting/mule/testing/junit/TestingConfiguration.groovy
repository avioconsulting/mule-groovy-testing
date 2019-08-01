package com.avioconsulting.mule.testing.junit

import com.avioconsulting.mule.testing.muleinterfaces.containers.BaseEngineConfig
import com.avioconsulting.mule.testing.muleinterfaces.containers.Dependency
import groovy.transform.Immutable

// Mainly exists just to speed up Mule test execution if we already have a context in place
@Immutable
class TestingConfiguration {
    Map startupProperties, classLoaderModel, artifactModel
    // mavenProfiles isn't directly used but since we're using Groovy immutable, it will control the state
    // of the 'deployed app' and cause an undeployment if they change, which is what we want.
    List<String> keepListenersOnForTheseFlows, mavenProfiles
    List<Dependency> dependenciesToFilter = []
    List<File> outputDirsToCopy
    BaseEngineConfig engineConfig
    String mavenPomPath, repositoryDirectory, mavenSettingsFilePath
    boolean lazyConnections, lazyInit, lazyInitXmlValidations, generateXmlSchemas

    Properties getStartupPropertiesAsJavaUtilProps() {
        def allStrings = ([
                // see org.mule.runtime.core.api.config.MuleDeploymentProperties
                'mule.application.deployment.lazyConnections'              : lazyConnections,
                'mule.application.deployment.lazyInit'                     : lazyInit,
                'mule.application.deployment.lazyInit.enableXmlValidations': lazyInitXmlValidations
        ] + startupProperties).collectEntries { key, value ->
            // Mule needs every value to be a string, doesn't like booleans that are not string
            [key.toString(), value.toString()]
        }
        new Properties(allStrings)
    }
}
