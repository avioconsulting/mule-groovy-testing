package com.avioconsulting.mule.testing.muleinterfaces

import com.avioconsulting.mule.testing.junit.TestingConfiguration
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.InterceptEventWrapperImpl
import groovy.util.logging.Log4j2
import org.apache.logging.log4j.CloseableThreadContext

@Log4j2
class MockingConfiguration {
    private final Map<String, MuleMessageTransformer> mocks = [:]
    private final Map<String, Integer> keepListenersOnForTheseFlows
    private final boolean lazyInitEnabled
    private final boolean generateXmlSchemas
    Object runtimeBridgeMuleSide

    MockingConfiguration(TestingConfiguration testingConfiguration) {
        this.keepListenersOnForTheseFlows = testingConfiguration.keepListenersOnForTheseFlows.collectEntries { flowName
            ->
            [flowName, 1]
        }
        this.lazyInitEnabled = testingConfiguration.lazyInit
        this.generateXmlSchemas = testingConfiguration.generateXmlSchemas
    }

    boolean isLazyInitEnabled() {
        this.lazyInitEnabled
    }

    boolean isGenerateXmlSchemas() {
        this.generateXmlSchemas
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
            def resolved = null
            try {
                // parameters have to be 'resolved' before we can use them
                // Parameters include things like the connector name that we're mocking, how it's configured
                // the target (payload or variable), etc.
                resolved = value.resolveValue()
            }
            catch (e) {
                def logContext = CloseableThreadContext.push('Mock processor')
                logContext.put('connector',
                               connectorName)
                logContext.put('parameterKey',
                               key as String)
                try {
                    if (e.cause.getClass().name == 'org.mule.runtime.api.connection.ConnectionException') {
                        log.info "Ignoring exception during parameter resolution because it is connection related and we're in a test: {}",
                                 e.message
                    } else {
                        log.error "Unable to resolve parameter!",
                                  e
                        throw e
                    }
                }
                finally {
                    logContext.close()
                }
            }
            [key, resolved]
        }
        def event = new InterceptEventWrapperImpl(interceptionEvent,
                                                  this.runtimeBridgeMuleSide)
        def factory = new ConnectorInfoFactory()
        def fetch = new FetchClassLoaders(runtimeBridgeMuleSide)
        def connectorInfo = factory.getConnectorInfo(componentLocation,
                                                     connectorName,
                                                     params,
                                                     fetch,
                                                     new LookupFromRegistryWithMuleBridge(this.runtimeBridgeMuleSide))
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

    Set<String> getKeepListenersOnForTheseFlows() {
        this.keepListenersOnForTheseFlows.keySet()
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

    Object getLocator() {
        runtimeBridgeMuleSide.locator
    }
}
