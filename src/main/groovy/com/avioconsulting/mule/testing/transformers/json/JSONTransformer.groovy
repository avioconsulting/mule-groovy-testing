package com.avioconsulting.mule.testing.transformers.json

import com.avioconsulting.mule.testing.dsl.invokers.FlowRunnerImpl
import com.avioconsulting.mule.testing.dsl.mocking.MockedConnectorType
import com.avioconsulting.mule.testing.runners.RunnerConfig
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

abstract class JSONTransformer implements MuleMessageTransformer {
    private final Class expectedPayloadType
    private final MuleContext muleContext
    private final MockedConnectorType mockedConnectorType

    JSONTransformer(Class expectedPayloadType,
                    MuleContext muleContext,
                    MockedConnectorType mockedConnectorType) {
        this.mockedConnectorType = mockedConnectorType
        this.muleContext = muleContext
        this.expectedPayloadType = expectedPayloadType
    }

    abstract MuleMessage transform(String jsonString)

    def validateContentType(MuleMessage message) {
        def runnerConfig = muleContext.registry.get(FlowRunnerImpl.AVIO_MULE_RUNNER_CONFIG_BEAN) as RunnerConfig
        // don't need content-type for VM right now
        if (runnerConfig.apiKitReferencesThisFlow || mockedConnectorType != MockedConnectorType.HTTP) {
            return
        }
        assert message.getOutboundProperty(
                'Content-Type') as String == 'application/json': "Content-Type was not set to 'application/json' before calling your mock endpoint! Add a set-property"
    }

    MuleMessage transform(MuleMessage muleMessage) {
        validateContentType(muleMessage)
        def payload = muleMessage.payload
        if (expectedPayloadType.isInstance(payload)) {
            return transform(muleMessage.payloadAsString)
        }
        throw new Exception(
                "Expected payload to be of type ${expectedPayloadType} here but it actually was ${payload.class}. Check the connectors you're mocking and make sure you transformed the payload properly! (e.g. payload into VMs must be Strings)")
    }
}
