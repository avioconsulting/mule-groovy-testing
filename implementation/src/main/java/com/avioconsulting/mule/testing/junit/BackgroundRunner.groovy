package com.avioconsulting.mule.testing.junit

import com.avioconsulting.mule.testing.muleinterfaces.containers.BaseEngineConfig
import com.avioconsulting.mule.testing.muleinterfaces.containers.MuleEngineContainer

class BackgroundRunner {
    static void main(String[] args) {
        new BackgroundRunner()
    }

    private TestState testState

    BackgroundRunner() {
        def config = new BaseEngineConfig(BaseEngineConfig.defaultFilters,
                                          true)
        def container = new MuleEngineContainer(config)
        this.testState = new TestState(container)
        // ensure any tests we run already see our established state
        BaseJunitTest.testState = testState
        // TODO: IPC mechanism/listener
    }
}
