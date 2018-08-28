package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.payloadvalidators.NoopValidator
import com.avioconsulting.mule.testing.transformers.ClosureCurrierNoop

class GenericRequestResponseChoiceImpl extends StandardRequestResponseImpl {
    GenericRequestResponseChoiceImpl(EventFactory eventFactory) {
        super(new NoopValidator(),
              eventFactory,
              new ClosureCurrierNoop(),
              'Generic Mock')
    }
}
