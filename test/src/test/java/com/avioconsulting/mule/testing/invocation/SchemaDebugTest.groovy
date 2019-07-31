package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.ConfigTrait
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class SchemaDebugTest extends BaseJunitTest implements
        ConfigTrait {
    List<String> getConfigResources() {
        ['simple_json_test.xml']
    }

    @Override
    boolean isGenerateXmlSchemas() {
        true
    }

    @Test
    void test() {
        // arrange + act happen during engine start

        // assert
        def httpSchema = new File('.mule/schemas_from_testing_framework/mule-http.xsd')
        assertThat httpSchema.exists(),
                   is(equalTo(true))
    }
}
