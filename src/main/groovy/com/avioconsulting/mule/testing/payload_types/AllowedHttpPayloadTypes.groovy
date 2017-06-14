package com.avioconsulting.mule.testing.payload_types

class AllowedHttpPayloadTypes implements IFetchAllowedPayloadTypes {
    List<Class> getAllowedPayloadTypes() {
        [InputStream]
    }
}
