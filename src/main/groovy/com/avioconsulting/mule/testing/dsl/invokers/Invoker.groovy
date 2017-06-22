package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import org.mule.api.MuleEvent

interface Invoker {
    MuleEvent getEvent()

    def transformOutput(MuleEvent event)

    Invoker withNewPayloadValidator(IPayloadValidator validator)

    IPayloadValidator getPayloadValidator()
}