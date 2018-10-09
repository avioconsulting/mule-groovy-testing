package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.XmlDateHelp
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import com.avioconsulting.mule.testing.soapxmlroot.SOAPTestRequest
import com.avioconsulting.mule.testing.soapxmlroot.SOAPTestResponse
import org.junit.Test

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class SoapTest extends BaseJunitTest implements OverrideConfigList,
        XmlDateHelp {
    List<String> getConfigResources() {
        ['soap_test.xml']
    }

    @Test
    void input_output() {
        // arrange
        def input = new SOAPTestRequest().with {
            title = 'hello there'
            approvalDate = getXmlDate(2018, 8, 07)
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
            approvalDate = getXmlDate(2018, 8, 07)
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
    void runs_via_apikit() {
        // arrange
        def input = new SOAPTestRequest().with {
            title = 'hello there'
            approvalDate = getXmlDate(2018, 8, 07)
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
}
