package com.softrizon.keycloak.providers.events.pubsub.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.keycloak.events.Event;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "_class")
public class UserEventMessage extends Event implements Serializable {

    private static final long serialVersionUID = -1L;

    public static UserEventMessage create(Event event) {
        UserEventMessage message = new UserEventMessage();
        message.setId(event.getId());
        message.setClientId(event.getClientId());
        message.setDetails(event.getDetails());
        message.setError(event.getError());
        message.setIpAddress(event.getIpAddress());
        message.setRealmId(event.getRealmId());
        message.setSessionId(event.getSessionId());
        message.setTime(event.getTime());
        message.setType(event.getType());
        message.setUserId(event.getUserId());

        return message;
    }
}
