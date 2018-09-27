package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.MessageFactory
import com.avioconsulting.mule.testing.TransformingEventFactory
import com.avioconsulting.mule.testing.payloadvalidators.VmPayloadValidator
import com.avioconsulting.mule.testing.transformers.ClosureCurrierNoop

class VMRequestResponseChoiceImpl extends
        StandardRequestResponseImpl {
    VMRequestResponseChoiceImpl(MessageFactory messageFactory,
                                TransformingEventFactory eventFactory) {
        super(messageFactory,
              new VmPayloadValidator(),
              new ClosureCurrierNoop(),
              'VM Mock',
              eventFactory)
    }
}
