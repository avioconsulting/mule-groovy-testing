package com.avioconsulting.mule.testing

import com.fasterxml.jackson.annotation.JsonProperty

class SampleMockedJacksonOutput {
    @JsonProperty('reply')
    int foobar
}
