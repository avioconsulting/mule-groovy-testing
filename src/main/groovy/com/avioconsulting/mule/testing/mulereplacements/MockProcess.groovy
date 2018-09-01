package com.avioconsulting.mule.testing.mulereplacements

import org.mule.runtime.core.api.event.CoreEvent
import org.mule.runtime.core.api.processor.Processor

interface MockProcess<T extends Processor> {
    CoreEvent process(CoreEvent event,
                      T originalProcessor)
}
