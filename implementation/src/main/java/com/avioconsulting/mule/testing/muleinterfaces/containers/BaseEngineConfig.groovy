package com.avioconsulting.mule.testing.muleinterfaces.containers

import groovy.transform.Immutable

@Immutable
class BaseEngineConfig {
    List<String> filterEngineExtensions
    boolean verboseExceptions
    static final String ANALYTICS_PLUGIN = 'mule-module-analytics'
    static final List<String> defaultFilters = [
            'mule-module-cluster-ee', // no Hazelcast cluster needed for a unit test
            'mule-module-api-deployment', // don't seem to need this either
            ANALYTICS_PLUGIN, // wouldn't need analytics for a test
            'mule-module-plugin' // don't really need to use plugins for tests either
    ]
}
