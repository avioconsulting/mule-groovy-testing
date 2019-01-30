package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.batch.BatchWaitUtil
import com.avioconsulting.mule.testing.dsl.invokers.*
import com.avioconsulting.mule.testing.dsl.mocking.*
import com.avioconsulting.mule.testing.dsl.mocking.sfdc.Choice
import com.avioconsulting.mule.testing.dsl.mocking.sfdc.ChoiceImpl
import com.avioconsulting.mule.testing.junit.TestingConfiguration
import com.avioconsulting.mule.testing.muleinterfaces.MockingConfiguration
import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.muleinterfaces.containers.BaseEngineConfig
import com.avioconsulting.mule.testing.muleinterfaces.containers.DescriptorGenerator
import com.avioconsulting.mule.testing.muleinterfaces.containers.MuleEngineContainer
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.Logger

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
            new File(acc,
                     file)
        }
    }

    // will be what shows up in app.name/MuleConfiguration.getId(), etc.
    String getUniqueArtifactName(TestingConfiguration testingConfiguration) {
        def muleArtifact = testingConfiguration.artifactModel
        def artifactName = muleArtifact.name as String
        assert artifactName.findAll(':').size() == 2: "Expected mule artifact name '${artifactName}' to be of format groupId:artifactName:version"
        def parts = artifactName.split(':')
        def nameOnly = parts[1]
        // ensure some uniqueness
        "${nameOnly}-${testingConfiguration.hashCode()}"
    }

    RuntimeBridgeTestSide deployApplication(MuleEngineContainer muleEngineContainer,
                                            TestingConfiguration testingConfiguration,
                                            MockingConfiguration mockingConfiguration) {
        def artifactName = getUniqueArtifactName(testingConfiguration)
        def appSourceDir = new File(muleEngineContainer.muleHomeDirectory,
                                    artifactName)
        appSourceDir.mkdirs()
        try {
            def metaInfDir = join(appSourceDir,
                                  'META-INF')
            def muleArtifactDir = join(metaInfDir,
                                       'mule-artifact')
            muleArtifactDir.mkdirs()
            def classLoaderFile = join(muleArtifactDir,
                                       'classloader-model.json')
            def classLoaderModel = testingConfiguration.classLoaderModel
            def logger = getLogger()
            classLoaderModel = filterDomainFromClassLoaderModel(classLoaderModel)
            def classLoaderModelJson = JsonOutput.prettyPrint(JsonOutput.toJson(classLoaderModel))
            logger.debug 'Using classloader model {}',
                         classLoaderModelJson
            classLoaderFile.text = classLoaderModelJson
            def artifactJson = join(muleArtifactDir,
                                    'mule-artifact.json')
            def muleArtifact = testingConfiguration.artifactModel
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
                def targetRepositoryDirectory = join(appSourceDir,
                                                     'repository')
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

    boolean isUseVerboseExceptions() {
        false
    }

    BaseEngineConfig getBaseEngineConfig() {
        new BaseEngineConfig(BaseEngineConfig.defaultFilters,
                             isUseVerboseExceptions())
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
        new File(projectDirectory,
                 'target')
    }

    List<File> outputDirsToCopy() {
        def build = buildOutputDirectory
        [
                new File(build,
                         'classes'),
                new File(build,
                         'test-classes')
        ]
    }

    File getRepositoryDirectory() {
        new File(buildOutputDirectory,
                 'repository')
    }

    File getMetaInfDirectory() {
        new File(buildOutputDirectory,
                 'META-INF')
    }

    File getMuleArtifactDirectory() {
        new File(metaInfDirectory,
                 'mule-artifact')
    }

    File getSkinnyMuleArtifactDescriptorPath() {
        new File(projectDirectory,
                 'mule-artifact.json')
    }

    // if the maven generation used to create artifact descriptors needs properties
    Properties getPropertiesForMavenGeneration() {
        null
    }

    File getMavenPomDirectory() {
        projectDirectory
    }

    File getMavenPomPath() {
        new File(mavenPomDirectory,
                 'pom.xml')
    }

    File getClassesDirectory() {
        new File(buildOutputDirectory,
                 'classes')
    }

    /**
     * If you need certain Maven profiles to be activated while calling Maven
     * to generate the classloader model, include them here
     * @return
     */
    List<String> getMavenProfiles() {
        []
    }

    private DescriptorGenerator getDescriptorGenerator() {
        new DescriptorGenerator(classLoaderModelFile,
                                classLoaderModelTestFile,
                                skinnyMuleArtifactDescriptorPath,
                                classesDirectory,
                                buildOutputDirectory,
                                muleArtifactDirectory,
                                mavenPomPath,
                                propertiesForMavenGeneration,
                                mavenProfiles)
    }

    File getClassLoaderModelFile() {
        new File(muleArtifactDirectory,
                 'classloader-model.json')
    }

    File getClassLoaderModelTestFile() {
        // putting this in buildOutputDirectory (e.g. target) rather than
        // muleArtifactDirectory because we do not want this file in the finished product/JAR
        // this file is also deterministic on the maven profiles that generated it so we need those in here
        def mavenProfileString = mavenProfiles.join('-')
        def filename = "classloader-model-test${mavenProfileString ? '-' + mavenProfileString : mavenProfileString}.json"
        new File(buildOutputDirectory,
                 filename)
    }

    /**
     * Default implementation
     * @return
     */
    Map getClassLoaderModel() {
        def generator = getDescriptorGenerator()
        generator.regenerateClassLoaderModelAndArtifactDescriptor()
        def file = classLoaderModelTestFile
        assert file.exists(): "Could not find ${file}. Has the Mule Maven plugin built your project yet? If you are not going to create this file yourself, you might want to run DescriptorGenerator.regenerateClassLoaderModelAndArtifactDescriptor()"
        new JsonSlurper().parse(file) as Map
    }

    File getMuleArtifactPath() {
        new File(muleArtifactDirectory,
                 'mule-artifact.json')
    }

    // domains are trickier to deal with here. for now, we just won't load them
    def filterDomainFromClassLoaderModel(Map classLoaderModel) {
        def dependencies = classLoaderModel.dependencies
        def domains = dependencies.findAll { dep ->
            dep.artifactCoordinates.classifier == 'mule-domain'
        }
        if (domains.any()) {
            logger.info 'Removing domain {} from classloader model to make tests simpler',
                        domains
        }
        def leanerDependencies = dependencies - domains
        return classLoaderModel + [dependencies: leanerDependencies]
    }

    Map getMuleArtifactJson() {
        def file = getMuleArtifactPath()
        assert file.exists(): "Could not find ${file}. Has the Mule Maven plugin built your project yet. If you are not going to create this file, override getMuleArtifactJson"
        def map = new JsonSlurper().parse(file) as Map
        def entirelyDifferentConfigList = getConfigResources()
        if (entirelyDifferentConfigList) {
            map.configs = entirelyDifferentConfigList
        }
        def configs = substituteConfigResources(map.configs as List<String>)
        configs += getAdditionalConfigResources()
        map.configs = configs
        map
    }

    List<String> getConfigResources() {
        // if null, framework will use list from artifact descriptor
        null
    }

    List<String> getAdditionalConfigResources() {
        []
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
        try {
            def code = closure.rehydrate(runner,
                                         this,
                                         this)
            code.resolveStrategy = Closure.DELEGATE_ONLY
            code()
            def outputEvent = runFlow(bridge,
                                      flowName,
                                      runner.getEvent())
            runner.transformOutput(outputEvent)
        }
        finally {
            runner.closeLogContext()
        }
    }

    EventWrapper runSoapApikitFlow(RuntimeBridgeTestSide bridge,
                                   String operation,
                                   String apiKitFlowName = 'api-main',
                                   String host = 'localhost:9999',
                                   @DelegatesTo(SoapInvoker) Closure closure) {
        def invoker = new SoapApikitInvokerImpl(bridge,
                                                apiKitFlowName,
                                                operation,
                                                host,
                                                bridge)
        def code = closure.rehydrate(invoker,
                                     this,
                                     this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        def event = invoker.event
        runFlow(bridge,
                apiKitFlowName,
                event)
    }

    EventWrapper runFlow(RuntimeBridgeTestSide bridge,
                         String flowName,
                         EventWrapper event) {
        def flow = bridge.getFlow(flowName)
        try {
            flow.process(event)
        }
        catch (e) {
            throw bridge.createInvocationException(e)
        }
    }

    def waitForBatchCompletion(RuntimeBridgeTestSide bridge,
                               List<String> jobsToWaitFor = null,
                               boolean throwUnderlyingException = false,
                               Closure closure) {
        def batchWaitUtil = new BatchWaitUtil(bridge)
        batchWaitUtil.waitFor(jobsToWaitFor,
                              throwUnderlyingException,
                              closure)
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
        def code = closure.rehydrate(runner,
                                     this,
                                     this)
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
        def code = closure.rehydrate(formatterChoice,
                                     this,
                                     this)
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
        def code = closure.rehydrate(formatterChoice,
                                     this,
                                     this)
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
        def code = closure.rehydrate(formatterChoice,
                                     this,
                                     this)
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
        def code = closure.rehydrate(choice,
                                     this,
                                     this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        mockingConfiguration.addMock(connectorName,
                                     choice.mock)
    }

    def mockSoapCall(MockingConfiguration mockingConfiguration,
                     RuntimeBridgeTestSide bridge,
                     String connectorName,
                     @DelegatesTo(SOAPFormatter) Closure closure) {
        def soapFormatter = new SOAPRequestResponseImpl(bridge,
                                                        closure)
        mockingConfiguration.addMock(connectorName,
                                     soapFormatter.transformer)
    }
}
