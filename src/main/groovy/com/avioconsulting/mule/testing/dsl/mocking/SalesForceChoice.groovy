package com.avioconsulting.mule.testing.dsl.mocking

interface SalesForceChoice {
    def withInputPayload(SalesForceCreateConnectorType type, Closure closure)
}