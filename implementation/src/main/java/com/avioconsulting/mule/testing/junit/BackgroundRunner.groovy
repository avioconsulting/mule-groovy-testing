package com.avioconsulting.mule.testing.junit

import com.avioconsulting.mule.testing.muleinterfaces.containers.BaseEngineConfig
import com.avioconsulting.mule.testing.muleinterfaces.containers.MuleEngineContainer

class BackgroundRunner {
    static void main(String[] args) {
        def config = new BaseEngineConfig(BaseEngineConfig.defaultFilters,
                                          true)
        new MuleEngineContainer(config)
    }
}
