package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import org.mule.runtime.core.api.event.CoreEvent
import org.mule.runtime.core.api.processor.Processor

// TODO: Find a better way to do this
class SoapFaultTransformer implements MuleMessageTransformer {
    @Lazy
    private static Class soapFaultExceptionClass = {
        try {
            SoapFaultTransformer.classLoader.loadClass('org.mule.module.ws.consumer.SoapFaultException')
        }
        catch (e) {
            throw new Exception('Was not able to load SoapFaultException properly. You need to have mule-module-ws in your project to use the XML functions. Consider adding the org.mule.modules:mule-module-ws:jar:3.9.1 dependency with at least test scope to your project',
                                e)
        }
    }()

    private CoreEvent muleEvent
    private Processor originalProcessor

    Throwable createSoapFaultException(soapFault) {
        soapFaultExceptionClass.newInstance(muleEvent,
                                            soapFault,
                                            originalProcessor)
    }

    @Override
    CoreEvent transform(CoreEvent muleEvent,
                        Processor originalProcessor) {
        // TODO: thread safe
        // we need to capture these so we can create an exception inside the closure
        this.muleEvent = muleEvent
        this.originalProcessor = originalProcessor
        muleEvent
    }
}
