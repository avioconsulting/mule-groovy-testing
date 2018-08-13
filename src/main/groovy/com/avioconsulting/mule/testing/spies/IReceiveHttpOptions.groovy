package com.avioconsulting.mule.testing.spies

import org.mule.module.http.internal.request.DefaultHttpRequester

interface IReceiveHttpOptions {
    def receive(Map queryParams,
                Map headers,
                String fullPath,
                DefaultHttpRequester httpRequester)
}