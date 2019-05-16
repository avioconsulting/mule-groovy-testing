package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.transformers.ClosureEvaluationResponse
import groovy.transform.Canonical

@Canonical
class HttpClosureEvalResponse extends ClosureEvaluationResponse {
    int httpStatus
}
