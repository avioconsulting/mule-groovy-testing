package com.avioconsulting.mule.testing.junit

import com.avioconsulting.mule.testing.BaseMuleGroovyTrait
import com.avioconsulting.mule.testing.dsl.invokers.BatchRunner
import com.avioconsulting.mule.testing.dsl.invokers.FlowRunner
import com.avioconsulting.mule.testing.dsl.invokers.SoapInvoker
import com.avioconsulting.mule.testing.dsl.mocking.SOAPFormatter
import com.avioconsulting.mule.testing.dsl.mocking.StandardRequestResponse
import com.avioconsulting.mule.testing.dsl.mocking.sfdc.Choice
import com.avioconsulting.mule.testing.muleinterfaces.MockingConfiguration
import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import groovy.util.logging.Log4j2
import org.apache.logging.log4j.Logger
import org.junit.runner.RunWith

// takes BaseMuleGroovyTrait and adds JUnit lifecycle/state
@Log4j2
@RunWith(MuleGroovyJunitRunner)
abstract class BaseJunitTest implements
        BaseMuleGroovyTrait {
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
        startMule()
    }

    // allow overriding
    def startMule() {
        testState.startMule(this)
    }

    Map getFreshClassLoaderModel() {
        BaseMuleGroovyTrait.super.getClassLoaderModel()
    }

    @Override
    Map getClassLoaderModel() {
        // this is deterministic based on config/deployed app so we delegate to test state
        testState.cachedClassLoaderModel
    }

    def runFlow(String flowName,
                @DelegatesTo(FlowRunner) Closure closure) {
        runFlow(runtimeBridge,
                flowName,
                closure)
    }

    def mockRestHttpCall(String connectorName,
                         @DelegatesTo(StandardRequestResponse) Closure closure) {
        mockRestHttpCall(mockingConfiguration,
                         runtimeBridge,
                         connectorName,
                         closure)
    }

    def mockSoapCall(String connectorName,
                     @DelegatesTo(SOAPFormatter) Closure closure) {
        mockSoapCall(mockingConfiguration,
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

    def waitForBatchCompletion(List<String> jobsToWaitFor = null,
                               boolean throwUnderlyingException = false,
                               Closure closure) {
        waitForBatchCompletion(runtimeBridge,
                               jobsToWaitFor,
                               throwUnderlyingException,
                               closure)
    }

    def runSoapApikitFlow(String operation,
                          String apiKitFlowName = 'api-main',
                          String host = 'localhost:9999',
                          @DelegatesTo(SoapInvoker) Closure closure) {
        runSoapApikitFlow(runtimeBridge,
                          operation,
                          apiKitFlowName,
                          host,
                          closure)
    }

    def runSoapApikitFlowJaxbResultBody(String operation,
                                        String apiKitFlowName = 'api-main',
                                        String host = 'localhost:9999',
                                        @DelegatesTo(SoapInvoker) Closure closure) {
        runSoapApikitFlowJaxbResultBody(runtimeBridge,
                                        operation,
                                        apiKitFlowName,
                                        host,
                                        closure)
    }

    // can't just new up a Java class inside the app from our test because the app runs in a different
    // classloader than our tests do
    def <T> T instantiateJavaClassWithAppClassLoader(Class<T> klass) {
        instantiateJavaClassWithAppClassLoader(klass,
                                               runtimeBridge)
    }
}
