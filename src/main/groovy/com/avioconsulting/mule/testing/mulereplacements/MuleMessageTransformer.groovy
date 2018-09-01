package com.avioconsulting.mule.testing.mulereplacements

import org.mule.runtime.core.api.event.CoreEvent
import org.mule.runtime.core.api.processor.Processor

// TODO: Collapse this and MockProcess??
trait MuleMessageTransformer implements MockProcess<Processor> {
    abstract CoreEvent transform(CoreEvent var1,
                                 Processor originalProcessor)

    CoreEvent process(CoreEvent event,
                      Processor originalProcessor) {
        transform(event, originalProcessor)
    }
}