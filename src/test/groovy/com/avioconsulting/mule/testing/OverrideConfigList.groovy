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
                dependencies       : []
        ]
    }

    Map getMuleArtifactJson() {
        [
                configs                         : getConfigResources(),
                secureProperties                : [],
                redeploymentEnabled             : true,
                name                            : 'tests_for_the_test',
                minMuleVersion                  : '4.1.2',
                requiredProduct                 : 'MULE_EE',
                classLoaderModelLoaderDescriptor: [
                        id        : 'mule',
                        attributes: [:]
                ],
                bundleDescriptorLoader          : [
                        id        : 'mule',
                        attributes: [:]
                ]
        ]
    }
}