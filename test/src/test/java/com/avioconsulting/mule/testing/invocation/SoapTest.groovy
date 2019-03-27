package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.ConfigTrait
import com.avioconsulting.mule.testing.XmlDateHelp
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import com.avioconsulting.mule.testing.soapxmlroot.SOAPTestRequest
import com.avioconsulting.mule.testing.soapxmlroot.SOAPTestResponse
import org.junit.Test

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class SoapTest extends
        BaseJunitTest implements
        ConfigTrait,
        XmlDateHelp {
    List<String> getConfigResources() {
        ['soap_test.xml']
    }

    @Test
    void input_output_not_wrapped_in_body() {
        // arrange
        def input = new SOAPTestRequest().with {
            title = 'hello there do not wrap in body'
            approvalDate = getXmlDate(2018,
                                      8,
                                      07)
            it
        }

        // act
        def result = runFlow('\\some\\soap\\flow') {
            soap {
                inputJaxbPayload(input)
            }
        } as SOAPTestResponse

        // assert
        assertThat result.details,
                   is(equalTo('theTitle hello there do not wrap in body'))
    }

    @Test
    void input_output_wrapped_in_body() {
        // arrange
        def input = new SOAPTestRequest().with {
            title = 'hello there'
            approvalDate = getXmlDate(2018,
                                      8,
                                      07)
            it
        }

        // act
        def result = runFlow('\\some\\soap\\flow') {
            soap {
                inputJaxbPayload(input)
            }
        } as SOAPTestResponse

        // assert
        assertThat result.details,
                   is(equalTo('theTitle hello there'))
    }

    @Test
    void no_payload_change() {
        // arrange
        def input = new SOAPTestRequest().with {
            title = 'hello there'
            approvalDate = getXmlDate(2018,
                                      8,
                                      07)
            it
        }

        // act
        def response = runFlow('soaptestFlowNoPayloadChange') {
            soap {
                inputJaxbPayload(input)
            }
        } as SOAPTestRequest

        // assert
        assertThat response.title,
                   is(equalTo('hello there'))
    }

    @Test
    void read_stream_twice() {
        // arrange
        def input = new SOAPTestRequest().with {
            title = 'hello there'
            approvalDate = getXmlDate(2018,
                                      8,
                                      07)
            it
        }

        // act
        def result = runFlow('readStreamTwice') {
            soap {
                inputJaxbPayload(input)
            }
        } as SOAPTestResponse

        // assert
        assertThat result.details,
                   is(equalTo('theTitle hello there'))
    }

    @Test
    void runs_via_apikit() {
        // arrange
        def input = new SOAPTestRequest().with {
            title = 'hello there'
            approvalDate = getXmlDate(2018,
                                      8,
                                      07)
            it
        }

        // act
        def result = runSoapApikitFlow('operation1') {
            inputJaxbPayload(input)
        }

        // assert
        def payload = result.messageAsString
        assertThat payload,
                   is(startsWith('<soap:Envelope'))
        assertThat payload,
                   is(containsString('<ns0:details>theTitle hello there'))
    }

    @Test
    void runs_via_apikit_jaxb_response_not_wrapped_in_body() {
        // arrange
        def input = new SOAPTestRequest().with {
            title = 'hello there do not wrap in body'
            approvalDate = getXmlDate(2018,
                                      8,
                                      07)
            it
        }

        // act
        def result = runSoapApikitFlowJaxbResultBody('operation1') {
            inputJaxbPayload(input)
        } as SOAPTestResponse

        // assert
        assertThat result.details,
                   is(equalTo('theTitle hello there do not wrap in body'))
    }

    @Test
    void runs_via_apikit_jaxb_response_wrapped_in_body() {
        // arrange
        def input = new SOAPTestRequest().with {
            title = 'hello there'
            approvalDate = getXmlDate(2018,
                                      8,
                                      07)
            it
        }

        // act
        def result = runSoapApikitFlowJaxbResultBody('operation1') {
            inputJaxbPayload(input)
        } as SOAPTestResponse

        // assert
        assertThat result.details,
                   is(equalTo('theTitle hello there'))
    }

    @Test
    void custom_host() {
        // arrange
        def input = new SOAPTestRequest().with {
            title = 'hello there'
            approvalDate = getXmlDate(2018,
                                      8,
                                      07)
            it
        }
        String loggedPayload = null
        mockGeneric('Log stuff') {
            raw {
                whenCalledWith { typedValue ->
                    loggedPayload = typedValue.value
                }
            }
        }

        // act
        runSoapApikitFlow('operation1',
                          'api-main',
                          'otherhost:123') {
            inputJaxbPayload(input)
        }

        // assert
        assertThat loggedPayload,
                   is(equalTo('otherhost:123'))
    }
}
