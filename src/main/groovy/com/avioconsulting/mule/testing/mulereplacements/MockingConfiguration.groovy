package com.avioconsulting.mule.testing.mulereplacements

import com.avioconsulting.mule.testing.mulereplacements.wrappers.MockEventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.MockEventWrapperImpl

class MockingConfiguration {
    private final Map<String, MuleMessageTransformer> mocks = [:]
    private final List<String> keepListenersOnForTheseFlows

    MockingConfiguration(List<String> keepListenersOnForTheseFlows) {
        this.keepListenersOnForTheseFlows = keepListenersOnForTheseFlows
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
    void executeMock(Object componentLocation,
                     Object interceptionEvent,
                     Object parameters) {
        def connectorName = parameters.get('doc:name').providedValue() as String
        def mockProcess = mocks[connectorName]
        def params = (parameters as Map).collectEntries { key, value ->
            [key, value.resolveValue()]
        }
        def event = new MockEventWrapperImpl(interceptionEvent)
        def factory = new ConnectorInfoFactory()
        def connectorInfo = factory.getConnectorInfo(componentLocation.fileName.get() as String,
                                                     componentLocation.lineInFile.get() as Integer,
                                                     params)
        def result = mockProcess.transform(event,
                                           connectorInfo)
        assert result == event : "Expected the same event to be passed along"
        result
    }

    boolean shouldFlowListenerBeEnabled(String flowName) {
        keepListenersOnForTheseFlows.contains(flowName)
    }
}
