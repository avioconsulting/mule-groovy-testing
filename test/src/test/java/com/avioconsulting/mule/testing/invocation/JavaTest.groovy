package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.ConfigTrait
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.StreamUtils
import groovy.json.JsonOutput
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class JavaTest extends
        BaseJunitTest implements
        ConfigTrait,
        StreamUtils {
    List<String> getConfigResources() {
        ['java_test.xml']
    }

    @Test
    void javaObject() {
        // arrange
        def input = new SimpleJavaClass().with {
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
                           key  : '123',
                           value: SimpleJavaClass.name
                   ]))
    }

    @Test
    void subflow() {
        // arrange
        def input = new SimpleJavaClass().with {
            howdy = '123'
            it
        }

        // act
        def result = runFlow('theSubFlow') {
            java {
                inputPayload(input)
            }
        }

        // assert
        assertThat result,
                   is(equalTo([
                           key  : '123',
                           value: SimpleJavaClass.name
                   ]))
    }

    @Test
    void defaultMimeType() {
        // arrange
        def input = new SimpleJavaClass().with {
            howdy = '123'
            it
        }

        // act
        def cursor = runFlow('mimeTypeTest') {
            java {
                inputPayload(input)
            }
        }

        // assert
        withCursorAsText(cursor) { String result ->
            assertThat result,
                       is(equalTo('"application/java"'))
        }
    }

    @Test
    void customMimeType() {
        // arrange
        def input = new SimpleJavaClass().with {
            howdy = '123'
            it
        }

        // act
        def cursor = runFlow('mimeTypeTest') {
            java {
                inputPayload(JsonOutput.toJson(input),
                             'application/json')
            }
        }

        // assert
        withCursorAsText(cursor) { String result ->
            assertThat result,
                       is(equalTo('"application/json"'))
        }
    }

    @Test
    void javaObject_using_java_invoke() {
        // arrange
        // you don't have to use this with java inputs but if your app serializes it (batch) or needs to
        // know what the class is, it will expect it to be w/ the app's classloader
        def input = instantiateJavaClassWithAppClassLoader(SimpleJavaClass).with {
            howdy = '123'
            it
        }

        // act
        def result = runFlow('javaFlowWithJavaInvokeUsage') {
            java {
                inputPayload(input)
            }
        }

        // assert
        assertThat result,
                   is(equalTo([
                           key                   : '123',
                           value                 : SimpleJavaClass.name,
                           value_from_java_module: '123'
                   ]))
    }
}
