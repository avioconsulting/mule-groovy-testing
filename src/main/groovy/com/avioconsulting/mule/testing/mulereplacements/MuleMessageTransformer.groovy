package com.avioconsulting.mule.testing.mulereplacements

import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

interface MuleMessageTransformer {
    MuleEvent transform(MuleEvent var1,
                        MessageProcessor originalProcessor)
}