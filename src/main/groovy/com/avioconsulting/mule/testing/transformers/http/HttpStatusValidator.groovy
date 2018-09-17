package com.avioconsulting.mule.testing.transformers.http

import org.mule.runtime.api.event.Event

// MUnit (Java or likely graphical too) substitutes an interceptor message processor for connectors you mock
// The processor does not have any annotations on it. When we simulate exceptions being thrown, Mule tries
// to obtain details of where the exception occurred and it crashes with an NPE (and the issue is exacerbated
// when in a transaction) or inside a message enricher
// ResponseValidatorException inherits from MessagingException which needs to be instantiated with the
// actual processor that's being mocked as the "failing message processor" rather than the MUnit interceptor
// the easiest way to do that is to wrap the method call and then set the private field
class HttpStatusValidator {//extends SuccessStatusCodeValidator {
//    private final DefaultHttpRequester httpRequester
//
//    HttpStatusValidator(SuccessStatusCodeValidator wrapped,
//                        DefaultHttpRequester httpRequester) {
//        super(wrapped.values)
//        this.httpRequester = httpRequester
//    }

    //@Override
    void validate(Event responseEvent) {// throws ResponseValidatorException {
        try {
            super.validate(responseEvent)
        }
        catch (Exception e) {
            // in certain cases, we need this populated or Java/Munit tests choke (e.g. inside an enricher)
            // this is a private field and there's no way to set it after and we want
            // to preserve the same exception class
            def field = MessagingException.getDeclaredField('failingMessageProcessor')
            field.accessible = true
            field.set(e, httpRequester)
            throw e
        }
    }
}
