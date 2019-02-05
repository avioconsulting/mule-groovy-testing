package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.ConfigTrait
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class JavaTest extends
        BaseJunitTest implements
        ConfigTrait {
    List<String> getConfigResources() {
        ['java_test.xml']
    }

    @Test
    void javaObject() {
        // arrange
        // you don't have to use this with java inputs but if your app serializes it (batch) or needs to
        // know what the class is, it will expect it to be w/ the app's classloader
        def input = instantiateJavaClassWithAppClassLoader(SimpleJavaClass).with {
            howdy = '123'
            it
        }

        // act
        def result = runFlow('javaFlow') {
            java {
                inputPayload(input)
            }
        }

        // assert
        assertThat result,
                   is(equalTo([
                           key                    : '123',
                           value                  : SimpleJavaClass.name,
                           value_from_jacva_module: '123'
                   ]))
    }
}
