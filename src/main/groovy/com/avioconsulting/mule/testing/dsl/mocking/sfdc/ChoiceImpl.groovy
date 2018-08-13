package com.avioconsulting.mule.testing.dsl.mocking.sfdc

import com.avioconsulting.mule.testing.ProcessorLocator
import com.avioconsulting.mule.testing.payloadvalidators.ListGenericPayloadValidator
import com.avioconsulting.mule.testing.transformers.StandardDsqlTransformer
import com.avioconsulting.mule.testing.transformers.sfdc.UpsertTransformer
import org.mule.api.MuleContext
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class ChoiceImpl implements Choice {
    String connectorType
    private MuleMessageTransformer transformer
    private final MuleContext muleContext
    // using a factory because this class handles multiple SFDC operations and we don't know the connector type
    // until these methods run
    private final Closure spyFactory
    private final ProcessorLocator processorLocator

    ChoiceImpl(MuleContext muleContext,
               Closure spyFactory,
               ProcessorLocator processorLocator) {
        this.processorLocator = processorLocator
        this.spyFactory = spyFactory
        this.muleContext = muleContext
    }

    def upsert(@DelegatesTo(UpsertResponseUtil) Closure closure) {
        this.connectorType = 'upsert'
        this.transformer = new UpsertTransformer(closure,
                                                 this.muleContext)
    }

    def query(Closure closure) {
        this.connectorType = 'query'
        //def spy = spyFactory(this.connectorType) as MunitSpy
        def validator = new ListGenericPayloadValidator(Map)
        def queryTransformer = new StandardDsqlTransformer(processorLocator,
                                                           muleContext,
                                                           closure,
                                                           validator)
        this.transformer = queryTransformer
        spy.before(queryTransformer)
        return null
    }

    MuleMessageTransformer getTransformer() {
        assert transformer: 'Need a transformer! Did you declare a closure wrapped in (upsert, etc.) ?'
        this.transformer
    }
}
