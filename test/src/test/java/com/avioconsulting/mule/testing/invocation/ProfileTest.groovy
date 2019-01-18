package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.ConfigTrait
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import org.junit.Test

import static org.hamcrest.Matchers.*
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

    @Test
    void real_classloader_model_preserved_for_packaging() {
        // arrange

        // act (happens already by virtue of loading the app)

        // assert
        def model = new File('target/META-INF/mule-artifact/classloader-model.json')
        assertThat 'packaging immediately follows testing. if we manipulate the model using profiles for the purposes of testing, it should not affect the real model',
                   model.text,
                   is(not(containsString('misc-dependency')))
    }
}
