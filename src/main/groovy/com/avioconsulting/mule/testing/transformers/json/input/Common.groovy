package com.avioconsulting.mule.testing.transformers.json.input

import com.avioconsulting.mule.testing.RunnerConfig
import com.avioconsulting.mule.testing.dsl.ConnectorType
import com.avioconsulting.mule.testing.dsl.invokers.FlowRunnerImpl
import com.avioconsulting.mule.testing.payload_types.IFetchAllowedPayloadTypes
import com.avioconsulting.mule.testing.payload_types.StreamingDisabledPayloadTypes
import com.avioconsulting.mule.testing.transformers.InputTransformer
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.transport.NullPayload

abstract class Common implements InputTransformer {
    private final MuleContext muleContext
    private final ConnectorType connectorType
    private IFetchAllowedPayloadTypes fetchAllowedPayloadTypes

    Common(MuleContext muleContext,
           ConnectorType connectorType,
           IFetchAllowedPayloadTypes fetchAllowedPayloadTypes) {
        this.fetchAllowedPayloadTypes = fetchAllowedPayloadTypes
        this.connectorType = connectorType
        this.muleContext = muleContext
    }

    def validateContentType(MuleMessage message) {
        def runnerConfig = muleContext.registry.get(FlowRunnerImpl.AVIO_MULE_RUNNER_CONFIG_BEAN) as RunnerConfig
        // don't need content-type for VM or empty strings
        if (!runnerConfig.doContentTypeCheck || message.payloadAsString == '') {
            return
        }
        def errorMessage
        switch (connectorType) {
            case ConnectorType.HTTP_REQUEST:
                errorMessage = "Content-Type was not set to 'application/json' before calling your mock endpoint! Add a set-property"
                break
            case ConnectorType.HTTP_LISTENER:
                errorMessage = "Content-Type was not set to 'application/json' within your flow! Add a set-property"
                break
            default:
                // VM, etc. should not need this
                return
        }
        if (!errorMessage) {
            return
        }
        def actualContentType = message.getOutboundProperty('Content-Type') as String
        assert actualContentType && actualContentType.contains(
                'application/json'): "${errorMessage}. Actual type was ${actualContentType}"
    }

    abstract def transform(String jsonString)

    def transformInput(MuleMessage muleMessage) {
        // comes back from some Mule connectors like JSON
        if (muleMessage.payload instanceof NullPayload) {
            return null
        }
        def allowedPayloadTypes = fetchAllowedPayloadTypes.allowedPayloadTypes
        def validType = allowedPayloadTypes.find { type ->
            type.isInstance(muleMessage.payload)
        }
        if (!validType) {
            throw new Exception(
                    "Expected payload to be of type ${allowedPayloadTypes} here but it actually was ${muleMessage.payload.class}. Check the connectors you're mocking and make sure you transformed the payload properly! (e.g. payload into VMs must be Strings)")
        }
        // want to wait to do this after if the payload type check above since it consumes the string
        def jsonString = muleMessage.payloadAsString
        validateContentType(muleMessage)
        return transform(jsonString)
    }

    def disableStreaming() {
        this.fetchAllowedPayloadTypes = new StreamingDisabledPayloadTypes()
    }
}
