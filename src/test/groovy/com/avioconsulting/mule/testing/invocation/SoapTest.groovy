package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.XmlDateHelp
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import com.avioconsulting.mule.testing.soapxmlroot.SOAPTestRequest
import com.avioconsulting.mule.testing.soapxmlroot.SOAPTestResponse
import org.junit.Test

import javax.wsdl.BindingOperation
import javax.wsdl.extensions.soap.SOAPOperation
import javax.wsdl.factory.WSDLFactory
import javax.xml.namespace.QName

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

    @Test
    void runs_via_apikit() {
        // arrange
        def input = new SOAPTestRequest().with {
            title = 'hello there'
            approvalDate = getXmlDate(2018, 8, 07)
            it
        }
        // TODO: More clear asserts
        // TODO: WSDL Path from flow's apikitrouter config
        // TODO: Remove types from closures and use reflection for factory
        def fact = WSDLFactory.newInstance()
        def reader = fact.newWSDLReader()
        def wsdlPath = 'soap/test.wsdl'
        def res = SoapTest.getResource("/${wsdlPath}")
        assert res: "Unable to find WSDL at path ${wsdlPath}"
        def defin = reader.readWSDL(res.toString())
        def bindings = defin.bindings.values() as List<javax.wsdl.Binding>
        def operations = bindings.collect { javax.wsdl.Binding binding ->
            binding.bindingOperations
        }.flatten() as List<BindingOperation>
        def op = operations.find { operation ->
            operation.name == 'operation1'
        }
        assert op
        def soapOperation = op.extensibilityElements.find() { el ->
            el.elementType == new QName('http://schemas.xmlsoap.org/wsdl/soap/',
                                        'operation')
        } as SOAPOperation
        assert soapOperation: "Expected a SOAP Action type attribute on the operation! e.g. <soap:operation\n" +
                "                    soapAction=\"http://www.avioconsulting.com/services/SOAPTest/v1/SOAPTest\"/>"
        def soapAction = soapOperation.soapActionURI

        // act
        def result = runSoapApikitFlow('operation1') {
            inputJaxbPayload(input)
        } as SOAPTestResponse

        // assert
        assertThat result.details,
                   is(equalTo('theTitle hello there'))
    }
}
