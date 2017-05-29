package com.avioconsulting.mule.testing.dsl.invokers

interface JsonInvoker {
    def jackson(inputObject)
    def jackson(inputObject, Class outputClass)
    def inputMap(Map input)
    def noStreaming()
}