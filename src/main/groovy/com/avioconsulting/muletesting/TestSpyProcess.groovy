package com.avioconsulting.muletesting

import org.mule.api.MuleEvent
import org.mule.api.MuleException
import org.mule.munit.common.mocking.SpyProcess

class TestSpyProcess implements SpyProcess {
    private final Object closure

    def TestSpyProcess(closure) {
        this.closure = closure
    }

    void spy(MuleEvent muleEvent) throws MuleException {
        this.closure(muleEvent)
    }
}
