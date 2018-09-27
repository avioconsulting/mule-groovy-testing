package com.avioconsulting.mule.testing

// allow us to avoid the overhead of setting up mule-deploy.properties for each test
trait OverrideConfigList {
    File getTestMavenDir() {
        new File('src/test/resources/maven')
    }

    File getMavenPomPath() {
        new File(testMavenDir, 'pom.xml')
    }

    List<File> getFlowDirectories() {
        [
                new File('src/test/resources')
        ]
    }

    File getRepositoryDirectory() {
        new File(testMavenDir, 'repository')
    }

    Map getClassLoaderModel() {
        // TODO: Run 'mvn clean compile' to generate this from the src/test/resources/maven directory
        [
                version            : '1.0',
                artifactCoordinates: [
                        groupId   : 'com.avioconsulting.mule',
                        artifactId: 'tests_for_the_testingframework',
                        version   : '1.0.0',
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