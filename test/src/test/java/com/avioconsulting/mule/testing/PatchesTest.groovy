package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.junit.BaseJunitTest
import org.junit.Ignore
import org.junit.Test

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

@Ignore("We do not currently have any patches")
class PatchesTest extends
        BaseJunitTest implements
        ConfigTrait {
    List<String> getConfigResources() {
        ['java_test.xml']
    }

    @Test
    void patch_exists() {
        // arrange

        // act
        def serverPatches = new FileNameFinder().getFileNames('.mule/lib',
                                                              '**/*')

        // assert
        assertThat serverPatches.size(),
                   is(equalTo(1))
        def patch = serverPatches[0]
        assertThat patch,
                   is(endsWith('MULE-17736-4.2.2-2.0.jar'))
    }
}
