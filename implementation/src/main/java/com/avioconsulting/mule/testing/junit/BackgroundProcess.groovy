package com.avioconsulting.mule.testing.junit

import com.avioconsulting.mule.testing.muleinterfaces.containers.BaseEngineConfig
import com.avioconsulting.mule.testing.muleinterfaces.containers.MuleEngineContainer

class BackgroundProcess {
    static void main(String[] args) {
        new BackgroundProcess()
    }

    private TestState testState

    BackgroundProcess() {
        def config = new BaseEngineConfig(BaseEngineConfig.defaultFilters,
                                          true)
        def container = new MuleEngineContainer(config)
        this.testState = new TestState(container)
        // ensure any tests we run already see our established state
        BaseJunitTest.testState = testState
        // TODO: IPC mechanism/listener
    }
}
