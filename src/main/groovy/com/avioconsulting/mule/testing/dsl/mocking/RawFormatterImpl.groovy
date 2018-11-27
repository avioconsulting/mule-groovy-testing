package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.ClosureCurrier
import com.avioconsulting.mule.testing.transformers.RawTransformer
import com.avioconsulting.mule.testing.transformers.StandardTransformer

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
        def inputOutput = new RawTransformer(eventFactory)
        this.transformer = new StandardTransformer(closure,
                                                   closureCurrier,
                                                   inputOutput,
                                                   inputOutput)
    }

    @Override
    MuleMessageTransformer getTransformer() {
        this.transformer
    }

    @Override
    IFormatter withNewPayloadValidator(IPayloadValidator validator) {
        new RawFormatterImpl(eventFactory,
                             validator,
                             closureCurrier)
    }

    @Override
    IPayloadValidator getPayloadValidator() {
        payloadValidator
    }
}
