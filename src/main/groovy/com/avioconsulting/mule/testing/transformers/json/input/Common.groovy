package com.avioconsulting.mule.testing.transformers.json.input

import com.avioconsulting.mule.testing.dsl.invokers.FlowRunnerImpl
import com.avioconsulting.mule.testing.dsl.mocking.ConnectorType
import com.avioconsulting.mule.testing.runners.RunnerConfig
import com.avioconsulting.mule.testing.transformers.InputTransformer
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.transport.NullPayload

abstract class Common implements InputTransformer {
    private final MuleContext muleContext
    private final ConnectorType mockedConnectorType
    private final Class expectedPayloadType

    Common(MuleContext muleContext,
           ConnectorType mockedConnectorType,
           Class expectedPayloadType) {
        this.expectedPayloadType = expectedPayloadType
        this.mockedConnectorType = mockedConnectorType
        this.muleContext = muleContext
    }

    def validateContentType(MuleMessage message) {
        def runnerConfig = muleContext.registry.get(FlowRunnerImpl.AVIO_MULE_RUNNER_CONFIG_BEAN) as RunnerConfig
        // don't need content-type for VM right now
        if (!runnerConfig.doContentTypeCheck) {
            return
        }
        def errorMessage = null
        switch (mockedConnectorType) {
            case ConnectorType.HTTP_REQUEST:
                errorMessage = "Content-Type was not set to 'application/json' before calling your mock endpoint! Add a set-property"
                break
            case ConnectorType.HTTP_LISTENER:
                errorMessage = "Content-Type was not set to 'application/json' within your flow! Add a set-property"
                break
        }
        if (!errorMessage) {
            return
        }
        assert message.getOutboundProperty('Content-Type') as String == 'application/json': errorMessage
    }

    abstract def transform(String jsonString)

    def transformInput(MuleMessage muleMessage) {
        // comes back from some Mule connectors like JSON
        if (muleMessage.payload instanceof NullPayload) {
            return null
        }
        def payloadType = muleMessage.payload.class
        // it's OK to not match IF we return an empty string here
        if (!expectedPayloadType.isInstance(muleMessage.payload) && muleMessage.payloadAsString) {
            throw new Exception(
                    "Expected payload to be of type ${expectedPayloadType} here but it actually was ${payloadType}. Check the connectors you're mocking and make sure you transformed the payload properly! (e.g. payload into VMs must be Strings)")
        }
        // want to wait to do this after if the payload type check above since it consumes the string
        def jsonString = muleMessage.payloadAsString
        // don't validate empty message content types
        if (!jsonString) {
            return
        }
        validateContentType(muleMessage)
        return transform(jsonString)
    }
}
