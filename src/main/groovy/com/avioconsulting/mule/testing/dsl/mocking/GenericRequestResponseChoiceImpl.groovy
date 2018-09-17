package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.payloadvalidators.NoopValidator
import com.avioconsulting.mule.testing.transformers.ClosureCurrierNoop

class GenericRequestResponseChoiceImpl extends StandardRequestResponseImpl {
    GenericRequestResponseChoiceImpl(InvokerEventFactory eventFactory) {
        super(new NoopValidator(),
              eventFactory,
              new ClosureCurrierNoop(),
              'Generic Mock')
    }
}
