package com.avioconsulting.mule.testing.runners

import com.avioconsulting.mule.testing.messages.JsonMessage
import org.mule.DefaultMuleEvent
import org.mule.MessageExchangePattern
import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.munit.common.util.MunitMuleTestUtils
import org.mule.transport.NullPayload

abstract class JsonRunner implements JsonMessage {
    private final MuleContext muleContext
    private boolean streaming = true
    private final RunnerConfig runnerConfig

    JsonRunner(MuleContext muleContext,
               RunnerConfig runnerConfig) {
        this.runnerConfig = runnerConfig
        this.muleContext = muleContext
    }

    protected abstract String getJsonString()

    def disableStreaming() {
        streaming = false
    }

    MuleEvent getEvent() {
        def message = getJSONMessage(jsonString,
                                     muleContext,
                                     null,
                                     streaming)
        new DefaultMuleEvent(message,
                             MessageExchangePattern.REQUEST_RESPONSE,
                             MunitMuleTestUtils.getTestFlow(muleContext))
    }

    protected abstract Object getObjectFromOutput(String outputJson)

    protected boolean isEnforceContentType() {
        !runnerConfig.apiKitReferencesThisFlow
    }

    def transformOutput(MuleEvent outputEvent) {
        // filters can return null events
        if (outputEvent == null) {
            return null
        }
        
        def message = outputEvent.message
        // comes back from some Mule connectors like JSON
        if (message.payload instanceof NullPayload) {
            return null
        }
        def jsonString = message.payloadAsString

        // empty payload
        if (!jsonString) {
            return null
        }

        if (enforceContentType) {
            def contentType = message.getOutboundProperty('Content-Type') as String
            assert contentType && contentType.contains(
                    'application/json'): "Content-Type was not set to 'application/json' within your flow! Add a set-property"
        }
        getObjectFromOutput(jsonString)
    }
}
