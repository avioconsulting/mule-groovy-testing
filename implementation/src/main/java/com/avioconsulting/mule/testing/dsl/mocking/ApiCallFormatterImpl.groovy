package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.muleinterfaces.MuleMessageTransformer
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.transformers.ApiTransformer

class ApiCallFormatterImpl<T extends ConnectorInfo> implements
        RawFormatter,
        IFormatter {
    private ApiTransformer apiTransformer

    @Override
    MuleMessageTransformer getTransformer() {
        this.apiTransformer
    }

    @Override
    def whenCalledWith(Closure closure) {
        this.apiTransformer = new ApiTransformer(closure)
    }
}
