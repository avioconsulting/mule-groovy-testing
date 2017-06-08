package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.dsl.mocking.ConnectorType
import com.avioconsulting.mule.testing.runners.RunnerConfig
import com.avioconsulting.mule.testing.transformers.InputTransformer
import com.avioconsulting.mule.testing.transformers.OutputTransformer
import com.avioconsulting.mule.testing.transformers.StringInputTransformer
import com.avioconsulting.mule.testing.transformers.json.input.JacksonInputTransformer
import com.avioconsulting.mule.testing.transformers.json.input.MapInputTransformer
import com.avioconsulting.mule.testing.transformers.json.output.JacksonOutputTransformer
import com.avioconsulting.mule.testing.transformers.json.output.MapOutputTransformer
import org.mule.DefaultMuleEvent
import org.mule.MessageExchangePattern
import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.munit.common.util.MunitMuleTestUtils

class JsonInvokerImpl implements JsonInvoker, Invoker {
    private final MuleContext muleContext
    private final RunnerConfig runnerConfig
    private OutputTransformer transformBeforeCallingFlow
    private InputTransformer transformAfterCallingFlow
    private inputObject
    private static final List<Class> allowedPayloadTypes = [InputStream]

    JsonInvokerImpl(MuleContext muleContext,
                    RunnerConfig runnerConfig) {
        this.runnerConfig = runnerConfig
        this.muleContext = muleContext
    }

    def inputPayload(Object inputObject) {
        setInputTransformer(inputObject)
        transformAfterCallingFlow = new MapInputTransformer(muleContext,
                                                            ConnectorType.HTTP_LISTENER,
                                                            allowedPayloadTypes)
    }

    def inputPayload(Object inputObject,
                     Class outputClass) {
        setInputTransformer(inputObject)
        if (outputClass == String) {
            transformAfterCallingFlow = new StringInputTransformer(ConnectorType.HTTP_LISTENER,
                                                                   muleContext)
        } else {
            setJacksonOutputTransformer(outputClass)
        }
    }

    private void setJacksonOutputTransformer(Class outputClass) {
        transformAfterCallingFlow = new JacksonInputTransformer(muleContext,
                                                                ConnectorType.HTTP_LISTENER,
                                                                allowedPayloadTypes,
                                                                outputClass)
    }

    def outputOnly(Class outputClass) {
        setJacksonOutputTransformer(outputClass)
    }

    private setInputTransformer(inputObject) {
        this.inputObject = inputObject
        if (inputObject instanceof Map) {
            transformBeforeCallingFlow = new MapOutputTransformer(muleContext)
        } else {
            transformBeforeCallingFlow = new JacksonOutputTransformer(muleContext)
        }
    }

    def noStreaming() {
        assert transformAfterCallingFlow: 'Need to specify a type of JSON serialization (jackson, map) first!'
        transformAfterCallingFlow.disableStreaming()
        assert transformBeforeCallingFlow: 'Need to specify a type of JSON serialization (jackson, map) first!'
        transformBeforeCallingFlow.disableStreaming()
    }

    MuleEvent getEvent() {
        assert transformBeforeCallingFlow: 'Need to specify a type of JSON serialization (jackson, map)'
        def inputMessage = transformBeforeCallingFlow.transformOutput(this.inputObject)
        new DefaultMuleEvent(inputMessage,
                             MessageExchangePattern.REQUEST_RESPONSE,
                             MunitMuleTestUtils.getTestFlow(muleContext))
    }

    def transformOutput(MuleEvent event) {
        assert transformAfterCallingFlow
        // filters return null events
        if (event == null) {
            return null
        }
        transformAfterCallingFlow.transformInput(event.message)
    }
}
