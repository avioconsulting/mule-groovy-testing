package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.spies.IReceiveHttpOptions
import org.mule.DefaultMuleMessage
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.module.http.internal.request.ResponseValidator
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class HttpGetTransformer implements MuleMessageTransformer, IReceiveHttpOptions {
    private String httpVerb
    private final MuleContext muleContext

    HttpGetTransformer(MuleContext muleContext) {
        this.muleContext = muleContext
    }

    def receive(Map queryParams,
                Map headers,
                String fullPath,
                String httpVerb,
                ResponseValidator responseValidator) {
        this.httpVerb = httpVerb
    }

    MuleMessage transform(MuleMessage muleMessage) {
        // for GET requests, we don't want to pass on the payload
        this.httpVerb == 'GET' ? new DefaultMuleMessage(null,
                                                        muleContext) : muleMessage
    }
}
