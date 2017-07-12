package com.avioconsulting.mule.testing.dsl.mocking

import org.mule.modules.interceptor.processors.MuleMessageTransformer

class SalesForceChoiceImpl implements SalesForceChoice {
    String connectorType

    def withInputPayload(SalesForceCreateConnectorType type, Closure closure) {
        return null
    }

    MuleMessageTransformer getTransformer() {

    }
}
