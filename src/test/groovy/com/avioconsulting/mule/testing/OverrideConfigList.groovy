package com.avioconsulting.mule.testing

import groovy.json.JsonSlurper

// allow us to avoid the overhead of setting up mule-deploy-props-test-artifact.json for each test
trait OverrideConfigList {
    static Map cachedClassLoaderModel

    File getProjectDirectory() {
        // putting this here because it's a @Before hook on the cheap
        System.setProperty('mule.verbose.exceptions',
                           'true')
        new File('src/test/resources/maven')
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
        def logger = getLogger()
        logger.info 'Using outputdirs {} for test',
                    result
        result
    }

    Map getClassLoaderModel() {
        def logger = getLogger()
        if (!cachedClassLoaderModel) {
            regenerateClassLoaderModelAndArtifactDescriptor()
            def classLoaderModelFile = getClassLoaderModelFile() as File
            assert classLoaderModelFile.exists()
            cachedClassLoaderModel = new JsonSlurper().parse(classLoaderModelFile)
        } else {
            logger.info 'Using cached/static classloader model'
        }
        cachedClassLoaderModel
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