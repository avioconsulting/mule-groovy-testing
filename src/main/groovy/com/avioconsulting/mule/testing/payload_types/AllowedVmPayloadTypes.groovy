package com.avioconsulting.mule.testing.payload_types

import com.avioconsulting.mule.testing.payload_types.IFetchAllowedPayloadTypes

class AllowedVmPayloadTypes implements IFetchAllowedPayloadTypes {
    List<Class> getAllowedPayloadTypes() {
        [String]
    }
}
