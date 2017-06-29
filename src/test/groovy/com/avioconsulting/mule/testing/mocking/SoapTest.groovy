package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.BaseTest
import com.avioconsulting.mule.testing.soapxmlroot.SOAPTestRequest
import com.avioconsulting.mule.testing.soapxmlroot.SOAPTestResponse
import com.avioconsulting.schemas.soaptest.v1.ObjectFactory
import com.avioconsulting.schemas.soaptest.v1.SOAPTestRequestType
import com.avioconsulting.schemas.soaptest.v1.SOAPTestResponseType
import org.junit.Test
import org.mule.api.MessagingException

import java.util.concurrent.TimeoutException

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class SoapTest extends BaseTest {
    List<String> getConfigResourcesList() {
        ['soap_test.xml']
    }

    @Test
    void mocksProperly() {
        // arrange
        SOAPTestRequestType mockedRequest = null

        mockSoapCall('A SOAP Call') {
            whenCalledWithJaxb(SOAPTestRequestType) { SOAPTestRequestType request ->
                mockedRequest = request
                def response = new SOAPTestResponseType()
                response.details = 'yes!'
                new ObjectFactory().createSOAPTestResponse(response)
            }
        }

        // act
        def result = runFlow('soaptestFlow') {
            json {
                inputPayload([foo: 123])
            }
        } as Map

        // assert
        assert mockedRequest
        assertThat mockedRequest.title,
                   is(equalTo("theTitle 123"))
        assertThat mockedRequest.approvalDate.toString(),
                   is(equalTo('2017-04-01'))
        assertThat result,
                   is(equalTo([result: 'yes!']))
    }

    @Test
    void forgetToReturnAJaxbElement() {
        // arrange
        mockSoapCall('A SOAP Call') {
            whenCalledWithJaxb(SOAPTestRequestType) { SOAPTestRequestType request ->
                def response = new SOAPTestResponseType()
                response.details = 'yes!'
                response
            }
        }

        // act
        def result = shouldFail {
            runFlow('soaptestFlow') {
                json {
                    inputPayload([foo: 123])
                }
            }
        }

        // assert
        assertThat result.message,
                   is(containsString(
                           'Unmarshal problem. if com.avioconsulting.schemas.soaptest.v1.SOAPTestResponseType is not an XML Root element, you need to use ObjectFactory to wrap it in a JAXBElement object!'))
    }

    @Test
    void rootElements() {
        // arrange
        SOAPTestRequest mockedRequest = null

        mockSoapCall('A SOAP Call') {
            whenCalledWithJaxb(SOAPTestRequest) { SOAPTestRequest request ->
                mockedRequest = request
                def response = new SOAPTestResponse()
                response.details = 'yes!'
                response
            }
        }

        // act
        def result = runFlow('soaptestFlow') {
            json {
                inputPayload([foo: 123])
            }
        } as Map

        // assert
        assert mockedRequest
        assertThat mockedRequest.title,
                   is(equalTo("theTitle 123"))
        assertThat mockedRequest.approvalDate.toString(),
                   is(equalTo('2017-04-01'))
        assertThat result,
                   is(equalTo([result: 'yes!']))
    }

    @Test
    void returnAFile() {
        // assert
        SOAPTestRequestType mockedRequest = null

        mockSoapCall('A SOAP Call') {
            whenCalledWithJaxb(SOAPTestRequestType) { SOAPTestRequestType request ->
                mockedRequest = request
                new File('src/test/resources/soap/as_file_response.xml')
            }
        }

        // act
        def result = runFlow('soaptestFlow') {
            json {
                inputPayload([foo: 123])
            }
        } as Map

        // assert
        assert mockedRequest
        assertThat mockedRequest.title,
                   is(equalTo("theTitle 123"))
        assertThat mockedRequest.approvalDate.toString(),
                   is(equalTo('2017-04-01'))
        assertThat result,
                   is(equalTo([result: 'yes!']))
    }

    @Test
    void httpConnectionError() {
        // arrange
        mockSoapCall('A SOAP Call') {
            httpConnectError()
        }

        // act
        def result = shouldFail {
            runFlow('soaptestFlow') {
                json {
                    inputPayload([foo: 123])
                }
            }
        }

        // assert
        assertThat result,
                   is(instanceOf(MessagingException))
        assertThat result.cause,
                   is(instanceOf(ConnectException))
    }

    @Test
    void http_TimeoutError() {
        // arrange
        mockSoapCall('A SOAP Call') {
            httpTimeoutError()
        }

        // act
        def result = shouldFail {
            runFlow('soaptestFlow') {
                json {
                    inputPayload([foo: 123])
                }
            }
        }

        // assert
        assertThat result,
                   is(instanceOf(MessagingException))
        assertThat result.cause,
                   is(instanceOf(TimeoutException))
    }
}