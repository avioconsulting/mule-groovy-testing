package com.avioconsulting.mule.testing.muleinterfaces.containers

import groovy.transform.Immutable

@Immutable
class BaseEngineConfig {
    String muleVersion
    List<String> filterEngineExtensions
    static final List<String> defaultFilters = [
            'mule-module-cluster-ee', // no Hazelcast cluster needed for a unit test
            'mule-module-api-deployment', // don't seem to need this either
            'mule-module-analytics' // wouldn't need analytics for a test
    ]
}
