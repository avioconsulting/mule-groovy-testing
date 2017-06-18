package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.spies.IReceiveMuleEvents
import org.mule.api.MessagingException
import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class HttpConnectorErrorTransformer implements MuleMessageTransformer, IReceiveMuleEvents {
    private final MuleContext muleContext
    private boolean triggerException
    private MuleEvent muleEvent

    HttpConnectorErrorTransformer(MuleContext muleContext) {
        this.muleContext = muleContext
    }

    def triggerException() {
        this.triggerException = true
    }

    MuleMessage transform(MuleMessage muleMessage) {
        if (!triggerException) {
            return muleMessage
        }
        throw new MessagingException(this.muleEvent,
                                     new ConnectException('could not reach HTTP server'))
    }

    def receive(MuleEvent muleEvent) {
        this.muleEvent = muleEvent
    }
}
