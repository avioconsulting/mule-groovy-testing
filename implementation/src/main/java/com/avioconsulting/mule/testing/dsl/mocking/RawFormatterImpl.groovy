package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.muleinterfaces.MuleMessageTransformer
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ReturnWrapper
import com.avioconsulting.mule.testing.transformers.ClosureCurrier
import com.avioconsulting.mule.testing.transformers.InputTransformer
import com.avioconsulting.mule.testing.transformers.OutputTransformer
import com.avioconsulting.mule.testing.transformers.StandardTransformer

class RawFormatterImpl<T extends ConnectorInfo> implements
        RawFormatter,
        IFormatter {
    private MuleMessageTransformer<T> transformer
    private final ClosureCurrier closureCurrier

    RawFormatterImpl(ClosureCurrier closureCurrier) {
        this.closureCurrier = closureCurrier
    }

    @Override
    def whenCalledWith(Closure closure) {
        def input = new InputTransformer<T>() {
            def transformInput(EventWrapper input,
                               T connectorInfo) {
                // if the connector allows changing what's used for input, it will come in here
                connectorInfo.supportsIncomingBody ? connectorInfo.incomingBody : input.message.payload
            }
        }
        def output = new OutputTransformer() {
            @Override
            EventWrapper transformOutput(Object inputMessage,
                                         EventWrapper originalMuleEvent,
                                         ConnectorInfo connectorInfo) {
                def payload = inputMessage
                String mediaType = null
                if (inputMessage instanceof ReturnWrapper) {
                    payload = inputMessage.payload
                    mediaType = inputMessage.mediaType
                }
                originalMuleEvent.withNewPayload(payload,
                                                 connectorInfo,
                                                 mediaType)
            }
        }
        this.transformer = new StandardTransformer(closure,
                                                   closureCurrier,
                                                   input,
                                                   output)
    }

    @Override
    MuleMessageTransformer<T> getTransformer() {
        this.transformer
    }
}
