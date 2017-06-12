package com.avioconsulting.mule.testing.dsl.mocking.formats

interface XMLFormatter {
    def whenCalledWithJaxb(Class inputJaxbClass,
                           Closure closure)
    def whenCalledWithMapAsXml(Closure closure)
    def whenCalledWithGroovyXmlParser(Closure closure)}