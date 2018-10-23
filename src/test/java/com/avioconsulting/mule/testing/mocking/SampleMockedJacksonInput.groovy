package com.avioconsulting.mule.testing.mocking

import com.fasterxml.jackson.annotation.JsonProperty

class SampleMockedJacksonInput {
    @JsonProperty('key')
    int foobar
}
