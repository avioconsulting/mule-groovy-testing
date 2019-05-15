package com.avioconsulting.mule.testing.dsl.mocking

interface SOAPFormatter extends
        XMLFormatter {
    def whenCalledWithJaxb(Class inputJaxbClass,
                           @DelegatesTo(SoapErrorThrowing) Closure closure)

    def whenCalledWithMapAsXml(@DelegatesTo(SoapErrorThrowing) Closure closure)

    def whenCalledWithGroovyXmlParser(@DelegatesTo(SoapErrorThrowing) Closure closure)
}
