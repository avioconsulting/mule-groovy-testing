package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.payloadvalidators.VmPayloadValidator
import org.mule.api.MuleContext

class VMRequestResponseChoiceImpl extends StandardRequestResponseImpl {
    VMRequestResponseChoiceImpl(MuleContext muleContext) {
        super(muleContext,
              new VmPayloadValidator())
    }
}
