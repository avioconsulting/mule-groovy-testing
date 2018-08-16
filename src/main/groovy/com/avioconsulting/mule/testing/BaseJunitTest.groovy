package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.dsl.invokers.BatchRunner
import com.avioconsulting.mule.testing.dsl.invokers.FlowRunner
import com.avioconsulting.mule.testing.dsl.mocking.HttpRequestResponseChoice
import com.avioconsulting.mule.testing.dsl.mocking.SOAPFormatter
import com.avioconsulting.mule.testing.dsl.mocking.StandardRequestResponse
import com.avioconsulting.mule.testing.dsl.mocking.sfdc.Choice
import com.avioconsulting.mule.testing.mulereplacements.MockingConfiguration
import groovy.util.logging.Log4j2
import org.apache.logging.log4j.Logger
import org.junit.AfterClass
import org.junit.Before
import org.mule.api.MuleContext
import org.mule.api.MuleEvent

// takes BaseMuleGroovyTrait and adds JUnit lifecycle/state
@Log4j2
class BaseJunitTest implements BaseMuleGroovyTrait {
    protected static MuleContext muleContext
    private static MockingConfiguration mockingConfiguration

    @Override
    Logger getLogger() {
        this.log
    }

    @Before
    void startMule() {
        if (!muleContext) {
            mockingConfiguration = new MockingConfiguration()
            muleContext = createMuleContext(mockingConfiguration)
            muleContext.start()
        }
        mockingConfiguration.clearMocks()
    }

    @AfterClass
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
                MuleEvent event) {
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
}
