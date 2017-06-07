package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.dsl.invokers.FlowRunner

abstract class BaseApikitTest extends BaseTest {
    protected abstract String getApiNameUnderTest()

    protected abstract String getApiVersionUnderTest()

    protected String getFullApiName() {
        "api-${apiNameUnderTest}-${apiVersionUnderTest}"
    }

    @Override
    Properties getStartUpProperties() {
        def properties = super.getStartUpProperties()
        // http listener gets going before the properties object this method creates has had its values take effect
        System.setProperty('avio.test.http.port',
                           getHttpPort() as String)
        properties.put('http.listener.config', 'test-http-listener-config')
        properties
    }

    @Override
    protected List<String> getFlowsExcludedOfInboundDisabling() {
        // apikit complains unless these 2 are both open
        ['main', 'console'].collect { suffix ->
            // toString here to ensure we return Java string and not Groovy strings
            "${fullApiName}-${suffix}".toString()
        }
    }

    @Override
    List<String> getConfigResourcesList() {
        [
                'global-test.xml',
                "${fullApiName}.xml".toString()
        ]
    }

    def runApiKitFlow(@DelegatesTo(FlowRunner) Closure closure) {

    }

    private Integer httpPort = null

    protected int getHttpPort() {
        if (httpPort) {
            return httpPort
        }
        httpPort = (8088..8199).find { candidate ->
            try {
                def socket = new ServerSocket(candidate)
                socket.close()
                true
            }
            catch (IOException ignored) {
                false
            }
        }
    }

}
