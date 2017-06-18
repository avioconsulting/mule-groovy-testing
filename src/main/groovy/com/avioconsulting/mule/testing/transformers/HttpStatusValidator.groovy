package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.ProcessorLocator
import org.mule.api.MessagingException
import org.mule.api.MuleEvent
import org.mule.config.i18n.CoreMessages
import org.mule.module.http.internal.request.ResponseValidatorException
import org.mule.module.http.internal.request.SuccessStatusCodeValidator

// overriding this because we need to set the message processor
class HttpStatusValidator extends SuccessStatusCodeValidator {
    private final ProcessorLocator processorLocator

    HttpStatusValidator(SuccessStatusCodeValidator wrapped,
                        ProcessorLocator processorLocator) {
        super(wrapped.values)
        this.processorLocator = processorLocator
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
