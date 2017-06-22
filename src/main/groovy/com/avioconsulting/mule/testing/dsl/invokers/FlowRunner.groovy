package com.avioconsulting.mule.testing.dsl.invokers

interface FlowRunner {
    def json(@DelegatesTo(JsonInvoker) Closure closure)

    def java(@DelegatesTo(JavaInvoker) Closure closure)

    def withOutputEvent(Closure closure)

    def withOutputHttpStatus(Closure closure)

    def withInputEvent(Closure closure)

    def disableContentTypeCheck()
}