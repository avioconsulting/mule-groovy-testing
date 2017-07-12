package com.avioconsulting.mule.testing.dsl.mocking

enum SalesForceCreateConnectorType {
    CreateSingle('create-single')

    private String connectorElementName

    SalesForceCreateConnectorType(String connectorElementName) {
        this.connectorElementName = connectorElementName
    }
}