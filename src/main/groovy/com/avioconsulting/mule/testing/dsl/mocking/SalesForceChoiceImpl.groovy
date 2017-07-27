package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.transformers.sfdc.UpsertTransformer
import org.mule.api.MuleContext
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class SalesForceChoiceImpl implements SalesForceChoice {
    String connectorType
    Closure closure
    private final MuleContext muleContext

    SalesForceChoiceImpl(MuleContext muleContext) {
        this.muleContext = muleContext
    }

    def withInputPayload(SalesForceCreateConnectorType type,
                         @DelegatesTo(SalesForceResponseUtil) Closure closure) {
        this.connectorType = type.connectorElementName
        this.closure = closure
        return null
    }

    MuleMessageTransformer getTransformer() {
        new UpsertTransformer(this.closure,
                              this.muleContext)
    }
}
