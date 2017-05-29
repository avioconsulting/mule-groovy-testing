package com.avioconsulting.mule.testing.mocks

import com.avioconsulting.mule.testing.formats.MockFormatterChoice
import org.mule.munit.common.mocking.Attribute
import org.mule.munit.common.mocking.MessageProcessorMocker

class HTTPMock {
    private final BaseMockUtils baseMockUtils
    private final String connectorName

    HTTPMock(String connectorName,
             BaseMockUtils baseMockUtils) {
        this.connectorName = connectorName
        this.baseMockUtils = baseMockUtils
    }

    def post(@DelegatesTo(MockFormatterChoice) Closure closure) {
        def mock = getHttpRequestMock(connectorName)
        def formatterChoice = new MockFormatterChoice(mock)
        def code = closure.rehydrate(formatterChoice, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
    }

    private MessageProcessorMocker getHttpRequestMock(String name) {
        baseMockUtils.doWhenMessageProcessor('request')
                .ofNamespace('http')
                .withAttributes(Attribute.attribute('name').ofNamespace('doc').withValue(name))
    }
}