package com.avioconsulting.mule.testing.dsl.invokers

interface JsonInvoker {
    def inputPayload(inputObject)

    def inputPayload(inputObject, Class outputClass)

    def inputOnly(inputObject)

    def outputOnly(Class outputClass)

    def nonRepeatableStream()
}