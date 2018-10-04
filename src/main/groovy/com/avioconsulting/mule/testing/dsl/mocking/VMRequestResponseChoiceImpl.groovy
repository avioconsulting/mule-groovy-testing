package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.payloadvalidators.VmPayloadValidator
import com.avioconsulting.mule.testing.transformers.ClosureCurrierNoop

class VMRequestResponseChoiceImpl extends
        StandardRequestResponseImpl {
    VMRequestResponseChoiceImpl() {
        super(new VmPayloadValidator(),
              new ClosureCurrierNoop(),
              'VM Mock')
    }
}
