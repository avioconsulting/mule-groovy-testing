package com.avioconsulting.mule.testing.transformers.http


import org.mule.api.MessagingException
import org.mule.api.MuleEvent
import org.mule.module.http.internal.request.ResponseValidatorException
import org.mule.module.http.internal.request.SuccessStatusCodeValidator

// MUnit (Java or likely graphical too) substitutes an interceptor message processor for connectors you mock
// The processor does not have any annotations on it. When we simulate exceptions being thrown, Mule tries
// to obtain details of where the exception occurred and it crashes with an NPE (and the issue is exacerbated
// when in a transaction) or inside a message enricher
// ResponseValidatorException inherits from MessagingException which needs to be instantiated with the
// actual processor that's being mocked as the "failing message processor" rather than the MUnit interceptor
// the easiest way to do that is to wrap the method call and then set the private field
class HttpStatusValidator extends SuccessStatusCodeValidator {

    HttpStatusValidator(SuccessStatusCodeValidator wrapped) {
        super(wrapped.values)
    }

    @Override
    void validate(MuleEvent responseEvent) throws ResponseValidatorException {
        try {
            super.validate(responseEvent)
        }
        catch (ResponseValidatorException e) {
            // in certain cases, we need this populated or Java/Munit tests choke (e.g. inside an enricher)
            def httpRequester = processorLocator.getProcessor(responseEvent)
            // this is a private field and there's no way to set it after and we want
            // to preserve the same exception class
            def field = MessagingException.getDeclaredField('failingMessageProcessor')
            field.accessible = true
            field.set(e, httpRequester)
            throw e
        }
    }
}
