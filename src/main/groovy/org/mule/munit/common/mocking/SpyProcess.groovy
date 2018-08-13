package org.mule.munit.common.mocking

import org.mule.api.MuleEvent
import org.mule.api.MuleException

// TODO: Move to another package
interface SpyProcess {
    void spy(MuleEvent var1) throws MuleException
}