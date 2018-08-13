package org.mule.modules.interceptor.processors

import org.mule.api.MuleMessage

// TODO: Move to another package
interface MuleMessageTransformer {
    MuleMessage transform(MuleMessage var1)
}