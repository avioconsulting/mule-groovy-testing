package com.avioconsulting.mule.testing.junit

import com.avioconsulting.mule.testing.containers.MuleEngineContainer
import com.avioconsulting.mule.testing.mulereplacements.MockingConfiguration
import com.avioconsulting.mule.testing.mulereplacements.RuntimeBridgeTestSide
import groovy.util.logging.Log4j2
import org.apache.logging.log4j.CloseableThreadContext
import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.InitializationError

@Log4j2
class MuleGroovyJunitRunner extends
        BlockJUnit4ClassRunner {
    static boolean listenerSetup = false
    private static MuleEngineContainer muleEngineContainer
    private static TestingConfiguration currentTestingConfig
    private static MockingConfiguration mockingConfiguration
    private static RuntimeBridgeTestSide runtimeBridge
    private static final Map<TestingConfiguration, Integer> failedConfigurations = [:]

    MuleGroovyJunitRunner(Class<?> klass) throws InitializationError {
        super(klass)
    }

    static void startEngine(BaseJunitTest test,
                            TestingConfiguration proposedConfig) {
        if (!muleEngineContainer) {
            log.info 'Starting up Mule engine for the first time for {}...',
                     test.baseEngineConfig
            muleEngineContainer = test.createMuleEngineContainer(proposedConfig)
        } else {
            if (muleEngineContainer.engineConfig != proposedConfig.engineConfig) {
                log.info 'Restarting Mule engine due to base engine config difference. Existing {}, New {}',
                         muleEngineContainer.engineConfig,
                         test.baseEngineConfig
                muleEngineContainer.shutdown()
                muleEngineContainer = test.createMuleEngineContainer(proposedConfig)
            }
        }
    }

    static void startMule(BaseJunitTest test) {
        def threadContext = CloseableThreadContext.putAll([
                testClass: test.getClass().getName()
        ])
        try {
            def proposedTestingConfig = new TestingConfiguration(test.getStartUpProperties(),
                                                                 test.getClassLoaderModel(),
                                                                 test.getMuleArtifactJson(),
                                                                 test.keepListenersOnForTheseFlows(),
                                                                 test.outputDirsToCopy(),
                                                                 test.getBaseEngineConfig(),
                                                                 test.getMavenPomPath().absolutePath,
                                                                 test.getRepositoryDirectory().absolutePath)
            if (failedConfigurations.containsKey(proposedTestingConfig)) {
                def msg = 'Skipping load of application because this testing config previously failed to start'
                log.error(msg)
                throw new RuntimeException(msg)
            }
            startEngine(test,
                        proposedTestingConfig)
            def newBridgeNeeded = proposedTestingConfig != currentTestingConfig || !runtimeBridge
            if (!runtimeBridge) {
                log.info 'Deploying Mule app for the first time'
            } else if (proposedTestingConfig != currentTestingConfig) {
                log.info 'Existing Mule app will not work because config has changed, undeploying'
                muleEngineContainer.undeployApplication(runtimeBridge)
                runtimeBridge = null
                test.runtimeBridge = null
                log.info 'Starting fresh Mule app...'
            } else {
                log.info 'Using existing Mule app...'
            }
            if (newBridgeNeeded) {
                mockingConfiguration = new MockingConfiguration(proposedTestingConfig)
                try {
                    runtimeBridge = test.deployApplication(muleEngineContainer,
                                                           proposedTestingConfig,
                                                           mockingConfiguration)
                }
                catch (e) {
                    failedConfigurations[proposedTestingConfig] = 1
                    throw e
                }
                currentTestingConfig = proposedTestingConfig
            }
            mockingConfiguration.clearMocks()
            test.runtimeBridge = runtimeBridge
            test.mockingConfiguration = mockingConfiguration
        }
        finally {
            threadContext.close()
        }
    }

    @Override
    protected Object createTest() throws Exception {
        def ourTest = super.createTest() as BaseJunitTest
        startMule(ourTest)
        ourTest
    }

    @Override
    protected void runChild(FrameworkMethod method,
                            RunNotifier notifier) {
        // this method is called for every test, so only do this once
        if (!listenerSetup) {
            listenerSetup = true
            notifier.addListener(new MuleGroovyShutdownListener())
        }
        super.runChild(method, notifier)
    }

    static void shutdownMule() {
        if (muleEngineContainer) {
            muleEngineContainer.shutdown()
            muleEngineContainer = null
        }
    }
}
