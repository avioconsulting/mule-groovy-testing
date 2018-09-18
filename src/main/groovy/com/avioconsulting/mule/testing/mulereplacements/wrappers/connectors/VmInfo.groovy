package com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo

class VmInfo extends ConnectorInfo {
    VmInfo(String fileName, Integer lineNumber, Map<String, Object> parameters) {
        super(fileName, lineNumber, parameters)
    }
}
