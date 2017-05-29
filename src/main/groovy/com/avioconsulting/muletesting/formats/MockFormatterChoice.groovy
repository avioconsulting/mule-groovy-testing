package com.avioconsulting.muletesting.formats

import org.mule.munit.common.mocking.MessageProcessorMocker

class MockFormatterChoice {
    private final MessageProcessorMocker muleMocker

    MockFormatterChoice(MessageProcessorMocker muleMocker) {
        this.muleMocker = muleMocker
    }

    def json() {
        println 'we can do JSON!'
    }
}
