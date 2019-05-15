package com.avioconsulting.mule.testing.dsl.mocking

interface XMLFormatter {
    def whenCalledWithJaxb(Class inputJaxbClass,
                           @DelegatesTo(ErrorThrowing) Closure closure)

    def whenCalledWithMapAsXml(@DelegatesTo(ErrorThrowing) Closure closure)

    def whenCalledWithGroovyXmlParser(@DelegatesTo(ErrorThrowing) Closure closure)
}
