package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.StandardTransformer
import com.avioconsulting.mule.testing.transformers.json.input.JacksonInputTransformer
import com.avioconsulting.mule.testing.transformers.json.output.JacksonOutputTransformer
import org.mule.api.MuleContext
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer

class JsonFormatterImpl implements JsonFormatter, IFormatter {
    private final MuleContext muleContext
    private MuleMessageTransformer transformer
    private final IPayloadValidator payloadValidator

    JsonFormatterImpl(MuleContext muleContext,
                      IPayloadValidator payloadValidator) {
        this.payloadValidator = payloadValidator
        this.muleContext = muleContext
    }

    def whenCalledWith(Closure closure) {
        def input = new JacksonInputTransformer(muleContext,
                                                payloadValidator,
                                                Map)
        def output = new JacksonOutputTransformer(muleContext)
        this.transformer = new StandardTransformer(closure, input, output)
    }

    def whenCalledWith(Class inputClass,
                       Closure closure) {
        def input = new JacksonInputTransformer(muleContext,
                                                payloadValidator,
                                                inputClass)
        def output = new JacksonOutputTransformer(muleContext)
        this.transformer = new StandardTransformer(closure, input, output)
    }

    MuleMessageTransformer getTransformer() {
        transformer
    }

    IFormatter withNewPayloadValidator(IPayloadValidator validator) {
        new JsonFormatterImpl(muleContext, validator)
    }

    IPayloadValidator getPayloadValidator() {
        payloadValidator
    }
}
