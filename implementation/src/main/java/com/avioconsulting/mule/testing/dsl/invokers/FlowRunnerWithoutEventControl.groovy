package com.avioconsulting.mule.testing.dsl.invokers

interface FlowRunnerWithoutEventControl {
    def json(@DelegatesTo(JsonInvoker) Closure closure)

    def java(@DelegatesTo(JavaInvoker) Closure closure)

    def soap(@DelegatesTo(SoapInvoker) Closure closure)
}
