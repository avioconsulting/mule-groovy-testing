package com.avioconsulting.mule.testing.dsl.mocking

interface SOAPFormatter extends
        XMLFormatter {
    def whenCalledWithJaxb(Class inputJaxbClass,
                           @DelegatesTo(SOAPErrorThrowing) Closure closure)

    def whenCalledWithMapAsXml(@DelegatesTo(SOAPErrorThrowing) Closure closure)

    def whenCalledWithGroovyXmlParser(@DelegatesTo(SOAPErrorThrowing) Closure closure)
}
