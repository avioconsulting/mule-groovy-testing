package com.avioconsulting.mule.testing.dsl.mocking

enum SalesForceCreateConnectorType {
    Upsert('upsert')

    private String connectorElementName

    SalesForceCreateConnectorType(String connectorElementName) {
        this.connectorElementName = connectorElementName
    }
}