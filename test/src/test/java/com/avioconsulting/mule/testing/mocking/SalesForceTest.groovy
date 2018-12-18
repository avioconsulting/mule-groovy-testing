package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.ConfigTrait
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import org.junit.Ignore
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

@Ignore('DQL is not available in Studio 7 yet')
class SalesForceTest extends
        BaseJunitTest implements
        ConfigTrait {
    List<String> getConfigResources() {
        ['sfdc_test.xml']
    }

    @Test
    void upsert_success() {
        // arrange
        List<Map> input = null
        mockSalesForceCall('Salesforce upsert') {
            upsert { List<Map> data ->
                input = data
                successful()
            }
        }

        assert false: 'SFDC module/EnrichedUpsertResult'

        // act
        def results = runFlow('sfdcUpsert') {
            java {
                inputPayload([howdy: 123])
            }
        } as List<Object>

        // assert
        assert input
        assertThat input,
                   is(equalTo([
                           [
                                   Name     : 'Brady product',
                                   Howdy2__c: 123
                           ]
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
            upsert { List<Map> data ->
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
            upsert { List<Map> data ->
                successful(false)
            }
        }
        assert false: 'SFDC module/EnrichedUpsertResult'

        // act
        def results = runFlow('sfdcUpsert') {
            java {
                inputPayload([howdy: 123])
            }
        } as List<Object>

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
            upsert { List<Map> data ->
                failed()
            }
        }
        assert false: 'SFDC module/EnrichedUpsertResult'

        // act
        def results = runFlow('sfdcUpsert') {
            java {
                inputPayload([howdy: 123])
            }
        } as List<Object>

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
                           'Must return a List<java.util.Map> result from your mock instead of foobar which is of type class java.lang.String!'))
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
                           'Must return a List<java.util.Map> result from your mock instead of List<java.lang.String>!'))
    }

    @Test
    void upsert_more_than_200_records() {
        // arrange
        def tooMany = (1..201).collect {
            [howdy: 123]
        }
        mockSalesForceCall('Salesforce upsert') {
            upsert { List<Map> data ->
                successful(false)
            }
        }

        // act
        def results = shouldFail {
            runFlow('sfdcUpsertFromInput') {
                java {
                    inputPayload(tooMany)
                }
            }
        }

        // assert
        assertThat results.message,
                   is(containsString('You can only upsert a maximum of 200 records but you just tried to upsert 201 records. Consider using a batch processor'))
    }
}
