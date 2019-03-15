package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.junit.BaseJunitTest
import org.junit.Test

import static org.hamcrest.Matchers.containsString
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

        // act
        def serverPatches = new FileNameFinder().getFileNames('.mule/lib',
                                                              '**/*')

        // assert
        assertThat serverPatches.join(','),
                   is(containsString('SE-9559-4.1.5-1.0.jar'))
    }
}
