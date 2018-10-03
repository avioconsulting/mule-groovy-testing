package com.avioconsulting.mule.testing.dsl.mocking


import com.avioconsulting.mule.testing.TransformingEventFactory
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ReturnWrapper
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.ClosureCurrier
import com.avioconsulting.mule.testing.transformers.InputTransformer
import com.avioconsulting.mule.testing.transformers.OutputTransformer
import com.avioconsulting.mule.testing.transformers.StandardTransformer

class RawFormatterImpl<T extends ConnectorInfo> implements
        RawFormatter,
        IFormatter {
    private final IPayloadValidator payloadValidator
    private MuleMessageTransformer<T> transformer
    private final ClosureCurrier closureCurrier
    private final TransformingEventFactory transformingEventFactory

    RawFormatterImpl(TransformingEventFactory transformingEventFactory,
                     IPayloadValidator payloadValidator,
                     ClosureCurrier closureCurrier) {
        this.transformingEventFactory = transformingEventFactory
        this.closureCurrier = closureCurrier
        this.payloadValidator = payloadValidator
    }

    @Override
    def whenCalledWith(Closure closure) {
        def input = new InputTransformer<T>() {
            def transformInput(EventWrapper input,
                               T connectorInfo) {
                input.message
            }
        }
        def output = new OutputTransformer() {
            @Override
            EventWrapper transformOutput(Object inputMessage,
                                         EventWrapper originalMuleEvent) {
                assert inputMessage instanceof ReturnWrapper : "When using 'raw', you need to return a ReturnWrapper object so that the media/mime type is known. You returned ${inputMessage.class}"
                transformingEventFactory.getMuleEventWithPayload(inputMessage.payload,
                                                                 inputMessage.mediaType,
                                                                 originalMuleEvent)
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
        new RawFormatterImpl(transformingEventFactory,
                             validator,
                             closureCurrier)
    }

    @Override
    IPayloadValidator getPayloadValidator() {
        payloadValidator
    }
}
