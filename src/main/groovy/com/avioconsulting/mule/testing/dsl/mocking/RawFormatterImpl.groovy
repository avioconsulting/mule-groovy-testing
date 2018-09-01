package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.ClosureCurrier
import com.avioconsulting.mule.testing.transformers.InputTransformer
import com.avioconsulting.mule.testing.transformers.OutputTransformer
import com.avioconsulting.mule.testing.transformers.StandardTransformer
import org.mule.runtime.core.api.event.CoreEvent
import org.mule.runtime.core.api.processor.Processor

class RawFormatterImpl implements RawFormatter, IFormatter {
    private final IPayloadValidator payloadValidator
    private MuleMessageTransformer transformer
    private final EventFactory eventFactory
    private final ClosureCurrier closureCurrier

    RawFormatterImpl(EventFactory eventFactory,
                     IPayloadValidator payloadValidator,
                     ClosureCurrier closureCurrier) {
        this.closureCurrier = closureCurrier
        this.eventFactory = eventFactory
        this.payloadValidator = payloadValidator
    }

    @Override
    def whenCalledWith(Closure closure) {
        def input = new InputTransformer() {
            @Override
            def transformInput(CoreEvent input,
                               Processor messageProcessor) {
                input.message.payload
            }

            @Override
            def disableStreaming() {
                // don't need to do anything for raw
            }
        }
        def output = new OutputTransformer() {
            @Override
            CoreEvent transformOutput(Object inputMessage,
                                      CoreEvent originalMuleEvent) {
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
    MuleMessageTransformer getTransformer() {
        this.transformer
    }

    @Override
    IFormatter withNewPayloadValidator(IPayloadValidator validator) {
        new RawFormatterImpl(eventFactory, validator)
    }

    @Override
    IPayloadValidator getPayloadValidator() {
        payloadValidator
    }
}
