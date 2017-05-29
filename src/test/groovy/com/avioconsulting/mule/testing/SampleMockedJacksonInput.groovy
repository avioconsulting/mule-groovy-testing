package com.avioconsulting.mule.testing

import com.fasterxml.jackson.annotation.JsonProperty

class SampleMockedJacksonInput {
    @JsonProperty('key')
    int foobar
}
