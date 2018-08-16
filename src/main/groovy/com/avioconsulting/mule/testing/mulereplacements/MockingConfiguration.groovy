package com.avioconsulting.mule.testing.mulereplacements

import org.mule.api.AnnotatedObject

import javax.xml.namespace.QName

class MockingConfiguration {
    static final QName processorName = new QName('http://www.mulesoft.org/schema/mule/documentation',
                                                 'name')
    private final Map<String, MockProcess> mocks = [:]

    def clearMocks() {
        mocks.clear()
    }

    def addMock(String processorName,
                MockProcess mockHandler) {
        mocks[processorName] = mockHandler
    }

    MockProcess getMockProcess(String processorName) {
        mocks[processorName]
    }

    MockProcess getMockProcess(AnnotatedObject processor) {
        def processorName = processor.annotations.get(processorName) as String
        mocks[processorName]
    }
}
