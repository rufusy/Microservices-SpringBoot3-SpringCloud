package com.rufusy.microservices.composite.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rufusy.microservices.api.event.Event;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IsSameEvent extends TypeSafeMatcher<String> {
    private static final Logger LOG = LoggerFactory.getLogger(IsSameEvent.class);

    private final ObjectMapper mapper = new ObjectMapper();

    private final Event<?, ?> expectedEvent;

    private IsSameEvent(Event<?, ?> expectedEvent) {
        this.expectedEvent = expectedEvent;
    }

    @Override
    protected boolean matchesSafely(String eventAsJson) {
        if (expectedEvent == null) {
            return false;
        }

        LOG.trace("Convert the following json string to a map: {}", eventAsJson);
        Map<String, Object> mapEvent = convertJsonStringToMap(eventAsJson);
        mapEvent.remove("eventCreatedAt");

        Map<?, ?> mapExpectedEvent = getMapWithoutCreatedAt(expectedEvent);

        LOG.trace("Got the map: {}", mapEvent);
        LOG.trace("Compare to the expected map: {}", mapExpectedEvent);

        return mapEvent.equals(mapExpectedEvent);
    }

    @Override
    public void describeTo(Description description) {
        String expectedJson = convertObjectToJsonString(expectedEvent);
        description.appendText("expected to look like " + expectedJson);
    }

    public static Matcher<String> sameEventExceptCreatedAt(Event<?, ?> expectedEvent) {
        return new IsSameEvent(expectedEvent);
    }

    private Map<?, ?> getMapWithoutCreatedAt(Object object) {
        Map<?, ?> mapEvent = convertObjectToMap(object);
        mapEvent.remove("eventCreatedAt");
        return mapEvent;
    }

    private Map<?, ?> convertObjectToMap(Object object) {
        JsonNode node = mapper.convertValue(object, JsonNode.class);
        return mapper.convertValue(node, Map.class);
    }

    private String convertObjectToJsonString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> convertJsonStringToMap(String eventAsJson) {
        try {
            return mapper.readValue(eventAsJson, new TypeReference<HashMap<String, Object>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
