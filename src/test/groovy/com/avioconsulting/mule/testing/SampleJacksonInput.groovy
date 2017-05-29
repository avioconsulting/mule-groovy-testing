package com.avioconsulting.mule.testing

import com.fasterxml.jackson.annotation.JsonProperty

class SampleJacksonInput {
    @JsonProperty('foo')
    int foobar
}
