package com.avioconsulting.mule.testing.mulereplacements

import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

interface MockProcess {
    MuleEvent process(MuleEvent event,
                      MessageProcessor processor)
}
