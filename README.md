# keycloak-event-listener-pubsub

This SPI allows one to listen and filter various event types from a Keycloak server
and publishes them to a Google Cloud Pub/Sub topic. It has been deployed on Keycloak 19.0.3 server
on a Kubernetes cluster. We did some simple tests on Keycloak 18.0.2, so if you run into issues,
we welcome your pull requests :).

# Configuration

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
USER:<REALM_ID>:<RESULT = SUCCESS | ERROR>:<CLIENT_ID>:<EVENT_TYPE>

# Matches all events of type user for any realm id, result, client id or event type
USER:*:*:*:*

# Matches the REGISTER event type for any realm id, result, or client id.
USER:*:*:*:REGISTER

# Matches successfull registration of user from the the android-app client from any realm.
USER:*:SUCCESS:android-app:REGISTER
```

### Admin event pattern examples

```
# Pattern placeholders
ADMIN:<REALM_ID>:<RESULT = SUCCESS | ERROR>:<RESOURCE_TYPE>:<OPERATION_TYPE>

# Matches all events of type admin for any realm id, result, client id or event type
ADMIN:*:*:*:*

# Matches successfull user updates from an admin for any realm.
ADMIN:*:SUCCESS:USER:UPDATE
```

# Build

## From source

Execute the command below to build and install this package locally. Post installation,
continue with steps 3 and 4 below.

```
mvn clean install
```

## Download the pre-build package

1. [Download the latest jar file](https://github.com/softrizon/keycloak-event-listener-google-cloud-pubsub/blob/target/event-listener-pubsub-1.0.0.jar?raw=true)
2. Copy the jar file into your bitnami Keycloak
   installation `/opt/bitnami/keycloak/providers/event-listener-pubsub-1.0.0.jar`
3. Restart the Keycloak server
4. Enable logging in Keycloak UI by adding **event-listener-pubsub**
   `Manage > Events > Config > Events Config > Event Listeners`

## Message attributes

Event representation published to pub/sub

format = JSON_API_V1
who=ADMIN | USER
realmId = master
result= SUCCESS | ERROR
clientId=test-app
resourceType=USER
operationType=UPDATE
eventType=REGISTER
event =
USER:<REALM_ID>:<RESULT = SUCCESS | ERROR>:<CLIENT_ID>:<EVENT_TYPE>
ADMIN:<REALM_ID>:<RESULT = SUCCESS | ERROR>:<RESOURCE_TYPE>:<OPERATION_TYPE>

to accept any value on specific field, put a wildcard * on it

USER.*.*.*.*
USER.ORCAS-CUPID.SUCCESS.a2a3d88f-e42e-4ce2-b134-707cd745e352.IDENTITY_PROVIDER_RETRIEVE_TOKEN_ERROR
USER.ORCAS-CUPID.SUCCESS.*.IDENTITY_PROVIDER_RETRIEVE_TOKEN_ERROR

ADMIN.*.*.*.*
ADMIN.ORCAS-CUPID.SUCCESS.AUTHORIZATION_RESOURCE_SERVER.CREATE
