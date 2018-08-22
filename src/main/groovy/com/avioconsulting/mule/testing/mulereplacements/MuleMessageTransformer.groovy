package com.avioconsulting.mule.testing.mulereplacements

import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

// TODO: Collapse this and MockProcess??
trait MuleMessageTransformer implements MockProcess<MessageProcessor> {
    abstract MuleEvent transform(MuleEvent var1,
                                 MessageProcessor originalProcessor)

    MuleEvent process(MuleEvent event,
                      MessageProcessor originalProcessor) {
        transform(event, originalProcessor)
    }
}