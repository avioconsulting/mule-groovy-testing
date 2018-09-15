package com.avioconsulting.mule.testing.mulereplacements;

import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.interception.InterceptionAction;
import org.mule.runtime.api.interception.InterceptionEvent;
import org.mule.runtime.api.interception.ProcessorInterceptor;
import org.mule.runtime.api.interception.ProcessorParameterValue;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.TypedValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class OurInt implements ProcessorInterceptor {
    private final MockingConfiguration mockingConfiguration;

    OurInt(MockingConfiguration mockingConfiguration) {
        this.mockingConfiguration = mockingConfiguration;
    }

    // TODO: See how to get our mocking config in here and call that instead
    @Override
    public CompletableFuture<InterceptionEvent> around(ComponentLocation location,
                                                       Map<String, ProcessorParameterValue> parameters,
                                                       InterceptionEvent event,
                                                       InterceptionAction action) {
        if (parameters.containsKey("doc:name") &&
                parameters.get("doc:name").providedValue().equals("Our Request")) {
            System.out.println("our mock ran!");
            Map<String, String> result = new HashMap<>();
            result.put("howdy", "nope");
            Message newMessage = Message.builder()
                    .payload(TypedValue.of(result))
                    .build();
            event.message(newMessage);
            return action.skip();
        }
        return action.proceed();
    }
}
