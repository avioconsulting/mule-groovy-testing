package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.MessageWrapper
import groovy.json.JsonOutput
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class XMLRestTest extends
        BaseJunitTest implements
        OverrideConfigList {
    List<String> getConfigResources() {
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
    void from_flowvar_map() {
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
        def result = runFlow('xmlTestFromFlowVar') {
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
        MessageWrapper sentMessage = null
        mockRestHttpCall('SomeSystem Call') {
            xml {
                whenCalledWithMapAsXml { Map input,
                                         EventWrapper event ->
                    sentMessage = event.message
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
        assertThat sentMessage.mimeType,
                   is(equalTo('application/xml; charset=UTF-8'))
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
        // arrange
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
    void from_flowvar_groovyxml() {
        // arrange
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
        def result = runFlow('xmlTestFromFlowVar') {
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
        MessageWrapper sentMessage = null
        mockRestHttpCall('SomeSystem Call') {
            xml {
                whenCalledWithGroovyXmlParser { Node input,
                                                EventWrapper muleEvent ->
                    sentMessage = muleEvent.message
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
        assertThat sentMessage.mimeType,
                   is(equalTo('application/xml; charset=UTF-8'))
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
}
