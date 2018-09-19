package com.avioconsulting.mule.testing.junit

import groovy.transform.Immutable

// Mainly exists just to speed up Mule test execution if we already have a context in place
@Immutable
class TestingConfiguration {
    Map startupProperties
    List<String> configResources
    List<String> keepListenersOnForTheseFlows
}
