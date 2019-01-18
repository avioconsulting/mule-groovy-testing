package com.avioconsulting.mule.testing.muleinterfaces

import com.avioconsulting.mule.testing.junit.TestingConfiguration
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.InterceptEventWrapperImpl
import groovy.util.logging.Log4j2
import org.apache.logging.log4j.CloseableThreadContext

@Log4j2
class MockingConfiguration {
    private final Map<String, MuleMessageTransformer> mocks = [:]
    private final Map<String, Integer> keepListenersOnForTheseFlows
    Object runtimeBridgeMuleSide

    MockingConfiguration(TestingConfiguration testingConfiguration) {
        this.keepListenersOnForTheseFlows = testingConfiguration.keepListenersOnForTheseFlows.collectEntries { flowName
            ->
            [flowName, 1]
        }
    }

    def clearMocks() {
        mocks.clear()
    }

    def addMock(String processorName,
                MuleMessageTransformer mockHandler) {
        mocks[processorName] = mockHandler
    }

    // consumed via reflection
    boolean isMocked(String connectorName) {
        mocks.containsKey(connectorName)
    }

    /**
     * consumed via reflection
     *
     * @param componentLocation - org.mule.runtime.api.component.location.ComponentLocation
     * @param interceptionEvent - org.mule.runtime.api.interception.InterceptionEvent
     * @param parameters - Map<String, org.mule.runtime.api.interception.ProcessorParameterValue>
     */
    void executeMock(String connectorName,
                     Object componentLocation,
                     Object interceptionEvent,
                     Object parameters) {
        def mockProcess = mocks[connectorName]
        def params = (parameters as Map).collectEntries { key, value ->
            [key, value.resolveValue()]
        }
        def event = new InterceptEventWrapperImpl(interceptionEvent,
                                                  this.runtimeBridgeMuleSide)
        def factory = new ConnectorInfoFactory()
        def connectorInfo = factory.getConnectorInfo(componentLocation.fileName.get() as String,
                                                     connectorName,
                                                     componentLocation.getRootContainerName() as String,
                                                     componentLocation.lineInFile.get() as Integer,
                                                     params)
        def threadContext = CloseableThreadContext.push('Mock processor')
        threadContext.put('connector',
                          connectorInfo.name)
        threadContext.put('container',
                          connectorInfo.container)
        threadContext.put('filename',
                          connectorInfo.fileName)
        threadContext.put('lineNumber',
                          connectorInfo.lineNumber.toString())
        try {
            log.info 'Beginning mock execution'
            try {
                mockProcess.transform(event,
                                      connectorInfo)
            }
            catch (e) {
                log.info 'Completed mock execution with exception'
                throw e
            }
            log.info 'Completed mock execution'
        }
        finally {
            threadContext.close()
        }
    }

    boolean shouldFlowListenerBeEnabled(String flowName) {
        def keepListenerOn = keepListenersOnForTheseFlows.containsKey(flowName)
        if (keepListenerOn) {
            log.info "Keeping listener enabled for flow '{}' per test class configuration",
                     flowName
        } else {
            log.info "Disabling listener for flow '{}' per test class configuration",
                     flowName
        }
        keepListenerOn
    }

    Object getErrorTypeRepository() {
        runtimeBridgeMuleSide.getErrorTypeRepository()
    }
}
