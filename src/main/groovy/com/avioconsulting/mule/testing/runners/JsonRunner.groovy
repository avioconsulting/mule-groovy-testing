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

    JsonRunner(MuleContext muleContext) {
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

    def transformOutput(MuleEvent outputEvent) {
        def jsonString = outputEvent.message.payloadAsString
        getObjectFromOutput(jsonString)
    }
}
