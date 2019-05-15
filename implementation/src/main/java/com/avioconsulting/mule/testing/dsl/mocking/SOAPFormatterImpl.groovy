package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.transformers.xml.XMLMessageBuilder

class SOAPFormatterImpl extends XMLFormatterImpl implements SOAPFormatter {
    SOAPFormatterImpl() {
        super(XMLMessageBuilder.MessageType.SoapMock)
    }
}
