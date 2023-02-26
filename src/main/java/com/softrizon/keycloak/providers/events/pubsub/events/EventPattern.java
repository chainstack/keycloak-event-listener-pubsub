package com.softrizon.keycloak.providers.events.pubsub.events;

import java.util.Objects;
import java.util.regex.Pattern;

public class EventPattern {

    public final Format format;
    public final Pattern pattern;
    public final String alias;

    public EventPattern(Format format, Pattern pattern, String alias) {
        this.format = format;
        this.pattern = pattern;
        this.alias = alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventPattern)) return false;
        EventPattern that = (EventPattern) o;

        return format == that.format
                && pattern.pattern().equals(that.pattern.pattern())
                && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(format, pattern.pattern(), alias);
    }

    @Override
    public String toString() {
        return "EventPattern{" +
                "format=" + format +
                ", pattern=" + pattern +
                ", alias=" + alias +
                '}';
    }

    public enum Format {JSON_API_V1}

    public enum Result {SUCCESS, ERROR}

    public enum Who {ADMIN, USER}
}
