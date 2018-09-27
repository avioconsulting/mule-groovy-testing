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
        if (!cachedClassLoaderModel) {
            def mvnExecutable = SystemUtils.IS_OS_WINDOWS ? 'mvn.cmd' : 'mvn'
            def processBuilder = new ProcessBuilder(mvnExecutable,
                                                    '-f',
                                                    mavenPomPath.absolutePath,
                                                    'clean',
                                                    'compile')
            println "Fetching dependencies that would normally be in project lib using command: ${processBuilder.command()}"
            def process = processBuilder.start()
            process.inputStream.eachLine { println it }
            assert process.waitFor() == 0
            def classLoaderModelFile = new File(testMavenDir, 'target/META-INF/mule-artifact/classloader-model.json')
            assert classLoaderModelFile.exists()
            cachedClassLoaderModel = new JsonSlurper().parse(classLoaderModelFile)
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