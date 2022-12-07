package com.softrizon.keycloak.providers.events.pubsub.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.keycloak.events.Event;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "_class")
public class UserEventMessage extends Event implements Serializable {

    private static final long serialVersionUID = -1L;

    private Date createdAt;

    public Date getCreatedAt() {
        return createdAt;
    }

    protected void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public static UserEventMessage create(Event event) {
        UserEventMessage message = new UserEventMessage();
        message.setClientId(event.getClientId());
        message.setDetails(event.getDetails());
        message.setError(event.getError());
        message.setIpAddress(event.getIpAddress());
        message.setRealmId(event.getRealmId());
        message.setSessionId(event.getSessionId());
        message.setTime(event.getTime());
        message.setCreatedAt(new Date(event.getTime()));
        message.setType(event.getType());
        message.setUserId(event.getUserId());

        return message;
    }
}
