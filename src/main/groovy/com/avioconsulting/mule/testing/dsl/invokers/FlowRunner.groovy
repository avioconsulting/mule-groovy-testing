package com.avioconsulting.mule.testing.dsl.invokers

interface FlowRunner {
    def json(@DelegatesTo(JsonInvoker) Closure closure)

    def withOutputEvent(Closure closure)

    def withOutputHttpStatus(Closure closure)

    def withInputEvent(Closure closure)

    // if APIKit references this flow, then apikit sets the Content-Type, so we should not have to worry about this
    def apiKitReferencesThisFlow()
}