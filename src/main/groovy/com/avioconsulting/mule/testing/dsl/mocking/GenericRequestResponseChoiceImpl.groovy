package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.transformers.ClosureCurrierNoop

class GenericRequestResponseChoiceImpl<T extends ConnectorInfo> extends
        StandardRequestResponseImpl<T> {
    GenericRequestResponseChoiceImpl() {
        super(new ClosureCurrierNoop<T>(),
              'Generic Mock')
    }
}
