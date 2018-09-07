package com.avioconsulting.mule.testing.mulereplacements

import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ProcessorWrapper

interface MockProcess<T extends ProcessorWrapper> {
    EventWrapper process(EventWrapper event,
                         T originalProcessor)
}
