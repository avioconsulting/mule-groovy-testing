package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.BaseTest
import com.avioconsulting.mule.testing.dsl.mocking.SalesForceCreateConnectorType
import org.junit.Test

class SalesForceTest extends BaseTest {
    List<String> getConfigResourcesList() {
        ['sfdc_test.xml']
    }

    @Test
    void createSingle() {
        // arrange
        Map input = null
        mockSalesForceCall('Salesforce create') {
            withInputPayload(SalesForceCreateConnectorType.CreateSingle) { Map data ->
                input = data
            }
        }

        // act
        runFlow('sfdcCreate') {
            json {
                inputOnly([howdy: 123])
            }
        }

        // assert
        assert input
        fail 'write this, including salesforceresponse class'
    }

    @Test
    void query() {
        // arrange

        // act

        // assert
        fail 'write this'
    }
}
