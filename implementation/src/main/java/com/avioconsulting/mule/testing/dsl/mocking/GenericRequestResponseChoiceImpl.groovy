package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo

class GenericRequestResponseChoiceImpl<T extends ConnectorInfo> extends
        StandardRequestResponseImpl<T> {
}
