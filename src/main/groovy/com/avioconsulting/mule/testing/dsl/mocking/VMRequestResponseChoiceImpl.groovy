package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.payload_types.VmPayloadValidator
import org.mule.api.MuleContext
import org.mule.munit.common.mocking.MessageProcessorMocker

class VMRequestResponseChoiceImpl extends StandardRequestResponseImpl {
    VMRequestResponseChoiceImpl(MessageProcessorMocker muleMocker,
                                MuleContext muleContext) {
        super(muleMocker,
              muleContext,
              new VmPayloadValidator())
    }
}
