package com.avioconsulting.mule.testing.transformers.json.output

import com.fasterxml.jackson.databind.ObjectMapper
import org.mule.api.MuleContext

class JacksonOutputTransformer extends Common {
    def mapper = new ObjectMapper()

    JacksonOutputTransformer(MuleContext muleContext) {
        super(muleContext)
    }

    String getJsonOutput(Object response) {
        mapper.writer().writeValueAsString(response)
    }
}
