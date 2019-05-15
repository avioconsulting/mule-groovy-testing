package com.avioconsulting.mule.testing.dsl.mocking

import javax.xml.namespace.QName

interface SoapErrorThrowing extends ErrorThrowing {
    def soapFault(String message,
                  QName faultCode,
                  QName subCode)

    def soapFault(String message,
                  QName faultCode,
                  QName subCode,
                  Closure detailMarkupBuilderClosure)
}
