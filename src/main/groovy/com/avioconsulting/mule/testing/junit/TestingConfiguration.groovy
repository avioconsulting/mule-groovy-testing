package com.avioconsulting.mule.testing.junit

import com.avioconsulting.mule.testing.containers.BaseEngineConfig
import groovy.transform.Immutable

// Mainly exists just to speed up Mule test execution if we already have a context in place
@Immutable
class TestingConfiguration {
    Map startupProperties, classLoaderModel, artifactModel
    List<String> keepListenersOnForTheseFlows
    BaseEngineConfig engineConfig
    String mavenPomPath, repositoryDirectory
}
