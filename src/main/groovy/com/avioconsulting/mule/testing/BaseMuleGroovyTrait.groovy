package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.batch.BatchWaitUtil
import com.avioconsulting.mule.testing.muleinterfaces.containers.BaseEngineConfig
import com.avioconsulting.mule.testing.muleinterfaces.containers.MuleEngineContainer
import com.avioconsulting.mule.testing.dsl.invokers.*
import com.avioconsulting.mule.testing.dsl.mocking.*
import com.avioconsulting.mule.testing.dsl.mocking.sfdc.Choice
import com.avioconsulting.mule.testing.dsl.mocking.sfdc.ChoiceImpl
import com.avioconsulting.mule.testing.junit.TestingConfiguration
import com.avioconsulting.mule.testing.muleinterfaces.MockingConfiguration
import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.Logger
import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker

import java.security.MessageDigest

// basic idea here is to have a trait that could be mixed in to any type of testing framework situation
// this trait should be stateless
trait BaseMuleGroovyTrait {
    abstract Logger getLogger()

    MuleEngineContainer createMuleEngineContainer(TestingConfiguration testingConfiguration) {
        new MuleEngineContainer(testingConfiguration.engineConfig)
    }

    static File join(Object... paths) {
        assert paths[0] instanceof File
        paths.inject { acc, file ->
            new File(acc, file)
        }
    }

    RuntimeBridgeTestSide deployApplication(MuleEngineContainer muleEngineContainer,
                                            TestingConfiguration testingConfiguration,
                                            MockingConfiguration mockingConfiguration) {
        def muleArtifact = testingConfiguration.artifactModel
        // will ensure some uniqueness
        def artifactName = "${muleArtifact.name}:${testingConfiguration.hashCode()}"
        def appSourceDir = new File(muleEngineContainer.muleHomeDirectory,
                                    artifactName)
        appSourceDir.mkdirs()
        try {
            def metaInfDir = join(appSourceDir, 'META-INF')
            def muleArtifactDir = join(metaInfDir, 'mule-artifact')
            muleArtifactDir.mkdirs()
            def classLoaderFile = join(muleArtifactDir, 'classloader-model.json')
            def classLoaderModel = testingConfiguration.classLoaderModel
            def logger = getLogger()
            def classLoaderModelJson = JsonOutput.prettyPrint(JsonOutput.toJson(classLoaderModel))
            logger.info 'Using classloader model {}',
                        classLoaderModelJson
            classLoaderFile.text = classLoaderModelJson
            def artifactJson = join(muleArtifactDir, 'mule-artifact.json')
            def muleArtifactJson = JsonOutput.prettyPrint(JsonOutput.toJson(muleArtifact))
            logger.info 'Using Mule artifact descriptor {}',
                        muleArtifactJson
            artifactJson.text = muleArtifactJson
            def srcMavenPath = new File(testingConfiguration.mavenPomPath)
            assert srcMavenPath.exists(): "Expected Maven pom @ ${srcMavenPath}."
            def artifactCoordinates = classLoaderModel.artifactCoordinates
            def destMavenPath = join(metaInfDir,
                                     'maven',
                                     artifactCoordinates.groupId,
                                     artifactCoordinates.artifactId)
            destMavenPath.mkdirs()
            FileUtils.copyFileToDirectory(srcMavenPath,
                                          destMavenPath)
            def sourceRepositoryDirectory = new File(testingConfiguration.repositoryDirectory)
            if (sourceRepositoryDirectory.exists()) {
                def targetRepositoryDirectory = join(appSourceDir, 'repository')
                FileUtils.copyDirectory(sourceRepositoryDirectory,
                                        targetRepositoryDirectory)
            }
            testingConfiguration.outputDirsToCopy.each { dir ->
                logger.info 'Copying classpath/output directory {} to {}',
                            dir,
                            appSourceDir
                FileUtils.copyDirectory(dir,
                                        appSourceDir)
            }
            muleEngineContainer.deployApplication(artifactName,
                                                  appSourceDir.toURI(),
                                                  mockingConfiguration,
                                                  testingConfiguration.startupPropertiesAsJavaUtilProps)
        }
        finally {
            FileUtils.deleteDirectory(appSourceDir)
        }
    }

    Map getStartUpProperties() {
        [:]
    }

    BaseEngineConfig getBaseEngineConfig() {
        new BaseEngineConfig('4.1.2')
    }

    List<String> keepListenersOnForTheseFlows() {
        []
    }

    Map<String, String> getConfigResourceSubstitutes() {
        [:]
    }

    File getProjectDirectory() {
        new File('.')
    }

    File getBuildOutputDirectory() {
        new File(projectDirectory, 'target')
    }

    List<File> outputDirsToCopy() {
        def build = buildOutputDirectory
        [
                new File(build, 'classes'),
                new File(build, 'test-classes')
        ]
    }

    File getRepositoryDirectory() {
        new File(buildOutputDirectory, 'repository')
    }

    File getMetaInfDirectory() {
        new File(buildOutputDirectory, 'META-INF')
    }

    File getMuleArtifactDirectory() {
        new File(metaInfDirectory, 'mule-artifact')
    }

    File getSkinnyMuleArtifactDescriptorPath() {
        new File(projectDirectory, 'mule-artifact.json')
    }

    File getMavenPomDirectory() {
        projectDirectory
    }

    File getMavenPomPath() {
        new File(mavenPomDirectory, 'pom.xml')
    }

    File getClassesDirectory() {
        new File(buildOutputDirectory, 'classes')
    }

    def regenerateClassLoaderModelAndArtifactDescriptor() {
        def isMavenRun = System.getProperty('sun.java.command').contains('surefire')
        if (isMavenRun) {
            assert classLoaderModelFile.exists(): "Expected ${classLoaderModelFile} to already exist because we are running from Maven but it does not. Has the Mule Maven plugin run?"
            // the odds are very low that a Maven based run will not have already generated our files
            logger.info 'Skipping classloader model/artifact descriptor regenerate because we are running in Maven'
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
        def allConfigFiles = new FileNameFinder().getFileNames(classesDirectory.absolutePath, '**/*.xml').collect { filename ->
            // relative in case code is moved around on machine
            classesPath.relativize(new File(filename).toPath()).toString()
        } as List<String>
        artifactDescriptorHashMap.configs = allConfigFiles
        def sha256 = hashString(JsonOutput.toJson(artifactDescriptorHashMap))
        def digestFile = new File(buildOutputDirectory, 'mule-artifact.json.sha256')
        def artifactDescFile = new File(muleArtifactDirectory, 'mule-artifact.json')
        def needUpdate = (!digestFile.exists()) || digestFile.text != sha256 || !artifactDescFile.exists()
        if (needUpdate) {
            // if we do an update for the classloader model, our artifact descriptor will already be taken care of
            if (!mavenRunAlreadyDone) {
                def context = artifactDescFile.exists() ? 'has been built but is out of date' :
                        'has not been built'
                // not the cleanest way in the world, but it avoids lots of coupling. and it's more cross platform
                // compatible than direct shell invocation
                logger.info 'Artifact descriptor {}, running maven against POM {} to generate one',
                            context,
                            mavenPomPath
                generateUsingMaven()
            }
            else {
                logger.info 'ClassLoader model already triggered Maven run so no need to run Maven to build artifact descriptor'
            }
            assert artifactDescFile.exists(): 'Somehow we successfully ran a Maven compile but did not generate an artifact descriptor.'
            digestFile.write(sha256)
        } else {
            logger.info 'already up to date artifact descriptor on filesystem'
        }
    }

    private boolean regenerateClassLoaderModel() {
        // our classloader model is pretty closely tied to the POM
        def sha256 = hashString(mavenPomPath.text)
        def digestFile = new File(buildOutputDirectory, 'pom.xml.sha256')
        def classLoaderModelFile = getClassLoaderModelFile() as File
        def needUpdate = (!digestFile.exists()) || digestFile.text != sha256 || !classLoaderModelFile.exists()
        if (needUpdate) {
            def context = classLoaderModelFile.exists() ? 'has been built but is out of date' :
                    'has not been built'
            // not the cleanest way in the world, but it avoids lots of coupling. and it's more cross platform
            // compatible than direct shell invocation
            logger.info 'ClassLoader model descriptor {}, running maven against POM {} to generate one',
                        context,
                        mavenPomPath
            generateUsingMaven()
            assert classLoaderModelFile.exists(): 'Somehow we successfully ran a Maven compile but did not generate a classloader model.'
            digestFile.write(sha256)
        } else {
            logger.info 'already up to date classLoader model on filesystem'
        }
        needUpdate
    }

    private String hashString(String text) {
        def digest = MessageDigest.getInstance('SHA-256')
        digest.update(text.bytes)
        Base64.encoder.encodeToString(digest.digest())
    }

    private void generateUsingMaven() {
        def mavenInvokeRequest = new DefaultInvocationRequest()
        mavenInvokeRequest.setPomFile(mavenPomPath)
        // this will trigger Mule's Maven plugin to populate both mule-artifact.json with all the config files/exports/etc.
        // and generate the classloader model
        mavenInvokeRequest.setGoals(['generate-test-sources'])
        def mavenInvoker = new DefaultInvoker()
        try {
            def result = mavenInvoker.execute(mavenInvokeRequest)
            if (result.exitCode != 0) {
                throw new Exception('Successfully located Maven executable but unable to use Maven to generate classloader model/artifact descriptor. This is likely a problem with your POM or your project. Examine the output for what might be wrong.')
            }
        }
        catch (IllegalStateException e) {
            def exception = new Exception("Unable to call Maven!\nNOTE: This requires a normal Maven install on your machine. You cannot use the version of Maven bundled inside Studio. See below for common IDE instructions.\n---------\nStudio/Eclipse: Window->Preferences->Java->Installed JREs->highlight the JRE->Edit, then paste in -Dmaven.home=yourMavenHomeDirectory into 'Default VM arguments'.\n---------\nIntelliJ: Run Menu->Edit Configurations->Templates->Junit, then paste in -Dmaven.home=yourMavenHomeDirectory into 'VM options'. You might need to re-create any existing Run Configurations.\n---------",
                                          e)
            throw exception
        }
    }

    File getClassLoaderModelFile() {
        new File(muleArtifactDirectory, 'classloader-model.json')
    }

    /**
     * Default implementation
     * @return
     */
    Map getClassLoaderModel() {
        def file = classLoaderModelFile
        assert file.exists(): "Could not find ${file}. Has the Mule Maven plugin built your project yet? If you are not going to create this file yourself, you might want to run regenerateClassLoaderModelAndArtifactDescriptor()"
        new JsonSlurper().parse(file) as Map
    }

    File getMuleArtifactPath() {
        new File(muleArtifactDirectory, 'mule-artifact.json')
    }

    Map getMuleArtifactJson() {
        def file = getMuleArtifactPath()
        assert file.exists(): "Could not find ${file}. Has the Mule Maven plugin built your project yet. If you are not going to create this file, override getMuleArtifactJson"
        def map = new JsonSlurper().parse(file) as Map
        def entirelyDifferentConfigList = getConfigResources()
        if (entirelyDifferentConfigList) {
            map.configs = entirelyDifferentConfigList
        }
        map.configs = substituteConfigResources(map.configs as List<String>)
        map
    }

    List<String> getConfigResources() {
        // if null, framework will use list from artifact descriptor
        null
    }

    List<String> substituteConfigResources(List<String> configs) {
        def subs = getConfigResourceSubstitutes()
        configs.findResults { String entry ->
            subs.containsKey(entry) ? subs[entry] : entry
        }
    }

    def runFlow(RuntimeBridgeTestSide bridge,
                String flowName,
                @DelegatesTo(FlowRunner) Closure closure) {
        def flow = bridge.getFlow(flowName)
        def runner = new FlowRunnerImpl(bridge,
                                        flow,
                                        flowName)
        def code = closure.rehydrate(runner, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        def outputEvent = runFlow(bridge,
                                  flowName,
                                  runner.getEvent())
        runner.transformOutput(outputEvent)
    }

    EventWrapper runSoapApikitFlow(RuntimeBridgeTestSide bridge,
                                   String operation,
                                   String apiKitFlowName = 'api-main',
                                   @DelegatesTo(SoapInvoker) Closure closure) {
        def invoker = new SoapApikitInvokerImpl(bridge,
                                                apiKitFlowName,
                                                operation,
                                                bridge)
        def code = closure.rehydrate(invoker, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        def event = invoker.event
        runFlow(bridge,
                apiKitFlowName,
                event)
    }

    EventWrapper runFlow(RuntimeBridgeTestSide muleContext,
                         String flowName,
                         EventWrapper event) {
        def flow = muleContext.getFlow(flowName)
        flow.process(event)
    }

    def waitForBatchCompletion(RuntimeBridgeTestSide bridge,
                               List<String> jobsToWaitFor = null,
                               boolean throwUnderlyingException = false,
                               Closure closure) {
        def batchWaitUtil = new BatchWaitUtil(bridge)
        batchWaitUtil.waitFor(jobsToWaitFor, throwUnderlyingException, closure)
    }

    def runBatch(RuntimeBridgeTestSide bridge,
                 String batchName,
                 List<String> jobsToWaitFor = null,
                 boolean throwUnderlyingException = false,
                 @DelegatesTo(BatchRunner) Closure closure) {
        def flow = bridge.getFlow(batchName)
        def runner = new FlowRunnerImpl(bridge,
                                        flow,
                                        batchName)
        def code = closure.rehydrate(runner, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        waitForBatchCompletion(bridge,
                               jobsToWaitFor,
                               throwUnderlyingException) {
            runFlow(bridge,
                    batchName,
                    runner.getEvent())
        }
    }

    def mockRestHttpCall(MockingConfiguration mockingConfiguration,
                         RuntimeBridgeTestSide bridge,
                         String connectorName,
                         @DelegatesTo(HttpRequestResponseChoice) Closure closure) {
        def formatterChoice = new HttpRequestResponseChoiceImpl(bridge)
        def code = closure.rehydrate(formatterChoice, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        mockingConfiguration.addMock(connectorName,
                                     formatterChoice.transformer)
    }

    def mockVmReceive(MockingConfiguration mockingConfiguration,
                      RuntimeBridgeTestSide bridge,
                      String connectorName,
                      @DelegatesTo(StandardRequestResponse) Closure closure) {
        def formatterChoice = new VMRequestResponseChoiceImpl()
        def code = closure.rehydrate(formatterChoice, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        mockingConfiguration.addMock(connectorName,
                                     formatterChoice.transformer)
    }

    def mockGeneric(MockingConfiguration mockingConfiguration,
                    RuntimeBridgeTestSide bridge,
                    String connectorName,
                    @DelegatesTo(StandardRequestResponse) Closure closure) {
        def formatterChoice = new GenericRequestResponseChoiceImpl()
        def code = closure.rehydrate(formatterChoice, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        mockingConfiguration.addMock(connectorName,
                                     formatterChoice.transformer)
    }

    def mockSalesForceCall(MockingConfiguration mockingConfiguration,
                           RuntimeBridgeTestSide muleContext,
                           String connectorName,
                           @DelegatesTo(Choice) Closure closure) {
        def choice = new ChoiceImpl(muleContext,
                                    muleContext)
        def code = closure.rehydrate(choice, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        mockingConfiguration.addMock(connectorName,
                                     choice.mock)
    }

    def mockSoapCall(MockingConfiguration mockingConfiguration,
                     RuntimeBridgeTestSide bridge,
                     String connectorName,
                     @DelegatesTo(SOAPFormatter) Closure closure) {
        def soapFormatter = new SOAPFormatterImpl(bridge)
        def code = closure.rehydrate(soapFormatter, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        mockingConfiguration.addMock(connectorName,
                                     soapFormatter.transformer)
    }
}
