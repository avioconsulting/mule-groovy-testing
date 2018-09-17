package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.InputTransformer
import com.avioconsulting.mule.testing.transformers.OutputTransformer
import com.avioconsulting.mule.testing.transformers.StringInputTransformer
import com.avioconsulting.mule.testing.transformers.json.input.JacksonInputTransformer
import com.avioconsulting.mule.testing.transformers.json.output.JacksonOutputTransformer

class JsonInvokerImpl implements JsonInvoker, Invoker {
    private OutputTransformer transformBeforeCallingFlow
    private InputTransformer transformAfterCallingFlow
    private inputObject
    private boolean outputOnly
    private boolean inputOnly
    private final IPayloadValidator initialPayloadValidator
    private final InvokerEventFactory eventFactory
    private final Object flow

    JsonInvokerImpl(IPayloadValidator initialPayloadValidator,
                    InvokerEventFactory eventFactory,
                    Object flow) {
        this.flow = flow
        this.eventFactory = eventFactory
        this.initialPayloadValidator = initialPayloadValidator
        this.outputOnly = false
        this.inputOnly = false
    }

    IPayloadValidator getPayloadValidator() {
        initialPayloadValidator
    }

    def inputPayload(Object inputObject) {
        setInputTransformer(inputObject)
        transformAfterCallingFlow = new JacksonInputTransformer(initialPayloadValidator,
                                                                [Map, Map[]])
    }

    def inputPayload(Object inputObject,
                     Class outputClass) {
        setInputTransformer(inputObject)
        if (outputClass == String) {
            transformAfterCallingFlow = new StringInputTransformer(initialPayloadValidator)
        } else {
            setJacksonOutputTransformer(outputClass)
        }
    }

    def inputOnly(Object inputObject) {
        this.inputOnly = true
        setInputTransformer(inputObject)
    }

    private void setJacksonOutputTransformer(Class outputClass) {
        transformAfterCallingFlow = new JacksonInputTransformer(initialPayloadValidator,
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
        transformBeforeCallingFlow = new JacksonOutputTransformer(eventFactory)
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

    EventWrapper getEvent() {
        def input = outputOnly ? null : this.inputObject
        def event = eventFactory.getMuleEventWithPayload(input,
                                                         flow.name,
                                                         MessageExchangePattern.REQUEST_RESPONSE)
        if (outputOnly) {
            return event
        } else {
            assert transformBeforeCallingFlow: 'Need to specify a type of JSON serialization (jackson, map)'
            return transformBeforeCallingFlow.transformOutput(this.inputObject,
                                                              event)
        }
    }

    def transformOutput(EventWrapper event) {
        if (inputOnly) {
            return
        }
        assert transformAfterCallingFlow
        transformAfterCallingFlow.transformInput(event,
                                                 flow)
    }

    Invoker withNewPayloadValidator(IPayloadValidator validator) {
        new JsonInvokerImpl(validator,
                            eventFactory,
                            flow)
    }
}
