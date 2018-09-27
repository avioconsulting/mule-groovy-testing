package com.avioconsulting.mule.testing

import groovy.json.JsonSlurper
import org.apache.commons.lang.SystemUtils

// allow us to avoid the overhead of setting up mule-deploy.properties for each test
trait OverrideConfigList {
    static Map cachedClassLoaderModel

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
        def targetDir = new File(testMavenDir, 'target')
        new File(targetDir, 'repository')
    }

    Map getClassLoaderModel() {
        def logger = getLogger()
        if (!cachedClassLoaderModel) {
            def mvnExecutable = SystemUtils.IS_OS_WINDOWS ? 'mvn.cmd' : 'mvn'
            logger.info 'ClassLoader model has not been built yet, running {} against POM {} to generate one for testing the testing framework',
            mvnExecutable,
            mavenPomPath
            def processBuilder = new ProcessBuilder(mvnExecutable,
                                                    '-f',
                                                    mavenPomPath.absolutePath,
                                                    'clean',
                                                    'compile')
            def process = processBuilder.start()
            process.inputStream.eachLine { println it }
            assert process.waitFor() == 0
            def classLoaderModelFile = new File(testMavenDir, 'target/META-INF/mule-artifact/classloader-model.json')
            assert classLoaderModelFile.exists()
            cachedClassLoaderModel = new JsonSlurper().parse(classLoaderModelFile)
            logger.info 'Parsed classloader model {}',
            cachedClassLoaderModel
        }
        else {
            logger.info 'Using cached/static classloader model'
        }
        cachedClassLoaderModel
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