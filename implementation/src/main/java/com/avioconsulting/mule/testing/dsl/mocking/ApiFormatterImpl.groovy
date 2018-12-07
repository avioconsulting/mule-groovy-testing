package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.muleinterfaces.MuleMessageTransformer
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ReturnWrapper
import com.avioconsulting.mule.testing.transformers.ClosureCurrier
import com.avioconsulting.mule.testing.transformers.InputTransformer
import com.avioconsulting.mule.testing.transformers.OutputTransformer
import com.avioconsulting.mule.testing.transformers.StandardTransformer

class ApiFormatterImpl<T extends ConnectorInfo> implements
        RawFormatter,
        IFormatter {
    private final ClosureCurrier closureCurrier
    private MuleMessageTransformer<T> transformer

    ApiFormatterImpl(ClosureCurrier closureCurrier) {
        this.closureCurrier = closureCurrier
    }

    @Override
    MuleMessageTransformer getTransformer() {
        this.transformer
    }

    @Override
    def whenCalledWith(Closure closure) {
        def input = new InputTransformer<T>() {
            def transformInput(EventWrapper input,
                               T connectorInfo) {
                // api payloads will be straight java most of the time
                input.message.valueInsideTypedValue
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
}
