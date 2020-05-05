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
import com.avioconsulting.mule.testing.muleinterfaces.containers.Dependency
import com.avioconsulting.mule.testing.muleinterfaces.containers.MuleEngineContainer
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.commons.codec.binary.Hex
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.Logger

import java.security.MessageDigest

// basic idea here is to have a trait that could be mixed in to any type of testing framework situation
// this trait should be stateless
trait BaseMuleGroovyTrait {
    abstract Logger getLogger()

    MuleEngineContainer createMuleEngineContainer(TestingConfiguration testingConfiguration) {
        new MuleEngineContainer(muleTestHomeDirectory,
                                testingConfiguration.engineConfig)
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
            classLoaderModel = filterDependenciesFromClassLoaderModel(classLoaderModel,
                                                                      testingConfiguration.dependenciesToFilter)
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
                logger.info 'Copying source repository {} to {}',
                            sourceRepositoryDirectory,
                            targetRepositoryDirectory
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
            logger.info 'Deploying with properties {}',
                        testingConfiguration.startupPropertiesAsJavaUtilProps
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

    boolean isUseLazyConnections() {
        // lazyConnections is important  otherwise connectors that make connections (like SFTP)
        // cannot be mocked because a connection attempt will fail before the mock
        // interceptor is reached
        true
    }

    boolean isUseLazyInit() {
        // see isUseLazyConnections, lazyInit is not required to address that issue
        // since our runner does a decent job of loading everything before the test starts, in order to
        // keep the actual test method output less noisy, making this false by default
        false
    }

    boolean isLazyInitXmlValidations() {
        // if we do use lazy init, still want the validations
        true
    }

    boolean isGenerateXmlSchemas() {
        // Allows doing this without changing the code but if you want to enable this with code, you can
        def prop = System.getProperty('avio.groovy.test.generate.xml.schemas')
        prop != null && prop == 'true'
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

    File getOriginalRepositoryDirectory() {
        new File(buildOutputDirectory,
                 'repository')
    }

    File getMuleTestHomeDirectory() {
        new File('.mule')
    }

    File getRepositoryDirectory() {
        // Studio wipes out the repository directory (and any other temp directories)
        // from target if you save an XML file. To avoid re-running the build cycle
        // we save off a copy in .mule (where it's safe from Studio) and use that
        // for repeated test runs while editing in Studio
        def digest = MessageDigest.getInstance('SHA-256')
        // we don't want subtle whitespace differences, etc. (which studio will do)
        // to mess up our hash
        digest.update(JsonOutput.toJson(classLoaderModel).bytes)
        def hashAsHex = Hex.encodeHexString(digest.digest())
        // matches up with where the dep resolver plugin put it
        def candidate = new File(muleTestHomeDirectory,
                                 "repository-${hashAsHex}")
        if (!candidate.exists()) {
            assert new FileNameFinder().getFileNames(originalRepositoryDirectory.absolutePath,
                                                     '**/*').size() > 0:
                    "Expected repository directory ${originalRepositoryDirectory} to already exist and have files! Run mvn clean generate-test-resources first!"
            logger.info "${candidate} repository directory does not exist, copying from ${originalRepositoryDirectory}"
            FileUtils.copyDirectory(originalRepositoryDirectory,
                                    candidate)
        }
        return candidate
    }

    File getMetaInfDirectory() {
        new File(buildOutputDirectory,
                 'META-INF')
    }

    File getMuleArtifactDirectory() {
        new File(metaInfDirectory,
                 'mule-artifact')
    }

    File getMavenPomDirectory() {
        projectDirectory
    }

    File getMavenPomPath() {
        new File(mavenPomDirectory,
                 'pom.xml')
    }

    /**
     * If you want some dependencies to be filtered out of your classloader model during the test runs
     * You can override this method. Regular expressions are accepted
     * @return
     */
    List<Dependency> getDependenciesToFilter() {
        []
    }

    File getClassLoaderModelFile() {
        new File(muleArtifactDirectory,
                 'classloader-model.json')
    }

    /**
     * Default implementation
     * @return
     */
    Map getClassLoaderModel() {
        def file = getClassLoaderModelFile()
        assert file.exists(): "Could not find ${file}. Has the Mule Maven plugin built your project yet? Run mvn clean test-compile to ensure that and the artifact descriptor are created"
        new JsonSlurper().parse(file) as Map
    }

    File getMuleArtifactPath() {
        new File(muleArtifactDirectory,
                 'mule-artifact.json')
    }

    def filterDependenciesFromClassLoaderModel(Map classLoaderModel,
                                               List<Dependency> filters) {
        def dependencies = classLoaderModel.dependencies
        def domains = dependencies.findAll { dep ->
            // domains are trickier to deal with here. for now, we just won't load them
            dep.artifactCoordinates.classifier == 'mule-domain'
        }
        if (domains.any()) {
            logger.info 'Removing domain {} from classloader model to make tests simpler',
                        domains
        }
        def otherMatches = filters.collect { filterDependency ->
            dependencies.findAll { actualDependency ->
                def coordinates = actualDependency.artifactCoordinates
                coordinates.groupId.matches(filterDependency.groupId) &&
                        coordinates.artifactId.matches(filterDependency.artifactId) &&
                        coordinates.version.matches(filterDependency.version) &&
                        actualDependency.uri.matches(filterDependency.filename)
            }
        }.flatten()
        if (otherMatches.any()) {
            logger.info 'Removing the following dependencies as requested by the getDependenciesToFilter() method: {}',
                        otherMatches
        }
        def leanerDependencies = dependencies - domains - otherMatches
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

    def runSoapApikitFlowJaxbResultBody(RuntimeBridgeTestSide bridge,
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
        def outputEvent = runFlow(bridge,
                                  apiKitFlowName,
                                  event)
        invoker.transformOutput(outputEvent)
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
                         @DelegatesTo(StandardRequestResponse) Closure closure) {
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
                     String connectorName,
                     @DelegatesTo(SOAPFormatter) Closure closure) {
        def soapFormatter = new SOAPRequestResponseImpl(closure)
        mockingConfiguration.addMock(connectorName,
                                     soapFormatter.transformer)
    }

    // can't just new up a Java class inside the app from our test because the app runs in a different
    // classloader than our tests do
    def <T> T instantiateJavaClassWithAppClassLoader(Class<T> klass,
                                                     RuntimeBridgeTestSide runtimeBridge) {
        // we can 'trick' Java by taking in a class/method generic which will be IDE friendly
        // but we'll actually return an instance of the class loaded from the app's classloader instead
        // but the generics will gone at that point so everything will just work
        def klassFromApp = runtimeBridge.getAppClassloader().loadClass(klass.name)
        klassFromApp.newInstance()
    }
}
