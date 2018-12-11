package com.avioconsulting.mule.testing.muleinterfaces.wrappers

class InvokeExceptionWrapper extends Exception {
    private final MessageWrapper muleMessage
    private final EventWrapper muleEvent

    InvokeExceptionWrapper(Exception cause,
                           MessageWrapper muleMessage,
                           EventWrapper muleEvent) {
        super(cause)
        this.muleEvent = muleEvent
        this.muleMessage = muleMessage
    }

    /**
     * If you want to make assertions on what any error handlers might have changed the payload to
     * use this method
     * @return
     */
    MessageWrapper getMuleMessage() {
        this.muleMessage
    }

    EventWrapper getMuleEvent() {
        this.muleEvent
    }
}
