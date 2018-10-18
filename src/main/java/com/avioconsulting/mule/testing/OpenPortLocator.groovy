package com.avioconsulting.mule.testing

class OpenPortLocator {
    static int getHttpPort() {
        (8088..8199).find { candidate ->
            try {
                def socket = new ServerSocket(candidate,
                                              1,
                                              InetAddress.loopbackAddress)
                socket.close()
                true
            }
            catch (IOException ignored) {
                false
            }
        }
    }

}
