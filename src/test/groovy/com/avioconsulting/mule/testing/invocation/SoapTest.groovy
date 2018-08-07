package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.BaseTest
import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.schemas.soaptest.v1.SOAPTestRequestType
import com.avioconsulting.schemas.soaptest.v1.SOAPTestResponseType
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class SoapTest extends BaseTest implements OverrideConfigList {
    List<String> getConfigResourcesList() {
        ['soap_test.xml']
    }

    @Test
    void input_output() {
        // arrange
        def input = new SOAPTestRequestType().with {
            title = 'hello there'
            approvalDate = getXmlDate(2018, 8, 07)
            it
        }

        // act
        def result = runFlow('soap-api-main') {
            soap {
                inputPayload(input)
            }
        } as SOAPTestResponseType

        // assert
        assertThat result.details,
                   is(equalTo('howdy'))
    }
}
