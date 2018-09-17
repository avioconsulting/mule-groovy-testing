package com.avioconsulting.mule.testing.mulereplacements

class MockingConfiguration {
    private final Map<String, MockProcess> mocks = [:]
    private final List<String> keepListenersOnForTheseFlows

    MockingConfiguration(List<String> keepListenersOnForTheseFlows) {
        this.keepListenersOnForTheseFlows = keepListenersOnForTheseFlows
    }

    def clearMocks() {
        mocks.clear()
    }

    def addMock(String processorName,
                MockProcess mockHandler) {
        mocks[processorName] = mockHandler
    }

    boolean isMocked(String connectorName) {
        mocks.containsKey(connectorName)
    }

    void executeMock(Object componentLocation,
                     Object interceptionEvent) {
        println 'our mock!'
    }

    boolean shouldFlowListenerBeEnabled(String flowName) {
        keepListenersOnForTheseFlows.contains(flowName)
    }
}
