package com.avioconsulting.mule.testing.transformers.sfdc

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.dsl.mocking.sfdc.UpsertResponseUtil
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ProcessorWrapper
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.payloadvalidators.ListGenericPayloadValidator

class UpsertTransformer implements MuleMessageTransformer {
    private final Closure closure
    private final IPayloadValidator payloadValidator
    private final InvokerEventFactory eventFactory

    UpsertTransformer(@DelegatesTo(UpsertResponseUtil) Closure closure,
                      InvokerEventFactory eventFactory) {
        this.eventFactory = eventFactory
        this.closure = closure
        this.payloadValidator = new ListGenericPayloadValidator(Map)
    }

    static def validateReturnPayloadList(Object result,
                                         Class responseUtilClass,
                                         Class validReturnType) {
        if (result instanceof List && (result.empty || result[0].class == validReturnType)) {
            return
        }
        def methodNames = { Class klass -> klass.declaredMethods.collect { m -> m.name } }
        def options = (methodNames(responseUtilClass) - methodNames(GroovyObject)).unique().sort()
        options.removeAll { name ->
            // hidden methods
            name.startsWith('$') || name.endsWith('Klass') || name.startsWith('this$')
        }
        throw new Exception(
                "Must return a SalesForce result from your mock. Options include ${options}. See ${responseUtilClass} class for options.")
    }

    EventWrapper transform(EventWrapper muleEvent,
                           ProcessorWrapper messageProcessor) {
        def payload = muleEvent.message.payload as List<Map>
        this.payloadValidator.validatePayloadType(payload)
        if (payload.size() > 200) {
            throw new Exception("You can only upsert a maximum of 200 records but you just tried to upsert ${payload.size()} records. Consider using a batch processor?")
        }
        def code = closure.rehydrate(new UpsertResponseUtil(), this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        def result = code(payload)
        validateReturnPayloadList(result,
                                  UpsertResponseUtil,
                                  UpsertResponseUtil.enrichedUpsertResultKlass)
        eventFactory.getMuleEventWithPayload(result,
                                             muleEvent)
    }
}
