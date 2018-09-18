package com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo

class SoapConsumerInfo extends ConnectorInfo {
    SoapConsumerInfo(String fileName, Integer lineNumber, Map<String, Object> parameters) {
        super(fileName, lineNumber, parameters)
    }
}
