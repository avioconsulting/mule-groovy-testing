package com.avioconsulting.mule.testing.dsl.invokers

interface FlowRunner extends FlowRunnerWithoutEventControl {
    def withOutputEvent(Closure closure)

    def withOutputHttpStatus(Closure closure)

    def withInputEvent(Closure closure)
}
