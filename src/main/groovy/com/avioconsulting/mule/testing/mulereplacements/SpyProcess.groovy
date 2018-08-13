package com.avioconsulting.mule.testing.mulereplacements

import org.mule.api.MuleEvent
import org.mule.api.MuleException

// TODO: Move to another package
interface SpyProcess {
    void spy(MuleEvent var1) throws MuleException
}