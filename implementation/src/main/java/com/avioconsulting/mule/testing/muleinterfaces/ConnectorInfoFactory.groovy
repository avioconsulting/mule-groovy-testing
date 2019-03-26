package com.avioconsulting.mule.testing.muleinterfaces


import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors.HttpRequesterInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors.SoapConsumerInfo

class ConnectorInfoFactory {
    ConnectorInfo getConnectorInfo(Object componentLocation,
                                   String name,
                                   Map params) {
        String fileName = componentLocation.fileName.get()
        String container = componentLocation.getRootContainerName()
        Integer lineInFile = componentLocation.lineInFile.get() as Integer
        // the last part of our identifier should be the connector being mocked
        // which should be a good tip as to what type of connector it is
        def componentIdentifierOptional = componentLocation.parts.last().partIdentifier
        assert componentIdentifierOptional.present: "Expected to find component identifier for ${name}!"
        def identifier = componentIdentifierOptional.get().identifier
        def info = {
            if (identifier.namespace == 'http' && identifier.name == 'request') {
                return new HttpRequesterInfo(fileName,
                                             lineInFile,
                                             container,
                                             params)
            } else if (identifier.namespace == 'wsc' && identifier.name == 'consume') {
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
