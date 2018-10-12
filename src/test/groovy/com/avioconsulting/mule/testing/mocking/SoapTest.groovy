package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.soapxmlroot.SOAPTestRequest
import com.avioconsulting.mule.testing.soapxmlroot.SOAPTestResponse
import com.avioconsulting.schemas.soaptest.v1.ObjectFactory
import com.avioconsulting.schemas.soaptest.v1.SOAPTestRequestType
import com.avioconsulting.schemas.soaptest.v1.SOAPTestResponseType
import groovy.xml.DOMBuilder
import org.junit.Test

import javax.xml.namespace.QName

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class SoapTest extends
        BaseJunitTest implements
        OverrideConfigList {
    List<String> getConfigResources() {
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
    void with_mule_message() {
        // arrange
        EventWrapper sentMessage = null
        mockSoapCall('A SOAP Call') {
            whenCalledWithJaxb(SOAPTestRequestType) { SOAPTestRequestType request,
                                                      EventWrapper msg ->
                sentMessage = msg
                def response = new SOAPTestResponseType()
                response.details = 'yes!'
                new ObjectFactory().createSOAPTestResponse(response)
            }
        }

        // act
        runFlow('soaptestFlow') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assertThat sentMessage.message.mimeType,
                   is(equalTo('application/xml; charset=UTF-8'))
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
    void httpConnectionError_no_custom_transport() {
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
        assertThat result.class.name,
                   is(equalTo('org.mule.runtime.core.internal.exception.MessagingException'))
        assertThat result.cause.class.name,
                   is(equalTo('org.mule.runtime.soap.api.exception.DispatchingException'))
        assertThat result.cause.cause,
                   is(nullValue())
        assertThat result.info['Error type'],
                   is(equalTo(' WSC:CANNOT_DISPATCH'))
        assertThat result.message,
                   is(equalTo('An error occurred while sending the SOAP request.'))
    }

    @Test
    void httpConnectionError_custom_transport() {
        // arrange
        mockSoapCall('A SOAP Call') {
            httpConnectError()
        }

        // act
        def result = shouldFail {
            runFlow('soaptestFlow_Custom_Transport') {
                json {
                    inputPayload([foo: 123])
                }
            }
        }

        // assert
        assertThat result.getClass().name,
                   is(equalTo('org.mule.runtime.core.internal.exception.MessagingException'))
        assertThat result.cause.getClass().name,
                   is(equalTo('org.mule.extension.http.api.error.HttpRequestFailedException'))
        assertThat result.cause.cause.getClass().name,
                   is(equalTo('java.net.ConnectException'))
        assertThat result.info['Error type'],
                   is(equalTo('HTTP:CONNECTIVITY'))
        assertThat result.message,
                   is(equalTo("HTTP POST on resource 'http://localhost:8081' failed: Connection refused."))
    }

    @Test
    void http_TimeoutError_no_custom_transport() {
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
        assertThat result.class.name,
                   is(equalTo('org.mule.runtime.core.internal.exception.MessagingException'))
        assertThat result.cause.class.name,
                   is(equalTo('org.mule.runtime.soap.api.exception.DispatchingException'))
        assertThat result.cause.cause.class.name,
                   is(equalTo('java.util.concurrent.TimeoutException'))
        assertThat result.info['Error type'],
                   is(equalTo('WSC:CANNOT_DISPATCH'))
        assertThat result.message,
                   is(equalTo('The SOAP request timed out.'))
    }

    @Test
    void http_TimeoutError_custom_transport() {
        // arrange
        mockSoapCall('A SOAP Call') {
            httpTimeoutError()
        }

        // act
        def result = shouldFail {
            runFlow('soaptestFlow_Custom_Transport') {
                json {
                    inputPayload([foo: 123])
                }
            }
        }

        // assert
        assertThat result.getClass().name,
                   is(equalTo('org.mule.runtime.core.internal.exception.MessagingException'))
        assertThat result.cause.getClass().name,
                   is(equalTo('org.mule.extension.http.api.error.HttpRequestFailedException'))
        assertThat result.cause.cause.getClass().name,
                   is(equalTo('java.util.concurrent.TimeoutException'))
        assertThat result.info['Error type'],
                   is(equalTo('HTTP:TIMEOUT'))
        assertThat result.message,
                   is(equalTo("HTTP POST on resource 'http://localhost:8081' failed: Some timeout error."))
    }

    @Test
    void soap_fault() {
        // arrange
        mockSoapCall('Get Weather') {
            whenCalledWithMapAsXml { request ->
                soapFault('Error with one or more zip codes: ',
                          new QName('',
                                    'SERVER'),
                          null) { DOMBuilder detailBuilder ->
                    detailBuilder.detail {
                        foobar()
                    }
                }
            }
        }

        // act
        def exception = shouldFail {
            runFlow('weatherSoapFaultFlow') {
                json {
                    inputPayload([foo: 123])
                }
            }
        }

        // assert
        assertThat exception,
                   is(instanceOf(SoapFaultException))
        // for intellij
        assert false: 'soapfaultexception??'
        //assert exception instanceof SoapFaultException
        assertThat exception.message,
                   is(equalTo('Error with one or more zip codes: .'))
        assertThat exception.faultCode,
                   is(equalTo(new QName('', 'SERVER')))
        assertThat exception.subCode,
                   is(nullValue())
        def detail = exception.detail
        assert detail
        assertThat detail.serialize().trim(),
                   is(equalTo('<detail>\n<foobar/>\n</detail>'))
    }
}
