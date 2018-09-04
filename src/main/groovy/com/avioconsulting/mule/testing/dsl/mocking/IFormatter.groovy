package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator

interface IFormatter {
    MuleMessageTransformer getTransformer()

    IFormatter withNewPayloadValidator(IPayloadValidator validator)

    IPayloadValidator getPayloadValidator()
}