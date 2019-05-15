package com.avioconsulting.mule.testing.transformers.json.output

import com.avioconsulting.mule.testing.muleinterfaces.HttpAttributeBuilder
import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.transformers.OutputTransformer
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Log4j2

@Log4j2
class JacksonOutputTransformer implements
        OutputTransformer,
        HttpAttributeBuilder {
    private boolean useRepeatableStream = true
    def mapper = new ObjectMapper()
    private final RuntimeBridgeTestSide runtimeBridgeTestSide

    def nonRepeatableStream() {
        this.useRepeatableStream = false
    }

    JacksonOutputTransformer(RuntimeBridgeTestSide runtimeBridgeTestSide) {

        this.runtimeBridgeTestSide = runtimeBridgeTestSide
    }

    EventWrapper transformOutput(Object input,
                                 EventWrapper originalMuleEvent,
                                 ConnectorInfo connectorInfo) {
        log.info 'Marshalling object of type {} into JSON',
                 input?.getClass()?.getName()
        def jsonString = getJsonOutput(input)
        log.info 'Marshalled JSON {}',
                 jsonString
        def attributes = getHttpResponseAttributes(200,
                                                   'the reason',
                                                   runtimeBridgeTestSide)
        originalMuleEvent.withNewStreamingPayload(jsonString,
                                                  'application/json',
                                                  attributes,
                                                  connectorInfo,
                                                  this.useRepeatableStream)
    }

    String getJsonOutput(Object response) {
        mapper.writer().writeValueAsString(response)
    }
}
