package com.avioconsulting.mule.testing.dsl.invokers

interface FlowRunner extends FlowRunnerWithoutEventControl {
    def withInputEvent(Closure closure)
}
