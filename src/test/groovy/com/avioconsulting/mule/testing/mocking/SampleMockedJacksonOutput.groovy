package com.avioconsulting.mule.testing.mocking

import com.fasterxml.jackson.annotation.JsonProperty

class SampleMockedJacksonOutput {
    @JsonProperty('reply')
    int foobar
}
