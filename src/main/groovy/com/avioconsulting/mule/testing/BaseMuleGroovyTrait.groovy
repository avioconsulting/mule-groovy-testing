package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.batch.BatchWaitUtil
import com.avioconsulting.mule.testing.containers.BaseEngineConfig
import com.avioconsulting.mule.testing.containers.MuleEngineContainer
import com.avioconsulting.mule.testing.dsl.invokers.*
import com.avioconsulting.mule.testing.dsl.mocking.*
import com.avioconsulting.mule.testing.dsl.mocking.sfdc.Choice
import com.avioconsulting.mule.testing.dsl.mocking.sfdc.ChoiceImpl
import com.avioconsulting.mule.testing.mulereplacements.MockingConfiguration
import com.avioconsulting.mule.testing.mulereplacements.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.payloadvalidators.SOAPPayloadValidator
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.NotImplementedException
import org.apache.logging.log4j.Logger

// basic idea here is to have a trait that could be mixed in to any type of testing framework situation
// this trait should be stateless
trait BaseMuleGroovyTrait {
    abstract Logger getLogger()

    MuleEngineContainer createMuleEngineContainer() {
        new MuleEngineContainer(baseEngineConfig)
    }

    static File join(Object... paths) {
        assert paths[0] instanceof File
        paths.inject { acc, file ->
            new File(acc, file)
        }
    }

    RuntimeBridgeTestSide deployApplication(MuleEngineContainer muleEngineContainer,
                                            MockingConfiguration mockingConfiguration) {
        def artifactName = getArtifactName()
        def appSourceDir = new File(muleEngineContainer.muleHomeDirectory,
                                    artifactName)
        appSourceDir.mkdirs()
        try {
            def metaInfDir = join(appSourceDir, 'META-INF')
            def muleArtifactDir = join(metaInfDir, 'mule-artifact')
            muleArtifactDir.mkdirs()
            def classLoaderFile = join(muleArtifactDir, 'classloader-model.json')
            def classLoaderModel = getClassLoaderModel()
            def logger = getLogger()
            def classLoaderModelJson = JsonOutput.prettyPrint(JsonOutput.toJson(classLoaderModel))
            logger.info 'Using classloader model {}',
                        classLoaderModelJson
            classLoaderFile.text = classLoaderModelJson
            def artifactJson = join(muleArtifactDir, 'mule-artifact.json')
            def muleArtifact = getMuleArtifactJson()
            def muleArtifactJson = JsonOutput.prettyPrint(JsonOutput.toJson(muleArtifact))
            logger.info 'Using Mule artifact descriptor {}',
                        muleArtifactJson
            artifactJson.text = muleArtifactJson
            def srcMavenPath = getMavenPomPath()
            assert srcMavenPath.exists(): "Expected Maven pom @ ${srcMavenPath}."
            def artifactCoordinates = classLoaderModel.artifactCoordinates
            def destMavenPath = join(metaInfDir,
                                     'maven',
                                     artifactCoordinates.groupId,
                                     artifactCoordinates.artifactId)
            destMavenPath.mkdirs()
            FileUtils.copyFileToDirectory(srcMavenPath,
                                          destMavenPath)
            def sourceRepositoryDirectory = repositoryDirectory
            if (sourceRepositoryDirectory.exists()) {
                def targetRepositoryDirectory = join(appSourceDir, 'repository')
                FileUtils.copyDirectory(sourceRepositoryDirectory,
                                        targetRepositoryDirectory)
            }

            def configFiles = muleArtifact.configs.collect { config ->
                def candidateUrl = BaseMuleGroovyTrait.getResource("/${config}")
                assert candidateUrl: "Expected to find ${config} in classpath but did not!"
                new File(candidateUrl.toURI())
            }

            logger.info 'Using config files {}',
                        configFiles

            configFiles.each { configFile ->
                FileUtils.copyFileToDirectory(configFile,
                                              appSourceDir)
            }
            muleEngineContainer.deployApplication(artifactName,
                                                  appSourceDir.toURI(),
                                                  mockingConfiguration,
                                                  startUpProperties)
        }
        finally {
            FileUtils.deleteDirectory(appSourceDir)
        }
    }

    Properties getStartUpProperties() {
        new Properties()
    }

    BaseEngineConfig getBaseEngineConfig() {
        new BaseEngineConfig('4.1.2')
    }

    // TODO: Figure out how to do this w/ Mule 4
    List<String> keepListenersOnForTheseFlows() {
        []
    }

    Map<String, String> getConfigResourceSubstitutes() {
        [:]
    }

    File getBuildOutputDirectory() {
        new File('target')
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

    File getMavenPomPath() {
        new File('pom.xml')
    }

    /**
     * Default implementation
     * @return
     */
    Map getClassLoaderModel() {
        def file = new File(muleArtifactDirectory, 'classloader-model.json')
        assert file.exists(): "Could not find ${file}. Has the Mule Maven plugin built your project yet. If you are not going to create this file, override getClassLoaderModel"
        new JsonSlurper().parse(file) as Map
    }

    Map getMuleArtifactJson() {
        def file = new File(muleArtifactDirectory, 'mule-artifact.json')
        assert file.exists(): "Could not find ${file}. Has the Mule Maven plugin built your project yet. If you are not going to create this file, override getMuleArtifactJson"
        new JsonSlurper().parse(file) as Map
    }

    String getArtifactName() {
        muleArtifactJson.name
    }

    List<String> getConfigResources() {
        muleArtifactJson.configs
    }

    def runFlow(RuntimeBridgeTestSide muleContext,
                String flowName,
                @DelegatesTo(FlowRunner) Closure closure) {
        def flow = muleContext.getFlow(flowName)
        def runner = new FlowRunnerImpl(muleContext,
                                        flow,
                                        flowName)
        def code = closure.rehydrate(runner, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        def outputEvent = runFlow(muleContext,
                                  flowName,
                                  runner.getEvent())
        runner.transformOutput(outputEvent)
    }

    EventWrapper runSoapApikitFlow(RuntimeBridgeTestSide muleContext,
                                   String operation,
                                   String apiKitFlowName = 'api-main',
                                   @DelegatesTo(SoapInvoker) Closure closure) {
        def invoker = new SoapApikitInvokerImpl(muleContext,
                                                muleContext,
                                                apiKitFlowName,
                                                operation)
        def code = closure.rehydrate(invoker, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        def event = invoker.event
        runFlow(muleContext,
                apiKitFlowName,
                event)
    }

    EventWrapper runFlow(RuntimeBridgeTestSide muleContext,
                         String flowName,
                         EventWrapper event) {
        def flow = muleContext.getFlow(flowName)
        flow.process(event)
    }

    def waitForBatchCompletion(RuntimeBridgeTestSide muleContext,
                               List<String> jobsToWaitFor = null,
                               boolean throwUnderlyingException = false,
                               Closure closure) {
        def batchWaitUtil = new BatchWaitUtil(muleContext)
        batchWaitUtil.waitFor(jobsToWaitFor, throwUnderlyingException, closure)
    }

    def runBatch(RuntimeBridgeTestSide muleContext,
                 String batchName,
                 List<String> jobsToWaitFor = null,
                 boolean throwUnderlyingException = false,
                 @DelegatesTo(BatchRunner) Closure closure) {
        throw new NotImplementedException()
//        def runner = new FlowRunnerImpl(muleContext,
//                                        null,// batch doesn't inherit from flow
//                                        batchName)
//        def code = closure.rehydrate(runner, this, this)
//        code.resolveStrategy = Closure.DELEGATE_ONLY
//        code()
//        def batchJob = muleContext.registry.get(batchName) as BatchJobAdapter
//        waitForBatchCompletion(muleContext,
//                               jobsToWaitFor,
//                               throwUnderlyingException) {
//            batchJob.execute(runner.getEvent())
//        }
    }

    def mockRestHttpCall(MockingConfiguration mockingConfiguration,
                         RuntimeBridgeTestSide bridge,
                         String connectorName,
                         @DelegatesTo(HttpRequestResponseChoice) Closure closure) {
        def formatterChoice = new HttpRequestResponseChoiceImpl(bridge,
                                                                bridge)
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
        def formatterChoice = new VMRequestResponseChoiceImpl(bridge)
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
        def formatterChoice = new GenericRequestResponseChoiceImpl(bridge)
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
                     RuntimeBridgeTestSide muleContext,
                     String connectorName,
                     @DelegatesTo(SOAPFormatter) Closure closure) {
        def payloadValidator = new SOAPPayloadValidator()
        def soapFormatter = new SOAPFormatterImpl(muleContext,
                                                  payloadValidator)
        def code = closure.rehydrate(soapFormatter, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        mockingConfiguration.addMock(connectorName,
                                     soapFormatter.transformer)
    }
}
