package com.avioconsulting.mule.testing.dsl.mocking

interface RawFormatter {
    def whenCalledWith(@DelegatesTo(ErrorThrowing) Closure closure)
}
