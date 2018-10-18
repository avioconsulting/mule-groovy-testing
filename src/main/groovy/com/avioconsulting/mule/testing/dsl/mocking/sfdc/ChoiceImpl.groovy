package com.avioconsulting.mule.testing.dsl.mocking.sfdc

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.mocks.DsqlMock
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.transformers.sfdc.UpsertTransformer
import org.mule.runtime.core.api.MuleContext

class ChoiceImpl<T extends ConnectorInfo> implements
        Choice {
    private final MuleContext muleContext
    private MuleMessageTransformer<T> mock
    private final InvokerEventFactory eventFactory

    ChoiceImpl(MuleContext muleContext,
               InvokerEventFactory eventFactory) {
        this.eventFactory = eventFactory
        this.muleContext = muleContext
    }

    MuleMessageTransformer<T> getMock() {
        mock
    }

    def upsert(@DelegatesTo(UpsertResponseUtil) Closure closure) {
        this.mock = new UpsertTransformer(closure,
                                          eventFactory)
        return null
    }

    def query(Closure closure) {
        this.mock = new DsqlMock(muleContext,
                                 closure,
                                 eventFactory)
        return null
    }
}
