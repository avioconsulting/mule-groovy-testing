package com.avioconsulting.mule.testing.dsl.mocking

interface StandardRequestResponse {
    def json(@DelegatesTo(JsonFormatter) Closure closure)

    def xml(@DelegatesTo(XMLFormatter) Closure closure)

    def raw(@DelegatesTo(RawFormatter) Closure closure)
}