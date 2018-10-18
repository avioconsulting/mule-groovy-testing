package com.avioconsulting.mule.testing.junit

import com.avioconsulting.mule.testing.BaseMuleGroovyTrait
import com.avioconsulting.mule.testing.dsl.invokers.BatchRunner
import com.avioconsulting.mule.testing.dsl.invokers.FlowRunner
import com.avioconsulting.mule.testing.dsl.invokers.SoapInvoker
import com.avioconsulting.mule.testing.dsl.mocking.HttpRequestResponseChoice
import com.avioconsulting.mule.testing.dsl.mocking.SOAPFormatter
import com.avioconsulting.mule.testing.dsl.mocking.StandardRequestResponse
import com.avioconsulting.mule.testing.dsl.mocking.sfdc.Choice
import com.avioconsulting.mule.testing.muleinterfaces.MockingConfiguration
import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j2
import org.apache.logging.log4j.Logger
import org.junit.runner.RunWith

// takes BaseMuleGroovyTrait and adds JUnit lifecycle/state
@Log4j2
@RunWith(MuleGroovyJunitRunner)
class BaseJunitTest implements
        BaseMuleGroovyTrait {
    private static Map cachedClassLoaderModel
    static TestState testState = new TestState()

    MockingConfiguration getMockingConfiguration() {
        testState.mockingConfiguration
    }

    RuntimeBridgeTestSide getRuntimeBridge() {
        testState.runtimeBridge
    }

    @Override
    Logger getLogger() {
        this.log
    }

    // by using the constructor, we can have our setup stuff run before each test
    // which clarifies the output a bit when trying to examine each test
    BaseJunitTest() {
        testState.startMule(this)
    }

    Map getClassLoaderModel() {
        if (!cachedClassLoaderModel) {
            regenerateClassLoaderModelAndArtifactDescriptor()
            cachedClassLoaderModel = new JsonSlurper().parse(classLoaderModelFile)
        }
        cachedClassLoaderModel
    }

    def runFlow(String flowName,
                @DelegatesTo(FlowRunner) Closure closure) {
        runFlow(runtimeBridge,
                flowName,
                closure)
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
