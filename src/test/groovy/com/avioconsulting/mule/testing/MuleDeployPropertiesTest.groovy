package com.avioconsulting.mule.testing

import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class MuleDeployPropertiesTest extends BaseTest {
    @Override
    Map<String, String> getConfigResourceSubstitutes() {
        [
                'simple_json_test.xml': 'java_test.xml',
                'foo.xml'             : null,
                'bar.xml'             : null
        ]
    }

    @Test
    void correct_resources() {
        // arrange

        // act
        def result = this.configResources.split(',').toList()

        // assert
        assertThat result,
                   is(equalTo([
                           'java_test.xml',
                           'http_test.xml'
                   ]))
    }
}
