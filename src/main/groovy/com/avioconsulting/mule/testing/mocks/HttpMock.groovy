package com.avioconsulting.mule.testing.mocks

import com.avioconsulting.mule.testing.mulereplacements.MockProcess
import org.mule.api.MuleEvent
import org.mule.module.http.internal.request.DefaultHttpRequester

// TODO: Tie this in with the transformers
class HttpMock implements MockProcess<DefaultHttpRequester> {
    @Override
    MuleEvent process(MuleEvent event,
                      DefaultHttpRequester originalProcessor) {
        return null
    }
}
