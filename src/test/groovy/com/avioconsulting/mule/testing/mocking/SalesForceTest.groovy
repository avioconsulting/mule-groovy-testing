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
                successful()
            }
        }

        // act
        def results = runFlow('sfdcUpsert') {
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
            runFlow('sfdcUpsert') {
                java {
                    inputPayload([howdy: 123])
                }
            }
        }

        // assert
        assertThat result.message,
                   is(containsString(
                           'Must return a SalesForce result from your mock. Options include [failed, successful]. See class com.avioconsulting.mule.testing.dsl.mocking.sfdc.UpsertResponseUtil class for options'))
    }

    @Test
    void upsert_not_created() {
        // arrange
        mockSalesForceCall('Salesforce upsert') {
            upsert { Map data ->
                successful(false)
            }
        }

        // act
        def results = runFlow('sfdcUpsert') {
            java {
                inputPayload([howdy: 123])
            }
        } as List<EnrichedUpsertResult>

        // assert
        assertThat results.size(),
                   is(equalTo(1))
        def result = results[0]
        assertThat result.success,
                   is(equalTo(true))
        assertThat result.created,
                   is(equalTo(false))
    }

    @Test
    void upsert_failure() {
        // arrange
        mockSalesForceCall('Salesforce upsert') {
            upsert { Map data ->
                failed()
            }
        }

        // act
        def results = runFlow('sfdcUpsert') {
            java {
                inputPayload([howdy: 123])
            }
        } as List<EnrichedUpsertResult>

        // assert
        assertThat results.size(),
                   is(equalTo(1))
        def result = results[0]
        assertThat result.success,
                   is(equalTo(false))
        assertThat result.created,
                   is(equalTo(false))
    }

    @Test
    void query() {
        // arrange
        String actualQuery = null
        mockSalesForceCall('Salesforce query') {
            query { String inputQuery ->
                actualQuery = inputQuery
                [
                        [value: 123]
                ]
            }
        }

        // act
        def results = runFlow('sfdcQuery') {
            java {
                inputPayload([howdy: 456])
            }
        } as List<Map>

        // assert
        assertThat results,
                   is(equalTo([
                           [value: 123]
                   ]))
        assertThat actualQuery,
                   is(equalTo("SELECT Foo_c FROM Product WHERE Foo_c = '456'"))
    }

    @Test
    void query_incorrectResponseType_String() {
        // arrange
        mockSalesForceCall('Salesforce query') {
            query { String inputQuery ->
                'foobar'
            }
        }

        // act
        def result = shouldFail {
            runFlow('sfdcQuery') {
                java {
                    inputPayload([howdy: 456])
                }
            }
        }

        // assert
        assertThat result.message,
                   is(containsString(
                           'Must return a List<Map> result from your mock instead of foobar which is of type class java.lang.String!'))
    }

    @Test
    void query_incorrectResponseType_wrongListType() {
        // arrange
        mockSalesForceCall('Salesforce query') {
            query { String inputQuery ->
                ['foobar']
            }
        }

        // act
        def result = shouldFail {
            runFlow('sfdcQuery') {
                java {
                    inputPayload([howdy: 456])
                }
            }
        }

        // assert
        assertThat result.message,
                   is(containsString(
                           'Must return a List<Map> result from your mock instead of List<class java.lang.String>!'))
    }
}
