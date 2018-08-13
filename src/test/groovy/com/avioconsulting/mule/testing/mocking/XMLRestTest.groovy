package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.BaseJunitTest
import com.avioconsulting.mule.testing.OverrideConfigList
import groovy.json.JsonOutput
import org.junit.Test
import org.mule.api.MuleMessage
import org.mule.api.transport.PropertyScope

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class XMLRestTest extends BaseJunitTest implements OverrideConfigList {
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
                inputPayload([foo: 123])
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
    void mockViaMap_withMuleMsg() {
        // arrange
        MuleMessage sentMessage = null
        mockRestHttpCall('SomeSystem Call') {
            xml {
                whenCalledWithMapAsXml { Map input,
                                         MuleMessage message ->
                    sentMessage = message
                    [
                            rootElementResponse: [
                                    reply: 22
                            ]
                    ]
                }
            }
        }

        // act
        runFlow('xmlTest') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assertThat sentMessage.getProperty('content-type', PropertyScope.INBOUND),
                   is(equalTo('application/json; charset=utf-8'))
    }

    @Test
    void nestedTest() {
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
        def result = runFlow('nestedTest') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assert mockedData
        assertThat JsonOutput.toJson(mockedData),
                   is(equalTo(JsonOutput.toJson([
                           rootElement: [
                                   anotherElement: [
                                           key: '123'
                                   ]
                           ]
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
                inputPayload([foo: 123])
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
    void mockGroovyXmlParser() {
        Node mockedData = null
        mockRestHttpCall('SomeSystem Call') {
            xml {
                whenCalledWithGroovyXmlParser { Node input ->
                    mockedData = input
                    def node = new Node(null, 'rootElementResponse')
                    node.appendNode('reply', 22)
                    node
                }
            }
        }

        // act
        def result = runFlow('xmlTest') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assert mockedData
        assertThat mockedData.name() as String,
                   is(equalTo('rootElement'))
        def key = mockedData.key[0] as Node
        assert key
        assertThat key.text(),
                   is(equalTo('123'))
        assertThat JsonOutput.toJson(result),
                   is(equalTo(JsonOutput.toJson(
                           [reply_key: 23]
                   )))
    }

    @Test
    void mockGroovyXmlParser_withMuleMsg() {
        // arrange
        MuleMessage sentMessage = null
        mockRestHttpCall('SomeSystem Call') {
            xml {
                whenCalledWithGroovyXmlParser { Node input,
                                                MuleMessage message ->
                    sentMessage = message
                    def node = new Node(null, 'rootElementResponse')
                    node.appendNode('reply', 22)
                    node
                }
            }
        }

        // act
        runFlow('xmlTest') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assertThat sentMessage.getProperty('content-type', PropertyScope.INBOUND),
                   is(equalTo('application/json; charset=utf-8'))
    }

    @Test
    void mockGroovyXmlParser_FileResponse() {
        // arrange
        Node mockedData = null
        mockRestHttpCall('SomeSystem Call') {
            xml {
                whenCalledWithGroovyXmlParser { Node input ->
                    mockedData = input
                    new File('src/test/resources/xml/simple_xml_response.xml')
                }
            }
        }

        // act
        def result = runFlow('xmlTest') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assert mockedData
        assertThat mockedData.name() as String,
                   is(equalTo('rootElement'))
        def key = mockedData.key[0] as Node
        assert key
        assertThat key.text(),
                   is(equalTo('123'))
        assertThat JsonOutput.toJson(result),
                   is(equalTo(JsonOutput.toJson(
                           [reply_key: 23]
                   )))
    }

    @Test
    void contentTypeNotSet() {
        // arrange
        mockRestHttpCall('SomeSystem Call') {
            xml {
                whenCalledWithMapAsXml { Map input ->
                }
            }
        }

        // act
        def result = shouldFail {
            runFlow('xmlTestWithoutContentType') {
                json {
                    inputPayload([foo: 123])
                }
            }
        }

        // assert
        assertThat result.message,
                   is(containsString(
                           'Expected Content-Type to be of type [application/xml] but it actually was null. Check your mock endpoints.'))
    }
}
