package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.InputTransformer
import com.avioconsulting.mule.testing.transformers.OutputTransformer
import com.avioconsulting.mule.testing.transformers.StringInputTransformer
import com.avioconsulting.mule.testing.transformers.json.input.JacksonInputTransformer
import com.avioconsulting.mule.testing.transformers.json.output.JacksonOutputTransformer
import org.mule.DefaultMuleEvent
import org.mule.DefaultMuleMessage
import org.mule.MessageExchangePattern
import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.api.MuleMessage
//import org.mule.munit.common.util.MunitMuleTestUtils

class JsonInvokerImpl implements JsonInvoker, Invoker {
    private final MuleContext muleContext
    private OutputTransformer transformBeforeCallingFlow
    private InputTransformer transformAfterCallingFlow
    private inputObject
    private boolean outputOnly
    private boolean inputOnly
    private final IPayloadValidator initialPayloadValidator

    JsonInvokerImpl(MuleContext muleContext,
                    IPayloadValidator initialPayloadValidator) {
        this.initialPayloadValidator = initialPayloadValidator
        this.muleContext = muleContext
        this.outputOnly = false
        this.inputOnly = false
    }

    IPayloadValidator getPayloadValidator() {
        initialPayloadValidator
    }

    def inputPayload(Object inputObject) {
        setInputTransformer(inputObject)
        transformAfterCallingFlow = new JacksonInputTransformer(muleContext,
                                                                initialPayloadValidator,
                                                                [Map, Map[]])
    }

    def inputPayload(Object inputObject,
                     Class outputClass) {
        setInputTransformer(inputObject)
        if (outputClass == String) {
            transformAfterCallingFlow = new StringInputTransformer(initialPayloadValidator,
                                                                   muleContext)
        } else {
            setJacksonOutputTransformer(outputClass)
        }
    }

    def inputOnly(Object inputObject) {
        this.inputOnly = true
        setInputTransformer(inputObject)
    }

    private void setJacksonOutputTransformer(Class outputClass) {
        transformAfterCallingFlow = new JacksonInputTransformer(muleContext,
                                                                initialPayloadValidator,
                                                                outputClass)
    }

    def outputOnly(Class outputClass) {
        // Jackson handles maps too
        setJacksonOutputTransformer(outputClass)
        outputOnly = true
    }

    private setInputTransformer(inputObject) {
        assert !(inputObject instanceof Class): 'Use outputOnly if a only an output class is being supplied!'
        this.inputObject = inputObject
        transformBeforeCallingFlow = new JacksonOutputTransformer(muleContext)
    }

    def noStreaming() {
        if (!inputOnly) {
            assert transformAfterCallingFlow: 'Need to specify a type of JSON serialization (jackson, map) first!'
            transformAfterCallingFlow.disableStreaming()
        }
        if (!outputOnly) {
            assert transformBeforeCallingFlow: 'Need to specify a type of JSON serialization (jackson, map) first!'
            transformBeforeCallingFlow.disableStreaming()
        }
    }

    MuleEvent getEvent() {
        MuleMessage inputMessage
        if (outputOnly) {
            inputMessage = new DefaultMuleMessage(null, muleContext)
        } else {
            assert transformBeforeCallingFlow: 'Need to specify a type of JSON serialization (jackson, map)'
            inputMessage = transformBeforeCallingFlow.transformOutput(this.inputObject)
        }
        new DefaultMuleEvent(inputMessage,
                             MessageExchangePattern.REQUEST_RESPONSE,
                             MunitMuleTestUtils.getTestFlow(muleContext))
    }

    def transformOutput(MuleEvent event) {
        if (inputOnly) {
            return
        }
        assert transformAfterCallingFlow
        transformAfterCallingFlow.transformInput(event.message)
    }

    Invoker withNewPayloadValidator(IPayloadValidator validator) {
        new JsonInvokerImpl(muleContext,
                            validator)
    }
}
