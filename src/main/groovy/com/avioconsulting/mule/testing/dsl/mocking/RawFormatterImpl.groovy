package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.InputTransformer
import com.avioconsulting.mule.testing.transformers.OutputTransformer
import com.avioconsulting.mule.testing.transformers.StandardTransformer
import org.mule.DefaultMuleMessage
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer

class RawFormatterImpl implements RawFormatter, IFormatter {
    private final IPayloadValidator payloadValidator
    private MuleMessageTransformer transformer
    private final MuleContext muleContext

    RawFormatterImpl(MuleContext muleContext,
                     IPayloadValidator payloadValidator) {
        this.muleContext = muleContext
        this.payloadValidator = payloadValidator
    }

    @Override
    def whenCalledWith(Closure closure) {
        def input = new InputTransformer() {
            @Override
            def transformInput(MuleMessage input) {
                input.payload
            }

            @Override
            def disableStreaming() {
                // don't need to do anything for raw
            }
        }
        def output = new OutputTransformer() {
            @Override
            MuleMessage transformOutput(Object inputMessage) {
                new DefaultMuleMessage(inputMessage, muleContext)
            }

            @Override
            def disableStreaming() {
                // don't need to do anything for raw
            }
        }
        this.transformer = new StandardTransformer(closure,
                                                   input,
                                                   output)
    }

    @Override
    MuleMessageTransformer getTransformer() {
        this.transformer
    }

    @Override
    IFormatter withNewPayloadValidator(IPayloadValidator validator) {
        new RawFormatterImpl(muleContext, validator)
    }

    @Override
    IPayloadValidator getPayloadValidator() {
        payloadValidator
    }
}
