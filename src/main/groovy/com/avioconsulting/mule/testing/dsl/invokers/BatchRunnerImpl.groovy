package com.avioconsulting.mule.testing.dsl.invokers

import org.mule.api.MuleContext

class BatchRunnerImpl extends FlowRunnerImpl implements BatchRunner {
    BatchRunnerImpl(MuleContext muleContext) {
        super(muleContext)
    }
}
