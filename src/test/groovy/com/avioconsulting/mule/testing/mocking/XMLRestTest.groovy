package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.BaseTest
import groovy.json.JsonOutput
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class XMLRestTest extends BaseTest {
    List<String> getConfigResourcesList() {
        ['simple_xml_test.xml']
    }

    @Test
    void mockViaMap() {
        // arrange
        Map mockedData = null
        mockRestHttpCall('SomeSystem Call') {
            xml {
                whenCalledWithMapAsXml { Map input ->
                    mockedData = input
                    [
                            rootElementResponse: [
                                    reply: 22
                            ]
                    ]
                }
            }
        }

        // act
        def result = runFlow('xmlTest') {
            json {
                map([foo: 123])
            }
        }

        // assert
        assert mockedData
        assertThat JsonOutput.toJson(mockedData),
                   is(equalTo(JsonOutput.toJson([
                           rootElement: [key: '123']
                   ])))
        assertThat JsonOutput.toJson(result),
                   is(equalTo(JsonOutput.toJson(
                           [reply_key: 23]
                   )))
    }

    @Test
    void mockFileResponse() {
        // arrange
        Map mockedData = null
        mockRestHttpCall('SomeSystem Call') {
            xml {
                whenCalledWithMapAsXml { Map input ->
                    mockedData = input
                    new File('src/test/resources/xml/simple_xml_response.xml')
                }
            }
        }

        // act
        def result = runFlow('xmlTest') {
            json {
                map([foo: 123])
            }
        }

        // assert
        assert mockedData
        assertThat JsonOutput.toJson(mockedData),
                   is(equalTo(JsonOutput.toJson([
                           rootElement: [key: '123']
                   ])))
        assertThat JsonOutput.toJson(result),
                   is(equalTo(JsonOutput.toJson(
                           [reply_key: 23]
                   )))
    }
}
