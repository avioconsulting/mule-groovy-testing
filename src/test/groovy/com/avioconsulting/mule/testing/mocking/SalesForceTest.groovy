package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.BaseTest
import org.junit.Test
import org.mule.modules.salesforce.bulk.EnrichedUpsertResult

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.*
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
            upsert { Map data ->
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
        mockSalesForceCall('Salesforce upsert') {
            upsert { Map data ->
                return null
            }
        }

        // act
        def result = shouldFail {
            runFlow('sfdcCreate') {
                java {
                    inputPayload([howdy: 123])
                }
            }
        }

        // assert
        assertThat result.message,
                   is(containsString(
                           'Must return a SalesForce result from your mock. Options include [successfulUpsertResult, successfulUpsertResults]. See class com.avioconsulting.mule.testing.dsl.mocking.sfdc.UpsertResponseUtil class for options'))
    }

    @Test
    void upsert_not_created() {
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
