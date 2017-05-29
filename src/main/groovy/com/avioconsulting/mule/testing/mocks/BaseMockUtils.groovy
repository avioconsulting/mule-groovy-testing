package com.avioconsulting.mule.testing.mocks

import org.mule.munit.common.mocking.MessageProcessorMocker

class BaseMockUtils {
    private whenMessageProcessor

    BaseMockUtils(whenMessageProcessor) {
        this.whenMessageProcessor = whenMessageProcessor
    }

    MessageProcessorMocker doWhenMessageProcessor(String name) {
        this.whenMessageProcessor(name)
    }
}
