package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.BaseTest
import com.avioconsulting.mule.testing.dsl.mocking.SalesForceCreateConnectorType
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class SalesForceTest extends BaseTest {
    List<String> getConfigResourcesList() {
        ['sfdc_test.xml']
    }

    @Test
    void upsert() {
        // arrange
        Map input = null
        mockSalesForceCall('Salesforce upsert') {
            withInputPayload(SalesForceCreateConnectorType.Upsert) { Map data ->
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
        assertThat input,
                   is(equalTo([
                           Name     : 'Brady product',
                           Howdy2__c: 'payload.howdy'
                   ]))

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
