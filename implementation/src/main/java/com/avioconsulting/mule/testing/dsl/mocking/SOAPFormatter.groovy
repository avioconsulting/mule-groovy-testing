package com.avioconsulting.mule.testing.dsl.mocking

import javax.xml.namespace.QName

interface SOAPFormatter extends
        XMLFormatter {
    // TODO: All of these methods belong in closure context (see XMLFormatterImpl)
    @Deprecated()
    def httpConnectError()

    @Deprecated()
    def httpTimeoutError()

    @Deprecated()
    def soapFault(String message,
                  QName faultCode,
                  QName subCode)

    @Deprecated()
    def soapFault(String message,
                  QName faultCode,
                  QName subCode,
                  Closure detailMarkupBuilderClosure)
}
