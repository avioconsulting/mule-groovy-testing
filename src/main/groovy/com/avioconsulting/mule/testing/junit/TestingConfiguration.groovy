package com.avioconsulting.mule.testing.junit

import groovy.transform.Immutable

// Mainly exists just to speed up Mule test execution if we already have a context in place
@Immutable
class TestingConfiguration {
    Map startupProperties
    // TODO: Subs, maven, classloader model??
    List<String> configResources
    List<String> keepListenersOnForTheseFlows
}
