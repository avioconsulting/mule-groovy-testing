package com.avioconsulting.mule.testing.mulereplacements

import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

interface MockProcess<T extends MessageProcessor> {
    MuleEvent processMuleEvent(event,
                               T originalProcessor)
}
