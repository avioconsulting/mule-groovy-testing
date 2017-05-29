package com.avioconsulting.mule.testing.messages

import org.mule.DefaultMuleMessage
import org.mule.api.MuleContext
import org.mule.api.MuleMessage

trait JsonMessage {
    MuleMessage getJSONMessage(String jsonString,
                               MuleContext muleContext,
                               Integer httpStatus = 200,
                               boolean useStream = true) {
        def messageProps = [
                'content-type': 'application/json; charset=utf-8'
        ]
        if (httpStatus != null) {
            messageProps['http.status'] = httpStatus
        }
        def payload = useStream ? new ByteArrayInputStream(jsonString.bytes) : jsonString
        new DefaultMuleMessage(payload,
                               messageProps,
                               null,
                               null,
                               muleContext)
    }
}