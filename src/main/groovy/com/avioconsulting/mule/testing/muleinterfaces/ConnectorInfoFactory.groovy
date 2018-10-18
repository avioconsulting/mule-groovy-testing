package com.avioconsulting.mule.testing.muleinterfaces


import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors.HttpRequesterInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors.SoapConsumerInfo

class ConnectorInfoFactory {
    ConnectorInfo getConnectorInfo(String fileName,
                                   String name,
                                   String container,
                                   Integer lineInFile,
                                   Map params) {
        def info = {
            if (params['requestBuilder']?.class?.name?.endsWith('HttpRequesterRequestBuilder')) {
                return new HttpRequesterInfo(fileName,
                                             lineInFile,
                                             container,
                                             params)
            } else if (params['message']?.class?.name?.endsWith('SoapMessageBuilder')) {
                return new SoapConsumerInfo(fileName,
                                            lineInFile,
                                            container,
                                            params)
            }
            new ConnectorInfo(fileName,
                              lineInFile,
                              container,
                              params)
        }()
        info.with {
            it.name = name
            it
        }
    }
}