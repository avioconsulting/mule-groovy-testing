package com.avioconsulting.mule.testing.dsl.mocking

interface JsonFormatter {
    def whenCalledWith(Closure closure)

    def whenCalledWith(Class inputClass,
                       Closure closure)

    def nonRepeatableStream()
}
