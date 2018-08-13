package org.mule.modules.interceptor.processors

import org.mule.api.MuleMessage

interface MuleMessageTransformer {
    MuleMessage transform(MuleMessage var1)
}