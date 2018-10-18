package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.muleinterfaces.MuleMessageTransformer
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo

interface IFormatter<T extends ConnectorInfo> {
    MuleMessageTransformer<T> getTransformer()
}