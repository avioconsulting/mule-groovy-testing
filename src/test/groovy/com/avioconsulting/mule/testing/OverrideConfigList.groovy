package com.avioconsulting.mule.testing

// allow us to avoid the overhead of setting up mule-deploy.properties for each test
trait OverrideConfigList {
    abstract List<String> getConfigResourcesList()

    String getConfigResources() {
        def directory = new File('.mule')
        println "Checking for .mule directory at ${directory.absolutePath}"
        if (directory.exists()) {
            println "Removing ${directory.absolutePath}"
            directory.deleteDir()
        }
        configResourcesList.join ','
    }
}