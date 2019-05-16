package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors.SoapConsumerInfo

class SOAPRequestResponseImpl extends
        StandardRequestResponseImpl<SoapConsumerInfo> {

    SOAPRequestResponseImpl(Closure closure) {
        def soapFormatter = new SOAPFormatterImpl()
        useFormatter(soapFormatter,
                     closure)
    }
}
