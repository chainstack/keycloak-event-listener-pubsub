package com.softrizon.keycloak.providers.events.google.cloud.pubsub.config;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PubSubConfig {

    public static final String PLUGIN_NAME = "event-listener-pubsub";
    public static final String EVENT_FORMAT = "JSON_API_V1";

    private static final Logger logger = Logger.getLogger(PubSubConfig.class);
    private static final Pattern SPECIAL_CHARACTERS = Pattern.compile("[^a-zA-Z0-9_.-]");
    private static final Pattern SPACE = Pattern.compile(" ");
    private static final Pattern DOT = Pattern.compile("\\.");

    private String topicId;
    private final List<String> userEventTypes = new ArrayList<>();
    private final List<String> adminOperationTypes = new ArrayList<>();

    public String getTopicId() {
        return topicId;
    }

    public List<String> getUserEventTypes() {
        return userEventTypes;
    }

    public List<String> getAdminOperationTypes() {
        return adminOperationTypes;
    }

    public Map<String, String> getMessageAttributes(AdminEvent event) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("format", EVENT_FORMAT);
        // Event example: ADMIN.<REALM_ID>.<RESULT = SUCCESS | ERROR>.<RESOURCE_TYPE>.<OPERATION>
        final String eventName = String.format(Locale.US, "ADMIN.%s.%s.%s.%s",
                removeDots(event.getRealmId()),
                (event.getError() == null ? "SUCCESS" : "ERROR"),
                event.getResourceTypeAsString(),
                event.getOperationType().toString());
        attributes.put("event", normalizeEventName(eventName));

        return attributes;
    }

    public Map<String, String> getMessageAttributes(Event event) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("format", EVENT_FORMAT);
        // Event example: USER.<REALM_ID>.<RESULT = SUCCESS | ERROR>.<EVENT_TYPE>
        final String eventName = String.format(Locale.US, "USER.%s.%s.%s",
                removeDots(event.getRealmId()),
                (event.getError() == null ? "SUCCESS" : "ERROR"),
                event.getType().toString());
        attributes.put("event", normalizeEventName(eventName));

        return attributes;
    }

    public static PubSubConfig create(Config.Scope scope) {
        PubSubConfig config = new PubSubConfig();

        // Process the topic id
        config.topicId = resolveConfigVariable(scope, "topic_id", null);
        Objects.requireNonNull(config.topicId, String.format("%s: the topic id is required.", PLUGIN_NAME));

        // Process registered user events
        final String userEvents = resolveConfigVariable(scope, "user_event_types", "REGISTER,DELETE_ACCOUNT,UPDATE_EMAIL");
        config.userEventTypes.addAll(parseEventTypes(userEvents));

        // Process registered admin events
        final String adminEvents = resolveConfigVariable(scope, "admin_operation_types", "CREATE,DELETE,UPDATE");
        config.adminOperationTypes.addAll(parseEventTypes(adminEvents));

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

    public static String normalizeEventName(CharSequence eventName) {
        if (eventName != null) {
            return SPACE.matcher(SPECIAL_CHARACTERS.matcher(eventName).replaceAll(""))
                    .replaceAll("_")
                    .toUpperCase(Locale.US);
        }

        return null;
    }

    public static String removeDots(String string) {
        if (string != null) {
            return DOT.matcher(string)
                    .replaceAll("")
                    .toUpperCase(Locale.US);
        }

        return null;
    }

    private static List<String> parseEventTypes(String events) {
        return Arrays.stream(events.split(","))
                .map(item -> item.trim().toUpperCase(Locale.US))
                .collect(Collectors.toList());
    }
}
