package com.avioconsulting.mule.testing.dsl.mocking


import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.ClosureCurrier
import com.avioconsulting.mule.testing.transformers.StandardTransformer
import com.avioconsulting.mule.testing.transformers.json.input.JacksonInputTransformer
import com.avioconsulting.mule.testing.transformers.json.output.JacksonOutputTransformer

class JsonFormatterImpl<T extends ConnectorInfo> implements
        JsonFormatter,
        IFormatter {
    private MuleMessageTransformer<T> transformer
    private final IPayloadValidator<T> payloadValidator
    private final ClosureCurrier<T> closureCurrier

    JsonFormatterImpl(IPayloadValidator<T> payloadValidator,
                      ClosureCurrier<T> closureCurrier) {
        this.closureCurrier = closureCurrier
        this.payloadValidator = payloadValidator
    }

    def whenCalledWith(Closure closure) {
        def input = new JacksonInputTransformer<T>(payloadValidator,
                                                   Map)
        def output = new JacksonOutputTransformer()
        this.transformer = new StandardTransformer<T>(closure,
                                                      closureCurrier,
                                                      input,
                                                      output)
    }

    def whenCalledWith(Class inputClass,
                       Closure closure) {
        def input = new JacksonInputTransformer<T>(payloadValidator,
                                                   inputClass)
        def output = new JacksonOutputTransformer()
        this.transformer = new StandardTransformer<T>(closure,
                                                      closureCurrier,
                                                      input,
                                                      output)
    }

    MuleMessageTransformer<T> getTransformer() {
        transformer
    }

    IFormatter<T> withNewPayloadValidator(IPayloadValidator validator) {
        new JsonFormatterImpl<T>(validator,
                                 closureCurrier)
    }

    IPayloadValidator<T> getPayloadValidator() {
        payloadValidator
    }
}
