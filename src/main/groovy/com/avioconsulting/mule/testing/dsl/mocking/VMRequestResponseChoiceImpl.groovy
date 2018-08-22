package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.payloadvalidators.VmPayloadValidator
import com.avioconsulting.mule.testing.transformers.ClosureCurrierNoop

class VMRequestResponseChoiceImpl extends StandardRequestResponseImpl {
    VMRequestResponseChoiceImpl(EventFactory eventFactory) {
        super(new VmPayloadValidator(),
              eventFactory,
              new ClosureCurrierNoop())
    }
}
