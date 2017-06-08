package com.avioconsulting.mule.testing.dsl.invokers

interface JsonInvoker {
    def inputPayload(inputObject)

    def inputPayload(inputObject, Class outputClass)

    def outputOnly(Class outputClass)

    def noStreaming()
}