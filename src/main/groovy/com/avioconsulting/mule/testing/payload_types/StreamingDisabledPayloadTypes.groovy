package com.avioconsulting.mule.testing.payload_types

class StreamingDisabledPayloadTypes implements IFetchAllowedPayloadTypes {
    List<Class> getAllowedPayloadTypes() {
        [String]
    }
}
