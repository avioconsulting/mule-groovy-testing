package com.avioconsulting.mule.testing

import com.fasterxml.jackson.annotation.JsonProperty

class SampleJacksonOutput {
    @JsonProperty('reply_key')
    int result
}
