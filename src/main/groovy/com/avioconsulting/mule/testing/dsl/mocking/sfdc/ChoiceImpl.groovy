package com.avioconsulting.mule.testing.dsl.mocking.sfdc

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.mocks.DsqlMock
import com.avioconsulting.mule.testing.mocks.StandardMock
import com.avioconsulting.mule.testing.mulereplacements.MockProcess
import com.avioconsulting.mule.testing.payloadvalidators.ListGenericPayloadValidator
import com.avioconsulting.mule.testing.transformers.sfdc.UpsertTransformer
import org.mule.api.MuleContext

class ChoiceImpl implements Choice {
    private final MuleContext muleContext
    private MockProcess mock
    private final EventFactory eventFactory

    ChoiceImpl(MuleContext muleContext,
               EventFactory eventFactory) {
        this.eventFactory = eventFactory
        this.muleContext = muleContext
    }

    MockProcess getMock() {
        mock
    }

    def upsert(@DelegatesTo(UpsertResponseUtil) Closure closure) {
        def transformer = new UpsertTransformer(closure,
                                                this.muleContext)
        this.mock = new StandardMock(transformer,
                                     this.eventFactory)
        return null
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
