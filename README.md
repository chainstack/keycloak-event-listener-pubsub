# keycloak-event-listener-pubsub

This SPI allows one to listen and filter various event types from a Keycloak server
and publishes them to a Google Cloud Pub/Sub topic. It has been deployed on Keycloak 20.0.5 server
on a Kubernetes cluster. We did some simple tests on Keycloak 18.0.2, so if you run into issues,
we welcome your pull requests :).

## Configuration

Add the following variables to your environment.

```
# Required variables
SN_PUBSUB_SERVICE_ACCOUNT_CREDENTIALS_FILE_PATH=/path/to/your/google/service_account_key_file.json
SN_PUBSUB_PROJECT_ID=your_project_id
SN_PUBSUB_TOPIC_ID=your_topic_id

# Optional variables
SN_KEYCLOAK_USER_EVENT_PATTERNS=USER:*:*:*:*
SN_KEYCLOAK_ADMIN_EVENT_PATTERNS=ADMIN:*:*:*:*
```

## Event patterns

By default, the SPI matches all events of type user and admin. To match or filter specific events you are interested in,
you define an event pattern. An event pattern is a string of 5 components seperated by semicolon.

* Component 1 is a Keycloak event type. It can be either `USER` or `ADMIN`.
* Component 2 is a realm ID.
* Component 3 is the result of an event. Either `SUCCESS` or `ERROR`.
* Component 4 is a client id for event of type `USER`
  and [resource type](https://www.keycloak.org/docs-api/18.0/javadocs/org/keycloak/events/admin/ResourceType.html)
  for `ADMIN`.
* Component 5 is an [event type](https://www.keycloak.org/docs-api/18.0/javadocs/org/keycloak/events/EventType.html) of
  event of type `USER`
  and [operation type](https://www.keycloak.org/docs-api/18.0/javadocs/org/keycloak/events/admin/OperationType.html)
  for `ADMIN`.

All components support the wildcard character `*` except for the first one.

### User event pattern examples

```
# Pattern placeholders
USER:<REALM_ID>:<RESULT>:<CLIENT_ID>:<EVENT_TYPE>

# Matches all events of type user for any realm id, result, client id or event type
USER:*:*:*:*

# Matches the REGISTER event type for any realm id, result, or client id.
USER:*:*:*:REGISTER

# Matches successfull registration of user from the the android-app client for any realm id.
USER:*:SUCCESS:android-app:REGISTER
```

### Admin event pattern examples

```
# Pattern placeholders
ADMIN:<REALM_ID>:<RESULT>:<RESOURCE_TYPE>:<OPERATION_TYPE>

# Matches all events of type admin for any realm id, result, resource type or operation type
ADMIN:*:*:*:*

# Matches successfull admin user updates for any realm id.
ADMIN:*:SUCCESS:USER:UPDATE
```

### Event pattern with alias examples

You can use aliases to rename matched events. For example, the following pattern `USER:*:SUCCESS:android-app:REGISTER`
will match all successful user registration events from the android-app and will publish them to the Pub/Sub topic with
the event name `USER:super-app-realm:SUCCESS:android-app:REGISTER`. Due to the 255 character-long of a Pub/Sub filter,
you might want to use aliases to shorten the event name as follows:

```
# Use a pipe to separate the pattern from the alias and the alias will be published to the Pub/Sub topic each time we 
# match the pattern. Different patterns can use the same alias.
USER:*:SUCCESS:android-app:REGISTER|USR_REG
```

## Event example output

All events will have a set of message attributes and a body when published to the Pub/Sub topic. You can use the
attributes to filter which messages get sent to a particular
subscription. [See how to filter messages from a subscription](https://cloud.google.com/pubsub/docs/subscription-message-filter).

#### Message attributes

| Field | Value | Description
| -- | ---- | ---- |
| format | JSON_API_V1 | The message body format and version. The only one supported for now. |
| who | USER | Event type. Can be USER or ADMIN. |
| realmId | super-app-realm | The id of the realm. |
| clientId | android-app | The client id of your app. |
| resourceType | USER | One of the possible value of the resource type enum of the ink above. |
| operationType | UPDATE | One of the possible value of the operation type enum of the link above. |
| eventType | REGISTER | One of the possible value of the event type enum of the link above. |
| event | USER:super-app-realm:SUCCESS:android-app:REGISTER | A fully qualified event name to use in your subscription
filters. |

#### Message body

```json
{
  "_class": "com.softrizon.keycloak.providers.events.pubsub.events.UserEventMessage",
  "time": 1670374956103,
  "type": "REGISTER",
  "realmId": "super-app-realm",
  "clientId": "android-app",
  "userId": "6e156e5e-4f1f-479f-8c54-11969cd2c8d2",
  "ipAddress": "127.0.0.1",
  "details": {
    "auth_method": "openid-connect",
    "auth_type": "code",
    "register_method": "form",
    "first_name": "John",
    "last_name": "Doe",
    "redirect_uri": "http://127.0.0.1:8080/realms/super-app-realm/account/#/",
    "code_id": "6536047c-4325-41e1-a563-576777b7c70e",
    "email": "john.doe@gmail.com",
    "username": "john.doe@gmail.com"
  },
  "createdAt": "2022-12-07T01:02:36Z"
}
```

## Build

### From source

Clone the repository and execute the command below in the project's root directory to build and install this package
locally. Post installation, continue with steps 3 and 4 below.

```
mvn clean install
```

### Download the pre-build package

1. [Download the latest jar file](https://github.com/softrizon/keycloak-event-listener-pubsub/releases/download/1.0/event-listener-pubsub-1.0.jar).
2. Copy the jar file into your bitnami Keycloak
   installation `/opt/bitnami/keycloak/providers/event-listener-pubsub-1.0.jar`.
3. Restart the Keycloak server.
4. Enable logging in Keycloak UI by adding **event-listener-pubsub**
   `Manage > Events > Config > Events Config > Event Listeners`.
