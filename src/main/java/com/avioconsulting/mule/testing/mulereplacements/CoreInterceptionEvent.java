package com.avioconsulting.mule.testing.mulereplacements;

import org.mule.runtime.api.el.BindingContext;
import org.mule.runtime.api.event.EventContext;
import org.mule.runtime.api.message.Error;
import org.mule.runtime.api.message.ItemSequenceInfo;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.security.Authentication;
import org.mule.runtime.api.security.SecurityContext;
import org.mule.runtime.core.api.context.notification.FlowCallStack;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.message.GroupCorrelation;
import org.mule.runtime.core.internal.interception.DefaultInterceptionEvent;

import java.util.Map;
import java.util.Optional;

// Unfortunately, DefaultInterceptionEvent is not a CoreEvent but we need to have
// CoreEvents to create streaming payloads
public class CoreInterceptionEvent implements CoreEvent {
    private final DefaultInterceptionEvent event;

    CoreInterceptionEvent(DefaultInterceptionEvent event) {
        this.event = event;
    }

    @Override
    public SecurityContext getSecurityContext() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Optional<GroupCorrelation> getGroupCorrelation() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public FlowCallStack getFlowCallStack() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Map<String, TypedValue<?>> getVariables() {
        return event.getVariables();
    }

    @Override
    public Message getMessage() {
        return event.getMessage();
    }

    @Override
    public Optional<Authentication> getAuthentication() {
        return event.getAuthentication();
    }

    @Override
    public Optional<Error> getError() {
        return event.getError();
    }

    @Override
    public String getCorrelationId() {
        return event.getCorrelationId();
    }

    @Override
    public Optional<ItemSequenceInfo> getItemSequenceInfo() {
        return event.getItemSequenceInfo();
    }

    @Override
    public EventContext getContext() {
        return event.getContext();
    }

    @Override
    public BindingContext asBindingContext() {
        return event.asBindingContext();
    }
}
