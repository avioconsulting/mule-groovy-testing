package com.avioconsulting.mule.testing.dsl.invokers

interface JsonInvoker {
    def inputJacksonObject(object)
    def outputJacksonClass(Class klass)
    def inputMap(Map input)
    def noStreaming()
}