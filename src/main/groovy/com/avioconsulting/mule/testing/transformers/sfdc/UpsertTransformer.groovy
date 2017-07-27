package com.avioconsulting.mule.testing.transformers.sfdc

import com.avioconsulting.mule.testing.dsl.mocking.SalesForceResponseUtil
import org.mule.DefaultMuleMessage
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class UpsertTransformer implements MuleMessageTransformer {
    private final Closure closure
    private final MuleContext muleContext

    UpsertTransformer(@DelegatesTo(SalesForceResponseUtil) Closure closure,
                      MuleContext muleContext) {
        this.muleContext = muleContext
        this.closure = closure
    }

    MuleMessage transform(MuleMessage muleMessage) {
        def payload = muleMessage.payload
        assert payload instanceof Map: 'Expect SFDC payloads to be maps!'
        def code = closure.rehydrate(new SalesForceResponseUtil(), this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        def result = code(payload)
        new DefaultMuleMessage(result,
                               this.muleContext)
    }
}
