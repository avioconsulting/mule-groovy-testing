package com.avioconsulting.mule.testing

import org.mule.api.MuleEvent
import org.mule.api.MuleException
import org.mule.munit.common.mocking.SpyProcess

class TestSpyProcess implements SpyProcess {
    private final Closure closure

    TestSpyProcess(Closure closure) {
        this.closure = closure
    }

    void spy(MuleEvent muleEvent) throws MuleException {
        this.closure(muleEvent)
    }
}
