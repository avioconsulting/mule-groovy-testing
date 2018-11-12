package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.FlowWrapper
import com.avioconsulting.mule.testing.transformers.InputTransformer
import com.avioconsulting.mule.testing.transformers.StringInputTransformer
import com.avioconsulting.mule.testing.transformers.json.input.JacksonInputTransformer
import com.avioconsulting.mule.testing.transformers.json.output.JacksonOutputTransformer

class JsonInvokerImpl implements
        JsonInvoker,
        Invoker {
    private JacksonOutputTransformer transformBeforeCallingFlow
    private InputTransformer transformAfterCallingFlow
    private inputObject
    private boolean outputOnly
    private boolean inputOnly
    private final InvokerEventFactory invokerEventFactory
    private final FlowWrapper flow

    JsonInvokerImpl(InvokerEventFactory invokerEventFactory,
                    FlowWrapper flow) {
        this.flow = flow
        this.invokerEventFactory = invokerEventFactory
        this.outputOnly = false
        this.inputOnly = false
    }

    def inputPayload(Object inputObject) {
        setInputTransformer(inputObject)
        transformAfterCallingFlow = new JacksonInputTransformer([Map, Map[]])
    }

    def inputPayload(Object inputObject,
                     Class outputClass) {
        setInputTransformer(inputObject)
        if (outputClass == String) {
            transformAfterCallingFlow = new StringInputTransformer()
        } else {
            setJacksonOutputTransformer(outputClass)
        }
    }

    def inputOnly(Object inputObject) {
        this.inputOnly = true
        setInputTransformer(inputObject)
    }

    private void setJacksonOutputTransformer(Class outputClass) {
        transformAfterCallingFlow = new JacksonInputTransformer(outputClass)
    }

    def outputOnly(Class outputClass) {
        // Jackson handles maps too
        setJacksonOutputTransformer(outputClass)
        outputOnly = true
    }

    @Override
    def nonRepeatableStream() {
        transformBeforeCallingFlow.nonRepeatableStream()
        return null
    }

    private setInputTransformer(inputObject) {
        assert !(inputObject instanceof Class): 'Use outputOnly if a only an output class is being supplied!'
        this.inputObject = inputObject
        transformBeforeCallingFlow = new JacksonOutputTransformer()
    }

    EventWrapper getEvent() {
        def input = outputOnly ? null : this.inputObject
        def event = invokerEventFactory.getMuleEventWithPayload(input,
                                                                flow.name)
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
}
