package com.avioconsulting.mule.testing
// allow us to avoid the overhead of setting up mule-deploy-props-test-artifact.json for each test
trait OverrideConfigList {
    boolean isUseVerboseExceptions() {
        true
    }

    Properties getPropertiesForMavenGeneration() {
        new Properties([
                // src/test/resources/maven/pom.xml does not have a version in it. we use this
                // to "populate" it so we can share the value with the artifact descriptor
                // override we have below
                'app.runtime': getMuleVersion()
        ])
    }

    def getMuleVersion() {
        // TODO: hard coded
        '4.1.4'
    }

    Map getMuleArtifactJson() {
        def configs = substituteConfigResources(getConfigResources())
        [
                configs                         : configs,
                secureProperties                : [],
                redeploymentEnabled             : true,
                name                            : 'tests_for_the_test',
                minMuleVersion                  : getMuleVersion(),
                requiredProduct                 : 'MULE_EE',
                classLoaderModelLoaderDescriptor: [
                        id        : 'mule',
                        attributes: [
                                exportedPackages : [
                                        'com.avioconsulting.mule.testing.invocation'
                                ],
                                // if we don't export resources, we can't load them off the classpath
                                exportedResources: [
                                        'log4j2.xml',
                                        'soap/test.wsdl',
                                        'soap/SOAPTest_v1.xsd'
                                ]
                        ]
                ],
                bundleDescriptorLoader          : [
                        id        : 'mule',
                        attributes: [:]
                ]
        ]
    }
}
