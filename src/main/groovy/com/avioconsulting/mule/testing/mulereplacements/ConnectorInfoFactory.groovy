package com.avioconsulting.mule.testing.mulereplacements


import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors.HttpRequesterInfo

class ConnectorInfoFactory {
    ConnectorInfo getConnectorInfo(String fileName,
                                   Integer lineInFile,
                                   Map params) {
        if (params['requestBuilder']?.class?.name?.endsWith('HttpRequesterRequestBuilder')) {
            return new HttpRequesterInfo(fileName,
                                         lineInFile,
                                         params,
                                         params['method'] as String)
        }
        new ConnectorInfo(fileName,
                          lineInFile,
                          params)
    }
}
