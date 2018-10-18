package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo

interface IFormatter<T extends ConnectorInfo> {
    MuleMessageTransformer<T> getTransformer()
}