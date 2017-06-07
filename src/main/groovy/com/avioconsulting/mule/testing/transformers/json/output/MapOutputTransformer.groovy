package com.avioconsulting.mule.testing.transformers.json.output

import groovy.json.JsonOutput
import org.mule.api.MuleContext

class MapOutputTransformer extends Common {
    MapOutputTransformer(MuleContext muleContext) {
        super(muleContext)
    }

    String getJsonOutput(Object input) {
        JsonOutput.toJson(input)
    }
}
