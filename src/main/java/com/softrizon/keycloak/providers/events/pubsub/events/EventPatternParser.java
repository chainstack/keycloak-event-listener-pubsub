package com.softrizon.keycloak.providers.events.pubsub.events;

import org.keycloak.events.EventType;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.softrizon.keycloak.providers.events.pubsub.config.PubSubConfig.PLUGIN_NAME;

public class EventPatternParser {

    private final Pattern REALM_ID_PATTERN = Pattern.compile("[^:/?#\\[\\]@!$&()*+,;=']+");
    private final Pattern CLIENT_ID_PATTERN = Pattern.compile(".*");
    private final Pattern RESULT_PATTERN = Pattern.compile("(?:SUCCESS|ERROR)");
    private final Pattern ENUM_TYPE_PATTERN = Pattern.compile("[0-9a-zA-Z_]+");
    private final Pattern SEMI_COLON_PATTERN = Pattern.compile(":");
    private final Pattern PIPE_PATTERN = Pattern.compile("\\|");

    private final List<String> formats;
    private final List<String> results;
    private final List<String> whos;

    public EventPatternParser() {
        // Extract the formats
        formats = Arrays.stream(EventPattern.Format.values())
                .map(Enum::toString)
                .collect(Collectors.toList());

        // Extract the results
        results = Arrays.stream(EventPattern.Result.values())
                .map(Enum::toString)
                .collect(Collectors.toList());

        // Extract the 'whos'
        whos = Arrays.stream(EventPattern.Who.values())
                .map(Enum::toString)
                .collect(Collectors.toList());
    }

    public EventPattern parse(String format, String pattern) {
        // Validate the format
        Objects.requireNonNull(format, String.format("%s: event format is required.", PLUGIN_NAME));
        format = format.trim().toUpperCase(Locale.US);

        // Require pattern
        Objects.requireNonNull(pattern, String.format("%s: event pattern is required.", PLUGIN_NAME));

        // Extract the alias if it exists
        String alias = null;
        String originalPattern = pattern;
        String[] components = Arrays.stream(pattern.split(PIPE_PATTERN.pattern()))
                .map(item -> item.trim().toUpperCase(Locale.US))
                .toArray(String[]::new);
        if (components.length == 2) {
            originalPattern = components[0];
            alias = components[1];
        }

        // Extract the components
        String[] parts = Arrays.stream(originalPattern.split(SEMI_COLON_PATTERN.pattern()))
                .map(item -> item.trim().toUpperCase(Locale.US))
                .toArray(String[]::new);

        // Make sure pattern length is checked
        if (parts.length != 5) {
            throw new IllegalArgumentException(String.format(Locale.US,
                    "%s: event pattern should have exactly 5 components, but %d were found.",
                    PLUGIN_NAME, parts.length));
        }

        // Make sure the format is checked
        if (!formats.contains(format)) {
            throw new IllegalArgumentException(String.format(Locale.US,
                    "%s: event format should be one of: %s, but '%s' was found.", PLUGIN_NAME, formats, format));
        }
        EventPattern.Format newFormat = EventPattern.Format.valueOf(format);

        // Validate the 'who' component
        if (!whos.contains(parts[0])) {
            throw new IllegalArgumentException(String.format(Locale.US,
                    "%s: event component index 0 should be one of: %s, but '%s' was found.",
                    PLUGIN_NAME, whos, parts[0]));
        }
        EventPattern.Who who = EventPattern.Who.valueOf(parts[0]);

        // Make sure the realm id is checked
        String realmId = extractRealmId(parts);
        Objects.requireNonNull(realmId, String.format("%s: event realm id '%s' is invalid.", PLUGIN_NAME, parts[1]));

        // Make sure the result is checked
        String result = extractResult(parts, results);
        Objects.requireNonNull(result, String.format("%s: event result should be one of: %s, but '%s' was found.",
                PLUGIN_NAME, results, parts[2]));

        // Create event pattern
        Pattern newPattern = null;

        // Process admin parameters
        if (who == EventPattern.Who.ADMIN) {
            // Extract the resource type
            String resourceType = extractResourceType(parts);
            Objects.requireNonNull(resourceType, String.format("%s: admin event resource type '%s' is invalid.",
                    PLUGIN_NAME, parts[3]));

            // Extract the operation type
            String operationType = extractOperationType(parts);
            Objects.requireNonNull(operationType, String.format("%s: admin event operation type '%s' is invalid.",
                    PLUGIN_NAME, parts[4]));

            // Event example: ADMIN:<REALM_ID>:<RESULT>:<RESOURCE_TYPE>:<OPERATION_TYPE>
            newPattern = Pattern.compile(String.format(Locale.US, "%s:%s:%s:%s:%s",
                    who, realmId, result, resourceType, operationType));
        } else if (who == EventPattern.Who.USER) { // Process user parameters
            // Extract the client id
            String clientId = extractClientId(parts);
            Objects.requireNonNull(clientId, String.format("%s: event client id '%s' is invalid.",
                    PLUGIN_NAME, parts[3]));

            // Extract the event type
            String eventType = extractEventType(parts);
            Objects.requireNonNull(eventType, String.format("%s: user event type '%s' is invalid.",
                    PLUGIN_NAME, parts[4]));

            // Event example: USER:<REALM_ID>:<RESULT>:<CLIENT_ID>:<EVENT_TYPE>
            newPattern = Pattern.compile(String.format(Locale.US, "%s:%s:%s:%s:%s",
                    who, realmId, result, clientId, eventType));
        }

        return new EventPattern(newFormat, newPattern, alias);
    }

    private String extractRealmId(String[] parts) {
        if ("*".equals(parts[1])) return REALM_ID_PATTERN.pattern();
        else if (REALM_ID_PATTERN.matcher(parts[1]).matches()) return parts[1];
        return null;
    }

    private String extractResult(String[] parts, List<String> results) {
        if ("*".equals(parts[2])) return RESULT_PATTERN.pattern();
        else if (results.contains(parts[2])) return parts[2];
        return null;
    }

    private String extractResourceType(String[] parts) {
        List<String> resourceTypes = Arrays.stream(ResourceType.values())
                .map(Enum::toString)
                .collect(Collectors.toList());

        if ("*".equals(parts[3])) return ENUM_TYPE_PATTERN.pattern();
        else if (resourceTypes.contains(parts[3])) return parts[3];
        return null;
    }

    private String extractOperationType(String[] parts) {
        List<String> operationTypes = Arrays.stream(OperationType.values())
                .map(Enum::toString)
                .collect(Collectors.toList());

        if ("*".equals(parts[4])) return ENUM_TYPE_PATTERN.pattern();
        else if (operationTypes.contains(parts[4])) return parts[4];
        return null;
    }

    private String extractClientId(String[] parts) {
        if ("*".equals(parts[3])) return CLIENT_ID_PATTERN.pattern();
        else if (CLIENT_ID_PATTERN.matcher(parts[3]).matches()) return parts[3];
        return null;
    }

    private String extractEventType(String[] parts) {
        List<String> eventTypes = Arrays.stream(EventType.values())
                .map(Enum::toString)
                .collect(Collectors.toList());

        if ("*".equals(parts[4])) return ENUM_TYPE_PATTERN.pattern();
        else if (eventTypes.contains(parts[4])) return parts[4];
        return null;
    }
}
