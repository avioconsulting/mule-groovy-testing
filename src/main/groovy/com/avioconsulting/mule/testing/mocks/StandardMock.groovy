package com.avioconsulting.mule.testing.mocks

import com.avioconsulting.mule.testing.mulereplacements.MockProcess
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.MockEventWrapper

class StandardMock implements MockProcess<ConnectorInfo> {
    private final MuleMessageTransformer mockTransformer

    StandardMock(MuleMessageTransformer mockTransformer) {
        this.mockTransformer = mockTransformer
    }

    @Override
    void process(MockEventWrapper event,
                 ConnectorInfo connectorInfo) {
        mockTransformer.transform(event,
                                  connectorInfo)
    }
}
