package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.junit.BaseJunitTest
import org.junit.Test

import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class PatchesTest extends
        BaseJunitTest implements
        ConfigTrait {
    List<String> getConfigResources() {
        ['java_test.xml']
    }

    @Test
    void patch_exists() {
        // arrange
        def serverPatches = []

        // act
        new File('.mule/lib').eachFileRecurse {
            def filename = it.name
            if (filename.endsWith('.jar')) {
                serverPatches << filename
            }
        }

        // assert
        assertThat 'We do not currently have any patches',
                   serverPatches,
                   is([
                   ])
    }
}
