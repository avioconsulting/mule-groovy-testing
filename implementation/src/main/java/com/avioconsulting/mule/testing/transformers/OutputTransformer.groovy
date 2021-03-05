package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper

/***
 * Job is to take the output from the mock's "whenCalledWith" closure and convert it back
 * to a Mule event so flow execution can resume
 */
interface OutputTransformer {
    EventWrapper transformOutput(input,
                                 EventWrapper originalMuleEvent,
                                 ConnectorInfo connectorInfo)
}
