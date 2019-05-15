package com.avioconsulting.mule.testing.muleinterfaces.wrappers

class EventWrapperImpl implements
        EventWrapper {
    protected final MessageWrapper message
    protected final Object nativeEvent
    protected final Object runtimeBridgeMuleSide

    EventWrapperImpl(Object nativeEvent,
                     Object runtimeBridgeMuleSide) {
        this.runtimeBridgeMuleSide = runtimeBridgeMuleSide
        this.message = new MessageWrapperImpl(nativeEvent.message)
        this.nativeEvent = nativeEvent
    }

    protected EventWrapperImpl(Object existingNativeEvent,
                               MessageWrapper newMessageWrapper,
                               Object runtimeBridgeMuleSide) {
        this.nativeEvent = existingNativeEvent
        this.message = newMessageWrapper
        this.runtimeBridgeMuleSide = runtimeBridgeMuleSide
    }

    protected EventWrapper withNewMessage(MessageWrapperImpl newMessage) {
        def muleMsg = newMessage.muleMessage
        def muleEvent = runtimeBridgeMuleSide.getEventFromOldEvent(muleMsg,
                                                                   this.nativeEvent)
        new EventWrapperImpl(muleEvent,
                             runtimeBridgeMuleSide)
    }

    @Override
    EventWrapper withNewPayload(Object payload,
                                ConnectorInfo connectorInfo,
                                String mediaType) {
        def targetVariable = connectorInfo.targetFlowVariable
        if (targetVariable) {
            return withVariable(targetVariable,
                                payload,
                                mediaType)
        }
        def message = new MessageWrapperImpl(payload,
                                             runtimeBridgeMuleSide,
                                             mediaType)
        withNewMessage(message)
    }

    @Override
    EventWrapper withSoapMockPayload(String xmlPayload,
                                     ConnectorInfo connectorInfo,
                                     Object attributes) {
        return getSoapEvent(xmlPayload,
                            connectorInfo,
                            attributes) { streamTypedValue ->
            def soapOutputPayloadClass = runtimeBridgeMuleSide
                    .getAppClassloader()
                    .loadClass('org.mule.runtime.extension.api.soap.SoapOutputPayload')
            soapOutputPayloadClass.newInstance(streamTypedValue,
                                               [:],
                                               // attachments
                                               [:]) // headers
        }
    }

    @Override
    EventWrapper withSoapInvokePayload(String xmlPayload,
                                       ConnectorInfo connectorInfo,
                                       Object attributes) {
        return getSoapEvent(xmlPayload,
                            connectorInfo,
                            attributes) { streamTypedValue ->
            // ideally we'd build org.mule.module.soapkit.internal.SoapSubFlowPayload
            // but that class is internal so we can't see it, so instead we build a hash map
            // that looks like it
            [
                    body       : streamTypedValue,
                    headers    : [:],
                    attachments: [:]
            ]
        }
    }

    private EventWrapper getSoapEvent(String xmlPayload,
                                      ConnectorInfo connectorInfo,
                                      Object attributes,
                                      Closure finalPayloadConstructor) {
        def stream = new ByteArrayInputStream(xmlPayload.bytes)
        stream = runtimeBridgeMuleSide.getMuleStreamCursor(this.nativeEvent,
                                                           stream)
        def streamTypedValue = runtimeBridgeMuleSide.getSoapTypedValue(stream)
        def soapOutputPayload = finalPayloadConstructor(streamTypedValue)
        def targetVariable = connectorInfo.targetFlowVariable
        if (targetVariable) {
            return withVariable(targetVariable,
                                soapOutputPayload,
                                'application/java')
        }
        def message = new MessageWrapperImpl(soapOutputPayload,
                                             runtimeBridgeMuleSide,
                                             'application/java',
                                             attributes)
        withNewMessage(message)
    }

    @Override
    EventWrapper withNewPayload(Object payload,
                                String mediaType,
                                ConnectorInfo connectorInfo,
                                Object attributes) {
        def targetVariable = connectorInfo.targetFlowVariable
        // if we have a target variable, we should preserve the payload but still update attributes
        def payloadToUse = targetVariable ? this.message.payload : payload
        def mediaTypeTouse = targetVariable ? this.message.mimeType : mediaType
        def message = new MessageWrapperImpl(payloadToUse,
                                             runtimeBridgeMuleSide,
                                             mediaTypeTouse,
                                             attributes)
        def newEvent = withNewMessage(message)
        targetVariable ? newEvent.withVariable(targetVariable,
                                               payload,
                                               mediaType) : newEvent
    }

    @Override
    EventWrapper withNewStreamingPayload(String payload,
                                         String mediaType,
                                         Object attributes,
                                         ConnectorInfo connectorInfo,
                                         boolean useRepeatableStream) {
        def stream = new ByteArrayInputStream(payload.bytes)
        def payloadStream = useRepeatableStream ? runtimeBridgeMuleSide.getMuleStreamCursor(this.nativeEvent,
                                                                                            stream) : stream
        withNewPayload(payloadStream,
                       mediaType,
                       connectorInfo,
                       attributes)
    }

    @Override
    String getCorrelationId() {
        nativeEvent.correlationId
    }

    @Override
    MessageWrapper getMessage() {
        message
    }

    @Override
    String getMessageAsString() {
        message.messageAsString
    }

    @Override
    Object getVariable(String variableName) {
        nativeEvent.getVariables()[variableName]
    }

    @Override
    EventWrapper withVariable(String variableName,
                              Object value) {
        def msg = this.message as MessageWrapperImpl
        def muleEvent = runtimeBridgeMuleSide.getEventFromOldEvent(msg.muleMessage,
                                                                   this.nativeEvent,
                                                                   variableName,
                                                                   value)
        new EventWrapperImpl(muleEvent,
                             runtimeBridgeMuleSide)
    }

    @Override
    EventWrapper withVariable(String variableName,
                              Object value,
                              String mediaType) {
        def msg = this.message as MessageWrapperImpl
        def muleEvent = runtimeBridgeMuleSide.getEventFromOldEvent(msg.muleMessage,
                                                                   this.nativeEvent,
                                                                   variableName,
                                                                   value,
                                                                   mediaType)
        new EventWrapperImpl(muleEvent,
                             runtimeBridgeMuleSide)
    }

    @Override
    EventWrapper withNewAttributes(attributes) {
        def message = new MessageWrapperImpl(this.message.payload,
                                             runtimeBridgeMuleSide,
                                             this.message.mimeType,
                                             attributes)
        withNewMessage(message)
    }

    Object getNativeMuleEvent() {
        nativeEvent
    }


    @Override
    String toString() {
        nativeEvent.toString()
    }
}
