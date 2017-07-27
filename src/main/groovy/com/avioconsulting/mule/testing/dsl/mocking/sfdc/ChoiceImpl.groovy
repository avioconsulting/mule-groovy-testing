package com.avioconsulting.mule.testing.dsl.mocking.sfdc

import com.avioconsulting.mule.testing.transformers.sfdc.UpsertTransformer
import org.mule.api.MuleContext
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class ChoiceImpl implements Choice {
    String connectorType
    private MuleMessageTransformer transformer
    private final MuleContext muleContext

    ChoiceImpl(MuleContext muleContext) {
        this.muleContext = muleContext
    }

    def upsert(@DelegatesTo(UpsertResponseUtil) Closure closure) {
        this.connectorType = 'upsert'
        this.transformer = new UpsertTransformer(closure,
                                                 this.muleContext)
    }

    def query(@DelegatesTo(Query) Closure closure) {
        return null
    }

    MuleMessageTransformer getTransformer() {
        assert transformer: 'Need a transformer! Did you declare a closure wrapped in (upsert, etc.) ?'
        this.transformer
    }
}
