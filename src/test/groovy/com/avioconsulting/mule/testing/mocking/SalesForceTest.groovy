package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.BaseTest
import com.avioconsulting.mule.testing.dsl.mocking.SalesForceCreateConnectorType
import org.junit.Test
import org.mule.modules.salesforce.bulk.EnrichedUpsertResult

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class SalesForceTest extends BaseTest {
    List<String> getConfigResourcesList() {
        ['sfdc_test.xml']
    }

    @Test
    void upsert_success() {
        // arrange
        Map input = null
        mockSalesForceCall('Salesforce upsert') {
            withInputPayload(SalesForceCreateConnectorType.Upsert) { Map data ->
                input = data
                successfulUpsertResult()
            }
        }

        // act
        def results = runFlow('sfdcCreate') {
            java {
                inputPayload([howdy: 123])
            }
        } as List<EnrichedUpsertResult>

        // assert
        assert input
        assertThat input,
                   is(equalTo([
                           Name     : 'Brady product',
                           Howdy2__c: 123
                   ]))
        assertThat results.size(),
                   is(equalTo(1))
        def result = results[0]
        assertThat result.success,
                   is(equalTo(true))
        assertThat result.created,
                   is(equalTo(true))
    }

    @Test
    void upsert_sfdc_result_not_returned() {
        // arrange

        // act

        // assert
        fail 'write this'
    }

    @Test
    void upsert_failure() {
        // arrange

        // act

        // assert
        fail 'write this'
    }

    @Test
    void query() {
        // arrange

        // act

        // assert
        fail 'write this'
    }
}
