package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.spies.IReceiveHttpOptions
import org.mule.DefaultMuleMessage
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.module.http.internal.request.DefaultHttpRequester

class HttpGetTransformer implements MuleMessageTransformer, IReceiveHttpOptions {
    private String httpVerb
    private final MuleContext muleContext

    HttpGetTransformer(MuleContext muleContext) {
        this.muleContext = muleContext
    }

    def receive(Map queryParams,
                Map headers,
                String fullPath,
                DefaultHttpRequester httpRequester) {
        this.httpVerb = httpRequester.method
    }

    MuleMessage transform(MuleMessage muleMessage) {
        // TODO: Centralize message creation just like we did for events
        // for GET requests, we don't want to pass on the payload
        this.httpVerb == 'GET' ? new DefaultMuleMessage(null,
                                                        muleContext) : muleMessage
    }
}
