package com.avioconsulting.mule.testing.dsl.mocking.sfdc

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.mocks.DsqlMock
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.payloadvalidators.ListGenericPayloadValidator
import com.avioconsulting.mule.testing.transformers.sfdc.UpsertTransformer
import org.mule.api.MuleContext

class ChoiceImpl implements Choice {
    private MuleMessageTransformer transformer
    private final MuleContext muleContext
    private DsqlMock mock
    private final EventFactory eventFactory

    ChoiceImpl(MuleContext muleContext,
               EventFactory eventFactory) {
        this.eventFactory = eventFactory
        this.muleContext = muleContext
    }

    DsqlMock getMock() {
        mock
    }

    def upsert(@DelegatesTo(UpsertResponseUtil) Closure closure) {
        this.transformer = new UpsertTransformer(closure,
                                                 this.muleContext)
    }

    def query(Closure closure) {
        def validator = new ListGenericPayloadValidator(Map)
        this.mock = new DsqlMock(muleContext,
                                 closure,
                                 validator,
                                 eventFactory)
        return null
    }
}
