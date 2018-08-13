package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer

interface IFormatter {
    MuleMessageTransformer getTransformer()

    IFormatter withNewPayloadValidator(IPayloadValidator validator)

    IPayloadValidator getPayloadValidator()
}