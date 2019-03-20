package com.avioconsulting.mule.testing.junit


import com.avioconsulting.mule.testing.muleinterfaces.MockingConfiguration
import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.muleinterfaces.containers.MuleEngineContainer
import groovy.util.logging.Log4j2
import org.apache.logging.log4j.CloseableThreadContext

@Log4j2
class TestState {
    private MuleEngineContainer muleEngineContainer
    private TestingConfiguration currentTestingConfig
    private MockingConfiguration mockingConfiguration
    private RuntimeBridgeTestSide runtimeBridge
    private final Map<TestingConfiguration, Integer> failedConfigurations = [:]
    private final List<String> newConfigs = []
    Map cachedClassLoaderModel

    MockingConfiguration getMockingConfiguration() {
        return mockingConfiguration
    }

    RuntimeBridgeTestSide getRuntimeBridge() {
        return runtimeBridge
    }

    void startEngine(BaseJunitTest test,
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

    void undeployExistingApps() {
        if (muleEngineContainer && runtimeBridge) {
            log.info 'Undeploying existing app {}',
                     runtimeBridge.artifactName
            muleEngineContainer.undeployApplication(runtimeBridge)
            runtimeBridge = null
        }
    }

    private def populateClassLoaderModel(BaseJunitTest test) {
        cachedClassLoaderModel = test.getFreshClassLoaderModel()
    }

    void startMule(BaseJunitTest test) {
        def threadContext = CloseableThreadContext.putAll([
                testClass: test.getClass().getName()
        ])
        try {
            if (!cachedClassLoaderModel) {
                // testing config needs this
                populateClassLoaderModel(test)
            }
            def proposedTestingConfig = new TestingConfiguration(test.getStartUpProperties(),
                                                                 test.getClassLoaderModel(),
                                                                 test.getMuleArtifactJson(),
                                                                 test.keepListenersOnForTheseFlows(),
                                                                 test.mavenProfiles,
                                                                 test.dependenciesToFilter,
                                                                 test.outputDirsToCopy(),
                                                                 test.getBaseEngineConfig(),
                                                                 test.getMavenPomPath().absolutePath,
                                                                 test.getRepositoryDirectory().absolutePath,
                                                                 test.useLazyConnections,
                                                                 test.useLazyInit,
                                                                 test.lazyInitXmlValidations)
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
                log.info 'Starting fresh Mule app...'
            } else {
                log.info 'Using existing Mule app...'
            }
            if (newBridgeNeeded) {
                populateClassLoaderModel(test)
                newConfigs << test.getClass().getName()
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
        }
        finally {
            threadContext.close()
        }
    }

    void shutdownMule() {
        if (muleEngineContainer) {
            muleEngineContainer.shutdown()
            muleEngineContainer = null
            log.info 'Shutdown Mule, started up a total of {} apps. List of tests that started apps: {}',
                     newConfigs.size(),
                     newConfigs
        }
    }
}
