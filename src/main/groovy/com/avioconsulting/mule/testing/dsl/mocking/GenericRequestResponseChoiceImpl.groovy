package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.MessageFactory
import com.avioconsulting.mule.testing.TransformingEventFactory
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.payloadvalidators.NoopValidator
import com.avioconsulting.mule.testing.transformers.ClosureCurrierNoop

class GenericRequestResponseChoiceImpl<T extends ConnectorInfo> extends
        StandardRequestResponseImpl<T> {
    GenericRequestResponseChoiceImpl(MessageFactory messageFactory,
                                     TransformingEventFactory eventFactory) {
        super(messageFactory,
              new NoopValidator<T>(),
              new ClosureCurrierNoop<T>(),
              'Generic Mock',
              eventFactory)
    }
}
