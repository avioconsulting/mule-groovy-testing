package com.avioconsulting.mule.testing.transformers.sfdc

import com.avioconsulting.mule.testing.dsl.mocking.sfdc.UpsertResponseUtil
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.payloadvalidators.ListGenericPayloadValidator
import org.mule.DefaultMuleMessage
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer
import org.mule.modules.salesforce.bulk.EnrichedUpsertResult

class UpsertTransformer implements MuleMessageTransformer {
    private final Closure closure
    private final MuleContext muleContext
    private final IPayloadValidator payloadValidator

    UpsertTransformer(@DelegatesTo(UpsertResponseUtil) Closure closure,
                      MuleContext muleContext) {
        this.muleContext = muleContext
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
            name.startsWith('$')
        }
        throw new Exception(
                "Must return a SalesForce result from your mock. Options include ${options}. See ${responseUtilClass} class for options.")
    }

    MuleMessage transform(MuleMessage muleMessage) {
        def payload = muleMessage.payload as List<Map>
        this.payloadValidator.validatePayloadType(payload)
        if (payload.size() > 200) {
            throw new Exception("You can only upsert a maximum of 200 records but you just tried to upsert ${payload.size()} records. Consider using a batch processor?")
        }
        def code = closure.rehydrate(new UpsertResponseUtil(), this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        def result = code(payload)
        validateReturnPayloadList(result,
                                  UpsertResponseUtil,
                                  EnrichedUpsertResult)
        new DefaultMuleMessage(result,
                               this.muleContext)
    }
}
