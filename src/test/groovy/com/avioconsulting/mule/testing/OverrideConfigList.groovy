package com.avioconsulting.mule.testing

// allow us to avoid the overhead of setting up mule-deploy.properties for each test
trait OverrideConfigList {
    Map getClassLoaderModel() {
        [
                version            : '1.0',
                artifactCoordinates: [
                        groupId   : 'com.avioconsulting.mule',
                        artifactId: 'testingframework',
                        version   : '1.0',
                        type      : 'jar',
                        classifier: 'mule-application'
                ],
                dependencies: []
        ]
    }
}