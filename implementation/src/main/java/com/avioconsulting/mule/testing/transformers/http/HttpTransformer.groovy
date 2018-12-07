package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors.HttpRequesterInfo

interface HttpTransformer {
    HttpState transform(HttpState httpState,
                        HttpRequesterInfo connectorInfo)
}
