package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.RunnerConfig
import com.avioconsulting.mule.testing.dsl.invokers.FlowRunnerImpl
import com.avioconsulting.mule.testing.dsl.mocking.ConnectorType
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.transport.NullPayload

class StringInputTransformer implements InputTransformer {
    private final ConnectorType connectorType
    private final MuleContext muleContext

    StringInputTransformer(ConnectorType connectorType,
                           MuleContext muleContext) {
        this.muleContext = muleContext
        this.connectorType = connectorType
    }

    def transformInput(MuleMessage muleMessage) {
        // comes back from some Mule connectors like JSON
        if (muleMessage.payload instanceof NullPayload) {
            return null
        }
        if (muleMessage.payload.class != String) {
            throw new Exception(
                    "Expected payload to be of type String here but it actually was ${muleMessage.payload.class}. Check the connectors you're mocking and make sure you transformed the payload properly! (e.g. payload into VMs must be Strings)")
        }
        validateContentType(muleMessage)
        muleMessage.payload
    }

    def disableStreaming() {
        // we already expect a string
    }

    private def validateContentType(MuleMessage muleMessage) {
        def runnerConfig = muleContext.registry.get(FlowRunnerImpl.AVIO_MULE_RUNNER_CONFIG_BEAN) as RunnerConfig
        // don't need content-type for VM right now
        if (!runnerConfig.doContentTypeCheck) {
            return
        }

        def errorMessage = null
        switch (connectorType) {
            case ConnectorType.HTTP_REQUEST:
                errorMessage = "Content-Type was not set to 'text/plain' before calling your mock endpoint! Add a set-property or remove the incorrect type."
                break
            case ConnectorType.HTTP_LISTENER:
                errorMessage = "Content-Type was not set to 'text/plain' within your flow! Add a set-property or remove the incorrect type."
                break
        }
        if (!errorMessage) {
            return
        }
        def actualContentType = muleMessage.getOutboundProperty('Content-Type') as String
        // HTTP default is plain
        if (!actualContentType) {
            return
        }
        assert actualContentType == 'text/plain': "${errorMessage}. Actual type was ${actualContentType}"
    }
}
