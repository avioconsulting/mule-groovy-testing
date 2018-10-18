package com.avioconsulting.mule.testing.dsl.mocking

interface XMLFormatter {
    def whenCalledWithJaxb(Class inputJaxbClass,
                           Closure closure)

    def whenCalledWithMapAsXml(Closure closure)

    def whenCalledWithGroovyXmlParser(Closure closure)
}