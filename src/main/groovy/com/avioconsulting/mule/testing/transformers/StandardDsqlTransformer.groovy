package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.ProcessorLocator
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.spies.DsqlBasedSpy
import org.mule.DefaultMuleMessage
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer

class StandardDsqlTransformer extends DsqlBasedSpy implements MuleMessageTransformer {
    private final Closure closure
    private final IPayloadValidator payloadValidator

    StandardDsqlTransformer(ProcessorLocator locator,
                            MuleContext muleContext,
                            Closure closure,
                            IPayloadValidator payloadValidator) {
        super(locator, muleContext)
        this.payloadValidator = payloadValidator
        this.closure = closure
    }

    MuleMessage transform(MuleMessage muleMessage) {
        def result = closure(this.dSqlQuery)
        if (payloadValidator.payloadTypeValidationRequired) {
            payloadValidator.validatePayloadType(result)
        }
        new DefaultMuleMessage(result,
                               this.muleContext)
    }
}
