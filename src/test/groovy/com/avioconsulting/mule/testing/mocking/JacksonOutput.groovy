package com.avioconsulting.mule.testing.mocking

import com.fasterxml.jackson.annotation.JsonProperty

class JacksonOutput {
    @JsonProperty('reply_key')
    int result
}
