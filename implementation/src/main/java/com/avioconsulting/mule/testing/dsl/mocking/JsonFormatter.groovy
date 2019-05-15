package com.avioconsulting.mule.testing.dsl.mocking

interface JsonFormatter {
    def whenCalledWith(@DelegatesTo(ErrorThrowing) Closure closure)

    def whenCalledWith(@DelegatesTo(ErrorThrowing) Class inputClass,
                       Closure closure)

    def nonRepeatableStream()
}
