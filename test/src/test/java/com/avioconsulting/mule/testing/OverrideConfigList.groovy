package com.avioconsulting.mule.testing
// allow us to avoid the overhead of setting up mule-deploy-props-test-artifact.json for each test
trait OverrideConfigList {
    boolean isUseVerboseExceptions() {
        true
    }
}
