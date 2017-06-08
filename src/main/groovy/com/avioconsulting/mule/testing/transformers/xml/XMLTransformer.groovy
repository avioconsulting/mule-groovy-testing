package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.dsl.ConnectorType
import org.mule.api.MuleContext
import org.mule.api.MuleMessage

class XMLTransformer {
    private final ConnectorType connectorType
    protected final XMLMessageBuilder xmlMessageBuilder

    XMLTransformer(MuleContext muleContext,
                   ConnectorType connectorType) {
        this.xmlMessageBuilder = new XMLMessageBuilder(muleContext)
        this.connectorType = connectorType
    }

    def validateContentType(MuleMessage muleMessage) {
        // ws-consumer sets this on its own
        if (connectorType == ConnectorType.SOAP) {
            return
        }
        assert muleMessage.getOutboundProperty(
                'Content-Type') as String == 'application/xml': "Content-Type was not set to 'application/xml' before calling your mock endpoint! Add a set-property"
    }
}
