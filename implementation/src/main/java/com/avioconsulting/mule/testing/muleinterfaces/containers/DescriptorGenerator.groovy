package com.avioconsulting.mule.testing.muleinterfaces.containers

import com.avioconsulting.mule.testing.EnvironmentDetector
import com.avioconsulting.mule.testing.TestingFrameworkException
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j2
import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker

import java.security.MessageDigest
import java.util.regex.Pattern

@Log4j2
class DescriptorGenerator implements EnvironmentDetector {
    private final File classLoaderModelFile
    private final File skinnyMuleArtifactDescriptorPath
    private final File classesDirectory
    private final File buildOutputDirectory
    private final File muleArtifactDirectory
    private final File mavenPomPath
    private final Properties propertiesForMavenGeneration

    DescriptorGenerator(File classLoaderModelFile,
                        File skinnyMuleArtifactDescriptorPath,
                        File classesDirectory,
                        File buildOutputDirectory,
                        File muleArtifactDirectory,
                        File mavenPomPath,
                        Properties propertiesForMavenGeneration) {
        this.propertiesForMavenGeneration = propertiesForMavenGeneration
        this.mavenPomPath = mavenPomPath
        this.muleArtifactDirectory = muleArtifactDirectory
        this.buildOutputDirectory = buildOutputDirectory
        this.classesDirectory = classesDirectory
        this.skinnyMuleArtifactDescriptorPath = skinnyMuleArtifactDescriptorPath
        this.classLoaderModelFile = classLoaderModelFile
    }

    def regenerateClassLoaderModelAndArtifactDescriptor() {
        if (isRunViaMavenSurefire()) {
            assert classLoaderModelFile.exists(): "Expected ${classLoaderModelFile} to already exist because we are running from Maven but it does not. Has the Mule Maven plugin run?"
            // the odds are very low that a Maven based run will not have already generated our files
            log.info 'Skipping classloader model/artifact descriptor regenerate because we are running in Maven'
            return
        }
        def updated = regenerateClassLoaderModel()
        regenerateArtifactDescriptor(updated)
    }

    // the basic one in source control projects is not enough to run because it does not include the config file listing
    // that is derived by Mule's Maven plugin. To avoid the expensive invocation of that plugin though
    // we can hash the contents of the artifact descriptor we do have (in source control) along with all the config files
    // we can see and get a pretty good idea if the developer either
    // A) added a config file since the last run OR
    // B) changed something in mule-artifact.json (like properties, etc.)
    private void regenerateArtifactDescriptor(boolean mavenRunAlreadyDone) {
        def file = skinnyMuleArtifactDescriptorPath
        assert file.exists(): "Expected your project to contain at least a basic artifact descriptor at ${file}"
        def artifactDescriptorHashMap = new JsonSlurper().parse(file) as Map
        def classesPath = classesDirectory.absoluteFile.toPath()
        def allConfigFiles = new FileNameFinder().getFileNames(classesDirectory.absolutePath,
                                                               '**/*.xml').collect { filename ->
            // relative in case code is moved around on machine
            classesPath.relativize(new File(filename).toPath()).toString()
        } as List<String>
        artifactDescriptorHashMap.configs = allConfigFiles
        def sha256 = hashString(JsonOutput.toJson(artifactDescriptorHashMap))
        def digestFile = new File(buildOutputDirectory,
                                  'mule-artifact.json.sha256')
        def artifactDescFile = new File(muleArtifactDirectory,
                                        'mule-artifact.json')
        def needUpdate = (!digestFile.exists()) || digestFile.text != sha256 || !artifactDescFile.exists()
        if (needUpdate) {
            // if we do an update for the classloader model, our artifact descriptor will already be taken care of
            if (!mavenRunAlreadyDone) {
                def context = artifactDescFile.exists() ? 'has been built but is out of date' :
                        'has not been built'
                // not the cleanest way in the world, but it avoids lots of coupling. and it's more cross platform
                // compatible than direct shell invocation
                log.info 'Artifact descriptor {}, running maven against POM {} to generate one',
                         context,
                         mavenPomPath
                runMaven()
            } else {
                log.info 'ClassLoader model already triggered Maven run so no need to run Maven to build artifact descriptor'
            }
            assert artifactDescFile.exists(): 'Somehow we successfully ran a Maven compile but did not generate an artifact descriptor.'
            digestFile.write(sha256)
        } else {
            log.info 'already up to date artifact descriptor on filesystem'
        }
    }

    private boolean regenerateClassLoaderModel() {
        // our classloader model is pretty closely tied to the POM
        def sha256 = hashString(mavenPomPath.text)
        def digestFile = new File(buildOutputDirectory,
                                  'pom.xml.sha256')
        def needUpdate = (!digestFile.exists()) || digestFile.text != sha256 || !classLoaderModelFile.exists()
        if (needUpdate) {
            def context = classLoaderModelFile.exists() ? 'has been built but is out of date' :
                    'has not been built'
            // not the cleanest way in the world, but it avoids lots of coupling. and it's more cross platform
            // compatible than direct shell invocation
            log.info 'ClassLoader model descriptor {}, running maven against POM {} to generate one',
                     context,
                     mavenPomPath
            runMaven()
            assert classLoaderModelFile.exists(): 'Somehow we successfully ran a Maven compile but did not generate a classloader model.'
            digestFile.write(sha256)
        } else {
            log.info 'already up to date classLoader model on filesystem'
        }
        needUpdate
    }

    private static String hashString(String text) {
        def digest = MessageDigest.getInstance('SHA-256')
        digest.update(text.bytes)
        Base64.encoder.encodeToString(digest.digest())
    }

    private static String getMavenHomeDirectoryFromPath() {
        def process = 'mvn --version'.execute()
        assert process.waitFor() == 0: "Expected mvn --version command to succeed but failed with ${process.err.text}"
        def output = process.text
        def matcher = Pattern.compile(/.*Maven home: (.*?)$.*/,
                                      Pattern.DOTALL | Pattern.MULTILINE).matcher(output)
        assert matcher.matches(): "Expected to find one Maven home entry in output ${output}"
        matcher.group(1)
    }

    private void runMaven() {
        def mavenHome = System.getProperty('maven.home')
        if (!mavenHome && isEclipse()) {
            throw new TestingFrameworkException("\n---------Eclipse/Studio does not make the system path available during JUnit runs and you do not have maven.home configured as a system property so we cannot invoke Maven to generate the descriptor. You cannot use the version of Maven bundled inside Studio. To fix this, go to Window->Preferences->Java->Installed JREs->highlight the JRE->Edit, then paste in -Dmaven.home=yourMavenHomeDirectory into 'Default VM arguments'.\n---------")
        }
        def viaSystemProperty = false
        def attempts = []
        try {
            if (mavenHome) {
                log.info 'Generating using Maven from maven.home system property - {}',
                         mavenHome
                viaSystemProperty = true
                attempts << 'maven.home system property'
            } else {
                attempts << 'mvn executable via system path'
                log.info 'maven.home property was not set, attempting to get mvn from system path'
                mavenHome = getMavenHomeDirectoryFromPath()
                log.info 'Generating using Maven from system mvn path - {}',
                         mavenHome
            }
            try {
                runMavenWithHome(mavenHome)
            }
            catch (e) {
                if (viaSystemProperty) {
                    attempts << 'mvn executable via system path'
                    mavenHome = getMavenHomeDirectoryFromPath()
                    log.info "Caught exception '{}' while using Maven from maven.home system property. This can happen while using IntelliJ's built-in Maven installation. Re-running using maven from system path {}",
                             e.message,
                             mavenHome
                    runMavenWithHome(mavenHome)
                } else {
                    throw e
                }
            }
        }
        catch (e) {
            throw new TestingFrameworkException("Attempted to locate Maven to generate classloader descriptor via these methods but failed! ${attempts}",
                                                e)
        }
    }

    private void runMavenWithHome(String mavenHome) {
        def mavenInvokeRequest = new DefaultInvocationRequest()
        mavenInvokeRequest.setPomFile(mavenPomPath)
        def mavenProps = propertiesForMavenGeneration
        if (mavenProps) {
            mavenInvokeRequest.setProperties(mavenProps)
        }
        // this will trigger Mule's Maven plugin to populate both mule-artifact.json with all the config files/exports/etc.
        // and generate the classloader model
        // we could get that with generate-test-resources but if we do test-compile, we ensure that
        // the dependency resolver maven plugin output is available
        mavenInvokeRequest.setGoals(['test-compile'])
        def mavenInvoker = new DefaultInvoker()
        mavenInvoker.setMavenHome(new File(mavenHome))
        def result = mavenInvoker.execute(mavenInvokeRequest)
        if (result.exitCode != 0) {
            throw new TestingFrameworkException('Successfully located Maven executable but unable to use Maven to generate classloader model/artifact descriptor. This is likely a problem with your POM or your project. Examine the output for what might be wrong.')
        }
    }
}
