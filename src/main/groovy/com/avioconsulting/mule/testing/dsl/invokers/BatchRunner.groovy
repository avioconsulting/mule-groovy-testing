package com.avioconsulting.mule.testing.dsl.invokers

// main purpose is to only offer a subset of FlowRunner's methods
interface BatchRunner {
    def java(@DelegatesTo(JavaInvoker) Closure closure)

    def json(@DelegatesTo(JsonInvoker) Closure closure)

    def withInputEvent(Closure closure)
}