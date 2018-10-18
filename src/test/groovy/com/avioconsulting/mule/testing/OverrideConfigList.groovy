package com.avioconsulting.mule.testing
// allow us to avoid the overhead of setting up mule-deploy-props-test-artifact.json for each test
trait OverrideConfigList {
    File getProjectDirectory() {
        // putting this here because it's a @Before hook on the cheap
        System.setProperty('mule.verbose.exceptions',
                           'true')
        new File('src/test/resources/maven')
    }

    File getClassesDirectory() {
        // this is used to get a list of possible config files for detecting whether we need to have Mule's Maven plugin build
        // a new artifact descriptor. Since this is not a traditional project, our
        // config files aren't copied around the build cycle as much. We'll just use where all of our files are
        new File('src/test/resources')
    }

    List<File> outputDirsToCopy() {
        def getResourcePath = { Class klass, String file ->
            def resource = klass.getResource(file)
            assert resource
            new File(resource.toURI()).toPath().parent.toFile()
        }
        def srcResourcesPath = getResourcePath(BaseMuleGroovyTrait,
                                               '/global-test.xml')
        def tstResourcesPath = getResourcePath(OverrideConfigList,
                                               '/http_test.xml')
        def result = [
                srcResourcesPath,
                tstResourcesPath
        ]
        result
    }

    Map getMuleArtifactJson() {
        def configs = substituteConfigResources(getConfigResources())
        [
                configs                         : configs,
                secureProperties                : [],
                redeploymentEnabled             : true,
                name                            : 'tests_for_the_test',
                minMuleVersion                  : '4.1.2',
                requiredProduct                 : 'MULE_EE',
                classLoaderModelLoaderDescriptor: [
                        id        : 'mule',
                        attributes: [
                                exportedPackages : [],
                                // if we don't export resources, we can't load them off the classpath
                                exportedResources: [
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