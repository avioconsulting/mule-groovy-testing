package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.muleinterfaces.MuleMessageTransformer
import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.transformers.xml.XMLGroovyParserTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLJAXBTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLMapTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLMessageBuilder.MessageType
import com.avioconsulting.mule.testing.transformers.xml.XMLTransformer

class XMLFormatterImpl<T extends ConnectorInfo> implements
        XMLFormatter,
        IFormatter<T> {
    protected MuleMessageTransformer<T> transformer
    // TODO: Refactor our error code to be evaluated with a closure context such that you can call httpConnectError() in the deepest closure and have the closure "reverse curried" to add the event and connector to it. then our error transformers can throw directly
    private XMLTransformer xmlTransformer
    private final MessageType messageType
    private final RuntimeBridgeTestSide runtimeBridgeTestSide

    XMLFormatterImpl(RuntimeBridgeTestSide runtimeBridgeTestSide,
                     MessageType messageType = MessageType.Mule41Stream) {
        // most of the time, this should be a sensible default
        this.runtimeBridgeTestSide = runtimeBridgeTestSide
        this.messageType = messageType
    }

    protected def notifyImpendingFault() {
        this.xmlTransformer.notifyImpendingFault()
    }

    def whenCalledWithJaxb(Class inputJaxbClass,
                           Closure closure) {
        def t = new XMLJAXBTransformer<T>(closure,
                                          inputJaxbClass,
                                          messageType,
                                          runtimeBridgeTestSide)
        this.transformer = t
        this.xmlTransformer = t
    }

    def whenCalledWithMapAsXml(Closure closure) {
        def t = new XMLMapTransformer(closure,
                                      messageType,
                                      runtimeBridgeTestSide)
        this.transformer = t
        this.xmlTransformer = t
    }

    def whenCalledWithGroovyXmlParser(Closure closure) {
        def t = new XMLGroovyParserTransformer(closure,
                                               messageType,
                                               runtimeBridgeTestSide)
        this.transformer = t
        this.xmlTransformer = t
    }

    MuleMessageTransformer<T> getTransformer() {
        transformer
    }
}
