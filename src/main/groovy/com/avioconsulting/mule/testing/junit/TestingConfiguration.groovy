package com.avioconsulting.mule.testing.junit

import com.avioconsulting.mule.testing.muleinterfaces.containers.BaseEngineConfig
import groovy.transform.Immutable

// Mainly exists just to speed up Mule test execution if we already have a context in place
@Immutable
class TestingConfiguration {
    Map startupProperties, classLoaderModel, artifactModel
    List<String> keepListenersOnForTheseFlows
    List<File> outputDirsToCopy
    BaseEngineConfig engineConfig
    String mavenPomPath, repositoryDirectory
}
