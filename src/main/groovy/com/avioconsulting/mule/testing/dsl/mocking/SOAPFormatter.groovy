package com.avioconsulting.mule.testing.dsl.mocking

import javax.xml.namespace.QName

interface SOAPFormatter extends XMLFormatter {
    def httpConnectError()

    def httpTimeoutError()

    def soapFault(QName faultCode,
                  QName subCode,
                  Node detail)
}
