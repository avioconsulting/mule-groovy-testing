package com.avioconsulting.mule.testing.spies

import org.mule.module.http.internal.request.ResponseValidator

interface IReceiveHttpOptions {
    def receive(Map queryParams,
                Map headers,
                String fullPath,
                String httpVerb,
                ResponseValidator responseValidator)
}