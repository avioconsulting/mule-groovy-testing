package com.avioconsulting.mule.testing.dsl.mocking


import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.MockEventWrapper
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.ClosureCurrier
import com.avioconsulting.mule.testing.transformers.InputTransformer
import com.avioconsulting.mule.testing.transformers.OutputTransformer
import com.avioconsulting.mule.testing.transformers.StandardTransformer

class RawFormatterImpl<T extends ConnectorInfo> implements
        RawFormatter,
        IFormatter {
    private final IPayloadValidator payloadValidator
    private MuleMessageTransformer transformer
    private final ClosureCurrier closureCurrier

    RawFormatterImpl(IPayloadValidator payloadValidator,
                     ClosureCurrier closureCurrier) {
        this.closureCurrier = closureCurrier
        this.payloadValidator = payloadValidator
    }

    @Override
    def whenCalledWith(Closure closure) {
        def input = new InputTransformer<T>() {
            def transformInput(EventWrapper input,
                               T connectorInfo) {
                input.message.payload
            }

            @Override
            def disableStreaming() {
                // don't need to do anything for raw
            }
        }
        def output = new OutputTransformer() {
            @Override
            void transformOutput(Object inputMessage,
                                 MockEventWrapper originalMuleEvent) {
                assert false: 'mutate the existing event/message as appropriate'
                eventFactory.getMuleEventWithPayload(inputMessage,
                                                     originalMuleEvent)
            }

            @Override
            def disableStreaming() {
                // don't need to do anything for raw
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

    @Override
    IFormatter withNewPayloadValidator(IPayloadValidator validator) {
        new RawFormatterImpl(validator,
                             closureCurrier)
    }

    @Override
    IPayloadValidator getPayloadValidator() {
        payloadValidator
    }
}
