package com.avioconsulting.mule.testing.runners

import com.avioconsulting.mule.testing.messages.JsonMessage
import org.mule.DefaultMuleEvent
import org.mule.MessageExchangePattern
import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.munit.common.util.MunitMuleTestUtils

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
        def message = outputEvent.message
        if (enforceContentType) {
            assert message.getOutboundProperty(
                    'Content-Type') as String == 'application/json': "Content-Type was not set to 'application/json' within your flow! Add a set-property"
        }
        def jsonString = outputEvent.message.payloadAsString
        getObjectFromOutput(jsonString)
    }
}
