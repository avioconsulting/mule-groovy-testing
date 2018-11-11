package com.avioconsulting.mule.testing.transformers

// transformer objects are currently reused for each invocation of the mock closure
// this allows the transformer to reset state caused by DSL invocations in the previous state for each
// invocation
interface IHaveStateToReset {
    def reset()
}