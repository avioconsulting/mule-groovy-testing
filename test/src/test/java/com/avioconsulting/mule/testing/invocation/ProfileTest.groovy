package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.ConfigTrait
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class ProfileTest extends
        BaseJunitTest implements
        ConfigTrait {
    List<String> getConfigResources() {
        ['profile_test.xml']
    }

    @Override
    List<String> getMavenProfiles() {
        ['add-misc-dependency']
    }

    @Test
    void loads_ok() {
        // arrange
        def mockCalled = false
        mockRestHttpCall('the name of our connector') {
            json {
                whenCalledWith(String) { String ourPayload ->
                    mockCalled = true
                    'new payload'
                }
            }
        }

        // act
        runFlow('foo') {
            java {
                inputPayload(null)
            }
        }

        // assert
        assertThat 'If we did all this right, we should load and run OK. if not, we probably will not even load',
                   mockCalled,
                   is(equalTo(true))
    }
}
