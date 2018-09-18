package com.avioconsulting.mule.testing.mulereplacements

import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

// TODO: Collapse this and MockProcess??
trait MuleMessageTransformer implements MockProcess<ProcessorWrapper> {
    abstract EventWrapper transform(EventWrapper var1,
                                    ProcessorWrapper originalProcessor)

    EventWrapper process(EventWrapper event,
                         ProcessorWrapper originalProcessor) {
        transform(event, originalProcessor)
    }
}