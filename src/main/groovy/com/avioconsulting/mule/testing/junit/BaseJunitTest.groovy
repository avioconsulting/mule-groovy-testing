package com.avioconsulting.mule.testing.junit

import com.avioconsulting.mule.testing.BaseMuleGroovyTrait
import com.avioconsulting.mule.testing.containers.MuleEngineContainer
import com.avioconsulting.mule.testing.dsl.invokers.BatchRunner
import com.avioconsulting.mule.testing.dsl.invokers.FlowRunner
import com.avioconsulting.mule.testing.dsl.invokers.SoapInvoker
import com.avioconsulting.mule.testing.dsl.mocking.HttpRequestResponseChoice
import com.avioconsulting.mule.testing.dsl.mocking.SOAPFormatter
import com.avioconsulting.mule.testing.dsl.mocking.StandardRequestResponse
import com.avioconsulting.mule.testing.dsl.mocking.sfdc.Choice
import com.avioconsulting.mule.testing.mulereplacements.MockingConfiguration
import com.avioconsulting.mule.testing.mulereplacements.RuntimeBridgeTestSide
import groovy.util.logging.Log4j2
import org.apache.logging.log4j.Logger
import org.junit.Before
import org.junit.runner.RunWith
import org.mule.runtime.api.event.Event

// takes BaseMuleGroovyTrait and adds JUnit lifecycle/state
// TODO: Use annotations to supply all the config stuff in startMule. That way Mule can be started before the test runs, which should make things more clear
@Log4j2
@RunWith(MuleGroovyJunitRunner)
class BaseJunitTest implements
        BaseMuleGroovyTrait {
    protected static MuleEngineContainer muleEngineContainer
    protected static RuntimeBridgeTestSide runtimeBridge
    protected static TestingConfiguration currentTestingConfig
    private static MockingConfiguration mockingConfiguration

    @Override
    Logger getLogger() {
        this.log
    }

    void startEngine() {
        if (!muleEngineContainer) {
            log.info 'Starting up Mule engine for the first time for {}...',
                     baseEngineConfig
            muleEngineContainer = createMuleEngineContainer()
        } else {
            if (muleEngineContainer.engineConfig != baseEngineConfig) {
                log.info 'Restarting Mule engine due to base engine config difference. Existing {}, New {}',
                         muleEngineContainer.engineConfig,
                         baseEngineConfig
                muleEngineContainer.shutdown()
                muleEngineContainer = createMuleEngineContainer()
            }
        }
    }

    @Before
    void startMule() {
        startEngine()
        def proposedTestingConfig = new TestingConfiguration(startUpProperties,
                                                             configResources,
                                                             this.keepListenersOnForTheseFlows())
        def newBridgeNeeded = proposedTestingConfig != currentTestingConfig || !runtimeBridge
        if (!runtimeBridge) {
            log.info 'Deploying Mule app for the first time'
        } else if (proposedTestingConfig != currentTestingConfig) {
            log.info 'Existing Mule app will not work because config has changed, undeploying'
            muleEngineContainer.undeployApplication(runtimeBridge)
            log.info 'Starting fresh Mule app...'
        } else {
            log.info 'Using existing Mule app...'
        }
        if (newBridgeNeeded) {
            mockingConfiguration = new MockingConfiguration(this.keepListenersOnForTheseFlows())
            runtimeBridge = deployApplication(muleEngineContainer,
                                              mockingConfiguration)
            currentTestingConfig = proposedTestingConfig
        }
        mockingConfiguration.clearMocks()
    }

    static void shutdownMule() {
        if (muleEngineContainer) {
            if (runtimeBridge) {
                muleEngineContainer.undeployApplication(runtimeBridge)
                runtimeBridge = null
            }
            muleEngineContainer.shutdown()
            muleEngineContainer = null
        }
    }

    def runFlow(String flowName,
                @DelegatesTo(FlowRunner) Closure closure) {
        runFlow(runtimeBridge,
                flowName,
                closure)
    }

    def runFlow(String flowName,
                Event event) {
        runFlow(runtimeBridge,
                flowName,
                event)
    }

    def mockRestHttpCall(String connectorName,
                         @DelegatesTo(HttpRequestResponseChoice) Closure closure) {
        mockRestHttpCall(mockingConfiguration,
                         runtimeBridge,
                         connectorName,
                         closure)
    }

    def mockSoapCall(String connectorName,
                     @DelegatesTo(SOAPFormatter) Closure closure) {
        mockSoapCall(mockingConfiguration,
                     runtimeBridge,
                     connectorName,
                     closure)
    }

    def mockVmReceive(String connectorName,
                      @DelegatesTo(StandardRequestResponse) Closure closure) {
        mockVmReceive(mockingConfiguration,
                      runtimeBridge,
                      connectorName,
                      closure)
    }

    def mockGeneric(String connectorName,
                    @DelegatesTo(StandardRequestResponse) Closure closure) {
        mockGeneric(mockingConfiguration,
                    runtimeBridge,
                    connectorName,
                    closure)
    }

    def mockSalesForceCall(String connectorName,
                           @DelegatesTo(Choice) Closure closure) {
        mockSalesForceCall(mockingConfiguration,
                           runtimeBridge,
                           connectorName,
                           closure)
    }

    def runBatch(String batchName,
                 List<String> jobsToWaitFor = null,
                 boolean throwUnderlyingException = false,
                 @DelegatesTo(BatchRunner) Closure closure) {
        runBatch(runtimeBridge,
                 batchName,
                 jobsToWaitFor,
                 throwUnderlyingException,
                 closure)
    }

    def runSoapApikitFlow(String operation,
                          String apiKitFlowName = 'api-main',
                          @DelegatesTo(SoapInvoker) Closure closure) {
        runSoapApikitFlow(runtimeBridge,
                          operation,
                          apiKitFlowName,
                          closure)
    }
}
