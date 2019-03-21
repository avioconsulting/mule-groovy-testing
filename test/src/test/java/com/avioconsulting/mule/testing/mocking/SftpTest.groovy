package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.ConfigTrait
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class SftpTest extends
        BaseJunitTest implements
        ConfigTrait {
    List<String> getConfigResources() {
        ['sftp_test.xml']
    }

    @Test
    void mocks_ok() {
        // arrange
        def mockCalled = false
        mockGeneric('Move the File') {
            raw {
                whenCalledWith { input ->
                    mockCalled = true
                }
            }
        }

        // act
        runFlow('move-stuff') {
            java {
                inputPayload(null)
            }
        }

        // assert
        assertThat mockCalled,
                   is(equalTo(true))
    }
}
