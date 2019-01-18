package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.ConfigTrait
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import org.junit.Test

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

        // act
        runFlow('foo') {
            java {
                inputPayload(null)
            }
        }

        // assert
        fail 'write the test'
    }
}
