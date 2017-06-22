package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import org.mule.modules.interceptor.processors.MuleMessageTransformer

interface IFormatter {
    MuleMessageTransformer getTransformer()

    IFormatter withNewPayloadValidator(IPayloadValidator validator)

    IPayloadValidator getPayloadValidator()
}