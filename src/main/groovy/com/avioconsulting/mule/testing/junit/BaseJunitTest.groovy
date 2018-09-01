package com.avioconsulting.mule.testing.junit

import com.avioconsulting.mule.testing.BaseMuleGroovyTrait
import com.avioconsulting.mule.testing.dsl.invokers.BatchRunner
import com.avioconsulting.mule.testing.dsl.invokers.FlowRunner
import com.avioconsulting.mule.testing.dsl.invokers.SoapInvoker
import com.avioconsulting.mule.testing.dsl.mocking.HttpRequestResponseChoice
import com.avioconsulting.mule.testing.dsl.mocking.SOAPFormatter
import com.avioconsulting.mule.testing.dsl.mocking.StandardRequestResponse
import com.avioconsulting.mule.testing.dsl.mocking.sfdc.Choice
import com.avioconsulting.mule.testing.mulereplacements.MockingConfiguration
import groovy.util.logging.Log4j2
import org.apache.logging.log4j.Logger
import org.junit.Before
import org.junit.runner.RunWith
import org.mule.runtime.core.api.MuleContext
import org.mule.runtime.core.api.event.CoreEvent

// takes BaseMuleGroovyTrait and adds JUnit lifecycle/state
// TODO: Use annotations to supply all the config stuff in startMule. That way Mule can be started before the test runs, which should make things more clear
@Log4j2
@RunWith(MuleGroovyJunitRunner)
class BaseJunitTest implements BaseMuleGroovyTrait {
    protected static MuleContext muleContext
    protected static TestingConfiguration currentTestingConfig
    private static MockingConfiguration mockingConfiguration

    @Override
    Logger getLogger() {
        this.log
    }

    @Before
    void startMule() {
        def proposedTestingConfig = new TestingConfiguration(startUpProperties,
                                                             configResources,
                                                             this.keepListenersOnForTheseFlows())
        def newContextNeeded = proposedTestingConfig != currentTestingConfig || !muleContext
        if (!muleContext) {
            log.info 'Starting up Mule test context for the first time'
        } else if (proposedTestingConfig != currentTestingConfig) {
            log.info 'Existing Mule context will not work because config has changed, killing existing context'
            shutdownMule()
            log.info 'Starting fresh Mule context...'
        } else {
            log.info 'Using existing Mule context'
        }
        if (newContextNeeded) {
            mockingConfiguration = new MockingConfiguration(this.keepListenersOnForTheseFlows())
            muleContext = createMuleContext(mockingConfiguration)
            muleContext.start()
            currentTestingConfig = proposedTestingConfig
        }
        mockingConfiguration.clearMocks()
    }

    static void shutdownMule() {
        if (muleContext && muleContext.started) {
            muleContext.stop()
            assert muleContext.stopped
            muleContext.dispose()
            assert muleContext.disposed
        }
        muleContext = null
    }

    def runFlow(String flowName,
                @DelegatesTo(FlowRunner) Closure closure) {
        runFlow(muleContext,
                flowName,
                closure)
    }

    def runFlow(String flowName,
                CoreEvent event) {
        runFlow(muleContext,
                flowName,
                event)
    }

    def mockRestHttpCall(String connectorName,
                         @DelegatesTo(HttpRequestResponseChoice) Closure closure) {
        mockRestHttpCall(mockingConfiguration,
                         muleContext,
                         connectorName,
                         closure)
    }

    def mockSoapCall(String connectorName,
                     @DelegatesTo(SOAPFormatter) Closure closure) {
        mockSoapCall(mockingConfiguration,
                     muleContext,
                     connectorName,
                     closure)
    }

    def mockVmReceive(String connectorName,
                      @DelegatesTo(StandardRequestResponse) Closure closure) {
        mockVmReceive(mockingConfiguration,
                      muleContext,
                      connectorName,
                      closure)
    }

    def mockGeneric(String connectorName,
                    @DelegatesTo(StandardRequestResponse) Closure closure) {
        mockGeneric(mockingConfiguration,
                    muleContext,
                    connectorName,
                    closure)
    }

    def mockSalesForceCall(String connectorName,
                           @DelegatesTo(Choice) Closure closure) {
        mockSalesForceCall(mockingConfiguration,
                           muleContext,
                           connectorName,
                           closure)
    }

    def runBatch(String batchName,
                 List<String> jobsToWaitFor = null,
                 boolean throwUnderlyingException = false,
                 @DelegatesTo(BatchRunner) Closure closure) {
        runBatch(muleContext,
                 batchName,
                 jobsToWaitFor,
                 throwUnderlyingException,
                 closure)
    }

    def runSoapApikitFlow(String operation,
                          String apiKitFlowName = 'api-main',
                          @DelegatesTo(SoapInvoker) Closure closure) {
        runSoapApikitFlow(muleContext,
                          operation,
                          apiKitFlowName,
                          closure)
    }
}
