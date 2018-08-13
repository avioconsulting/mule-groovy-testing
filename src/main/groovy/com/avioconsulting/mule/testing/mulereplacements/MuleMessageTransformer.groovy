package com.avioconsulting.mule.testing.mulereplacements

import org.mule.api.MuleMessage

// TODO: Move to another package
interface MuleMessageTransformer {
    MuleMessage transform(MuleMessage var1)
}