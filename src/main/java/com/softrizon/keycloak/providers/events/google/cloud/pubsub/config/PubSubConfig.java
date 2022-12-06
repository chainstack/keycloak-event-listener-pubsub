package com.softrizon.keycloak.providers.events.google.cloud.pubsub.config;

import com.softrizon.keycloak.providers.events.google.cloud.pubsub.events.EventPattern;
import com.softrizon.keycloak.providers.events.google.cloud.pubsub.events.EventPatternParser;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;

import java.util.*;
import java.util.stream.Collectors;

public class PubSubConfig {

    public static final String PLUGIN_NAME = "event-listener-pubsub";
    private static final Logger logger = Logger.getLogger(PubSubConfig.class);
    private static final EventPatternParser parser = new EventPatternParser();

    private String topicId;
    private final Set<EventPattern> userEventTypes = new HashSet<>();
    private final Set<EventPattern> adminEventTypes = new HashSet<>();

    public String getTopicId() {
        return topicId;
    }

    public Set<EventPattern> getUserEventTypes() {
        return userEventTypes;
    }

    public Set<EventPattern> getAdminEventTypes() {
        return adminEventTypes;
    }

    public static String createEventName(AdminEvent event) {
        // Event example: ADMIN:<REALM_ID>:<RESULT = SUCCESS | ERROR>:<RESOURCE_TYPE>:<OPERATION_TYPE>
        return String.format(Locale.US, "ADMIN:%s:%s:%s:%s",
                event.getRealmId(), processResult(event.getError()), event.getResourceTypeAsString(),
                event.getOperationType().toString());

    }

    public static String createEventName(Event event) {
        // Event example: USER:<REALM_ID>:<RESULT = SUCCESS | ERROR>:<CLIENT_ID>:<EVENT_TYPE>
        return String.format(Locale.US, "USER:%s:%s:%s:%s",
                event.getRealmId(), processResult(event.getError()), event.getClientId(),
                event.getType().toString());
    }

    public static Map<String, String> getMessageAttributes(AdminEvent event) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("format", EventPattern.Format.JSON_API_V1.toString());
        attributes.put("who", "ADMIN");
        attributes.put("realmId", event.getRealmId());
        attributes.put("resourceType", event.getResourceTypeAsString());
        attributes.put("operationType", event.getOperationType().toString());
        attributes.put("result", processResult(event.getError()));
        attributes.put("eventName", createEventName(event));

        return attributes;
    }

    public static Map<String, String> getMessageAttributes(Event event) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("format", EventPattern.Format.JSON_API_V1.toString());
        attributes.put("who", "USER");
        attributes.put("realmId", event.getRealmId());
        attributes.put("clientId", event.getClientId());
        attributes.put("eventType", event.getType().toString());
        attributes.put("result", processResult(event.getError()));
        attributes.put("eventName", createEventName(event));

        return attributes;
    }

    public static PubSubConfig create(Config.Scope scope) {
        PubSubConfig config = new PubSubConfig();
        String format = EventPattern.Format.JSON_API_V1.toString();

        // Process the topic id
        config.topicId = resolveConfigVariable(scope, "topic_id", null);
        Objects.requireNonNull(config.topicId, String.format("%s: the topic id is required.", PLUGIN_NAME));

        // Process registered user events
        final String userEvents = resolveConfigVariable(scope, "user_event_patterns", "USER:*:*:*:*");
        final EventPattern[] userEventPatterns = parseEventTypes(userEvents).stream()
                .map(event -> parser.parse(format, event))
                .toArray(EventPattern[]::new);
        config.userEventTypes.addAll(Arrays.asList(userEventPatterns));

        // Process registered admin events
        final String adminEvents = resolveConfigVariable(scope, "admin_event_patterns", "ADMIN:*:*:*:*");
        final EventPattern[] adminEventPatterns = parseEventTypes(adminEvents).stream()
                .map(event -> parser.parse(format, event))
                .toArray(EventPattern[]::new);
        config.adminEventTypes.addAll(Arrays.asList(adminEventPatterns));

        return config;
    }

    private static String resolveConfigVariable(Config.Scope scope, String variable, String defaultValue) {
        Objects.requireNonNull(variable, String.format("%s: the variable name is required.", PLUGIN_NAME));

        String value = defaultValue;

        // Extract the value for this variable
        if (scope != null && scope.get(variable) != null) {
            value = scope.get(variable);
        } else { // Try to retrieve the value for this variable from environment variables. Eg: SN_KC_PUBSUB_TOPIC_ID.
            String envVariable = String.format(Locale.US, "SN_KC_PUBSUB_%s", variable.toUpperCase(Locale.US));
            String tmpValue = System.getenv(envVariable);
            if (tmpValue != null) {
                value = tmpValue;
            }
        }

        logger.infof("%s: configuration: %s=%s.%n", PLUGIN_NAME, variable, value);

        return value;
    }

    private static List<String> parseEventTypes(String events) {
        return Arrays.stream(events.split(","))
                .map(item -> item.trim().toUpperCase(Locale.US))
                .collect(Collectors.toList());
    }

    private static String processResult(String error) {
        return error == null ? "SUCCESS" : "ERROR";
    }
}
