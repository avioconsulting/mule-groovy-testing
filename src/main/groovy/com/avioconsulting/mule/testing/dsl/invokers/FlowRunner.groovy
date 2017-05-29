package com.avioconsulting.mule.testing.dsl.invokers

interface FlowRunner {
    def json(@DelegatesTo(JsonInvoker) Closure closure)
}