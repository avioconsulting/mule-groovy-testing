package com.avioconsulting.mule.testing.mocks

import org.mule.munit.common.mocking.MessageProcessorMocker

class BaseMockUtils {
    private whenMessageProcessor

    BaseMockUtils(whenMessageProcessor) {
        this.whenMessageProcessor = whenMessageProcessor
    }

    MessageProcessorMocker whenMessageProcessor(String name) {
        this.whenMessageProcessor(name)
    }
}
