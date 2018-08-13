package org.mule.munit.common.mocking

import org.mule.api.MuleEvent
import org.mule.api.MuleException

interface SpyProcess {
    void spy(MuleEvent var1) throws MuleException
}