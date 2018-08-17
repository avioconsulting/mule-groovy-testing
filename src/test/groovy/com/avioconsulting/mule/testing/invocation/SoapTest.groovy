package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.XmlDateHelp
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import com.avioconsulting.mule.testing.soapxmlroot.SOAPTestRequest
import com.avioconsulting.mule.testing.soapxmlroot.SOAPTestResponse
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class SoapTest extends BaseJunitTest implements OverrideConfigList,
        XmlDateHelp {
    List<String> getConfigResourcesList() {
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
        def result = runFlow('/some/soap/flow') {
            soap {
                inputJaxbPayload(input)
            }
        } as SOAPTestResponse

        // assert
        assertThat result.details,
                   is(equalTo('theTitle hello there'))
    }

    @Test
    void input_output_messagepayloadasstring() {
        // arrange
        def input = new SOAPTestRequest().with {
            title = 'hello there'
            approvalDate = getXmlDate(2018, 8, 07)
            it
        }

        // act
        def result = runFlow('/some/soap/flow/messagepayloadasstring') {
            soap {
                inputJaxbPayload(input)
            }
        } as SOAPTestResponse

        // assert
        assertThat result.details,
                   is(equalTo('theTitle hello there'))
    }
}
