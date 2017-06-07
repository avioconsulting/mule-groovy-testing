package com.avioconsulting.mule.testing.dsl.invokers

interface JsonInvoker {
    def jackson(inputObject)

    def jackson(inputObject, Class outputClass)

    def jackson(Class outputClass)

    def map(Map input)

    def noStreaming()
}