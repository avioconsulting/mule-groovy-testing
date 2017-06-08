package com.avioconsulting.mule.testing.transformers.json.input

import com.avioconsulting.mule.testing.RunnerConfig
import com.avioconsulting.mule.testing.dsl.ConnectorType
import com.avioconsulting.mule.testing.dsl.invokers.FlowRunnerImpl
import com.avioconsulting.mule.testing.transformers.InputTransformer
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.transport.NullPayload

abstract class Common implements InputTransformer {
    private final MuleContext muleContext
    private final ConnectorType connectorType
    private List<Class> allowedPayloadTypes

    Common(MuleContext muleContext,
           ConnectorType connectorType,
           List<Class> allowedPayloadTypes) {
        this.allowedPayloadTypes = allowedPayloadTypes
        this.connectorType = connectorType
        this.muleContext = muleContext
    }

    def validateContentType(MuleMessage message) {
        def runnerConfig = muleContext.registry.get(FlowRunnerImpl.AVIO_MULE_RUNNER_CONFIG_BEAN) as RunnerConfig
        // don't need content-type for VM right now
        if (!runnerConfig.doContentTypeCheck) {
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
        if (!isInvalidPayloadType(muleMessage.payload)) {
            throw new Exception(
                    "Expected payload to be of type ${allowedPayloadTypes} here but it actually was ${muleMessage.payload.class}. Check the connectors you're mocking and make sure you transformed the payload properly! (e.g. payload into VMs must be Strings)")
        }
        // want to wait to do this after if the payload type check above since it consumes the string
        def jsonString = muleMessage.payloadAsString
        validateContentType(muleMessage)
        return transform(jsonString)
    }

    private boolean isInvalidPayloadType(payload) {
        allowedPayloadTypes.find { type ->
            type.isInstance(payload)
        }
    }

    def disableStreaming() {
        this.allowedPayloadTypes = [String]
    }
}
