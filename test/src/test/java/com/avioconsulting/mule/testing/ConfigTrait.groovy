package com.avioconsulting.mule.testing

import java.util.regex.Pattern

// allow us to avoid the overhead of setting up mule-deploy-props-test-artifact.json for each test
trait ConfigTrait {
    boolean isUseVerboseExceptions() {
        true
    }

    // maven within maven within maven/jenkins
    String getMavenSettingsFilePath() {
        // We're running Maven from within Maven and we will not have necessarily retrieved all of the dependencies of
        // the test Mule project. We need credentials (and thus a settings file to do that). Locally it's no problem.
        // on Jenkins, we need to find out the settings file the Jenkinsfile has setup. The Jenkins Maven Pipeline
        // plugin sets the --settings param in the MAVEN_CONFIG environment variable
        def settingsMatcher = Pattern.compile(/.*--settings "(.*?)".*/).matcher(System.getenv('MAVEN_CONFIG') ?: '')
        settingsMatcher.matches() ? settingsMatcher.group(1) : null
    }
}
