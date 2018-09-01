package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import org.mule.runtime.core.api.event.CoreEvent

interface Invoker {
    CoreEvent getEvent()

    def transformOutput(CoreEvent event)

    Invoker withNewPayloadValidator(IPayloadValidator validator)

    IPayloadValidator getPayloadValidator()
}