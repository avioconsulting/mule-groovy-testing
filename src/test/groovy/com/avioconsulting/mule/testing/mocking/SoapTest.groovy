package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.BaseTest
import com.avioconsulting.schemas.soaptest.v1.ObjectFactory
import com.avioconsulting.schemas.soaptest.v1.SOAPTestRequestType
import com.avioconsulting.schemas.soaptest.v1.SOAPTestResponseType
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
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
                // TODO: Avoid having to do the object factory stuff??
                new ObjectFactory().createSOAPTestResponse(response)
            }
        }

        // act
        def result = runFlow('soaptestFlow') {
            json {
                map([foo: 123])
            }
        } as Map

        // assert
        assert mockedRequest
        assertThat mockedRequest.title,
                   is(equalTo("theTitle 123"))
        assertThat result,
                   is(equalTo([result: 'yes!']))
    }
}
