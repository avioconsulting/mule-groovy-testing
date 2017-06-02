package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.dsl.mocking.MockedConnectorType
import org.mule.api.MuleContext
import org.mule.api.MuleMessage

class XMLTransformer {
    private final MockedConnectorType mockedConnectorType
    protected final XMLMessageBuilder xmlMessageBuilder

    XMLTransformer(MuleContext muleContext,
                   MockedConnectorType mockedConnectorType) {
        this.xmlMessageBuilder = new XMLMessageBuilder(muleContext)
        this.mockedConnectorType = mockedConnectorType
    }

    def validateContentType(MuleMessage muleMessage) {
        // ws-consumer sets this on its own
        if (mockedConnectorType == MockedConnectorType.SOAP) {
            return
        }
        assert muleMessage.getOutboundProperty(
                'Content-Type') as String == 'application/xml': "Content-Type was not set to 'application/xml' before calling your mock endpoint! Add a set-property"
    }
}
