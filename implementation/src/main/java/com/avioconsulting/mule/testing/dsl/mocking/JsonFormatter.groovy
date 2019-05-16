package com.avioconsulting.mule.testing.dsl.mocking

interface JsonFormatter {
    def whenCalledWith(@DelegatesTo(ErrorThrowing) Closure closure)

    def whenCalledWith(Class inputClass,
                       @DelegatesTo(ErrorThrowing) Closure closure)

    def nonRepeatableStream()
}
