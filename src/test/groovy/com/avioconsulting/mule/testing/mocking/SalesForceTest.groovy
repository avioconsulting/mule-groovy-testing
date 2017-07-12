package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.BaseTest
import org.junit.Test
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer
import org.mule.munit.common.mocking.Attribute

class SalesForceTest extends BaseTest {
    List<String> getConfigResourcesList() {
        ['sfdc_test.xml']
    }

    @Test
    void createSingle() {
        // arrange
        def input = null
        def mock = whenMessageProcessor('create-single')
                .ofNamespace('sfdc')
                .withAttributes(Attribute.attribute('name')
                                        .ofNamespace('doc')
                                        .withValue('Salesforce create'))
        mock.thenApply(new MuleMessageTransformer() {
            @Override
            MuleMessage transform(MuleMessage muleMessage) {
                input = muleMessage.payload
                // TODO: Build SDFC result classes
                return muleMessage
            }
        })

        // act
        runFlow('sfdcCreate') {
            json {
                inputOnly([howdy: 123])
            }
        }

        // assert
        assert input
        fail 'write this'
    }

    @Test
    void query() {
        // arrange

        // act

        // assert
        fail 'write this'
    }
}
