package com.avioconsulting.mule.testing.muleinterfaces.wrappers

class InvokeExceptionWrapper extends Exception {
    InvokeExceptionWrapper(Exception cause) {
        super('Problem invoking flow',
              cause)
    }

    /**
     * If you want to make assertions on what any error handlers might have changed the payload to
     * use this method
     * @return
     */
    MessageWrapper getMuleMessage() {
        assert cause.getClass().getName().contains('MessagingException')
        new MessageWrapperImpl(cause.muleMessage)
    }
}
