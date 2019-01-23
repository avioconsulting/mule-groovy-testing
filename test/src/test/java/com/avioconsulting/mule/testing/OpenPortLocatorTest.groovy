package com.avioconsulting.mule.testing

import org.apache.tools.ant.taskdefs.condition.Os
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class OpenPortLocatorTest implements OpenPortLocator {
    static int getExpectedPort() {
        // not sure why 8088 is occupied on Windows (nothing shows as listening on that port) but
        // took care of test once I changed this
        // where it's 8088 or 8089 for the first port is not important. What is import is
        // that the 2nd port is used if the first port is taken and that will be 8090 for Windows
        // see getHttpPort_secondPortOpen
        Os.isFamily(Os.FAMILY_WINDOWS) ? 8089 : 8088
    }

    @Test
    void getHttpPort_firstPortOpen() {
        // arrange

        // act
        def port = httpPort

        // assert
        assertThat port,
                   is(equalTo(expectedPort))
    }

    @Test
    void getHttpPort_secondPortOpen() {
        // arrange
        def serverSocket = new ServerSocket(expectedPort,
                                            1,
                                            InetAddress.loopbackAddress)
        try {

            // act
            def port = httpPort

            // assert
            assertThat port,
                       is(equalTo(expectedPort + 1))
        }
        finally {
            serverSocket.close()
        }
    }
}
