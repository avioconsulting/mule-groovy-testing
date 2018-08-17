package com.avioconsulting.mule.testing

// allow us to avoid the overhead of setting up mule-deploy.properties for each test
trait OverrideConfigList {
    abstract List<String> getConfigResourcesList()

    String getConfigResources() {
        configResourcesList.join ','
    }
}