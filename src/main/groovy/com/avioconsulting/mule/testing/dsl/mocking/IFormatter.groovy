package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator

interface IFormatter<T extends ConnectorInfo> {
    MuleMessageTransformer<T> getTransformer()

    IFormatter<T> withNewPayloadValidator(IPayloadValidator<T> validator)

    IPayloadValidator<T> getPayloadValidator()
}