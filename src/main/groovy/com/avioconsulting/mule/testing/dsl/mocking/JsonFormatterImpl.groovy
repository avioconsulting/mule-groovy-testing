package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.ClosureCurrier
import com.avioconsulting.mule.testing.transformers.StandardTransformer
import com.avioconsulting.mule.testing.transformers.json.input.JacksonInputTransformer
import com.avioconsulting.mule.testing.transformers.json.output.JacksonOutputTransformer

class JsonFormatterImpl implements JsonFormatter, IFormatter {
    private MuleMessageTransformer transformer
    private final IPayloadValidator payloadValidator
    private final InvokerEventFactory eventFactory
    private final ClosureCurrier closureCurrier

    JsonFormatterImpl(IPayloadValidator payloadValidator,
                      InvokerEventFactory eventFactory,
                      ClosureCurrier closureCurrier) {
        this.closureCurrier = closureCurrier
        this.eventFactory = eventFactory
        this.payloadValidator = payloadValidator
    }

    def whenCalledWith(Closure closure) {
        def input = new JacksonInputTransformer(payloadValidator,
                                                Map)
        def output = new JacksonOutputTransformer(eventFactory)
        this.transformer = new StandardTransformer(closure,
                                                   closureCurrier,
                                                   input,
                                                   output)
    }

    def whenCalledWith(Class inputClass,
                       Closure closure) {
        def input = new JacksonInputTransformer(payloadValidator,
                                                inputClass)
        def output = new JacksonOutputTransformer(eventFactory)
        this.transformer = new StandardTransformer(closure,
                                                   closureCurrier,
                                                   input,
                                                   output)
    }

    MuleMessageTransformer getTransformer() {
        transformer
    }

    IFormatter withNewPayloadValidator(IPayloadValidator validator) {
        new JsonFormatterImpl(validator,
                              eventFactory,
                              closureCurrier)
    }

    IPayloadValidator getPayloadValidator() {
        payloadValidator
    }
}
