# keycloak-event-listener-google-cloud-pubsub

Configuration

Environment variables
SN_KC_PUBSUB_SERVICE_ACCOUNT_CREDENTIALS_FILE_PATH=/path/to/google/service_account_key_file
SN_KC_PUBSUB_PROJECT_ID=<your_project_id>
SN_KC_PUBSUB_TOPIC_ID=<your_topic_id>
SN_KC_PUBSUB_USER_EVENT_PATTERNS=USER:*:*:*:*,USER:*:*:*:REGISTER,USER:*:*:*:UPDATE_EMAIL
SN_KC_PUBSUB_ADMIN_EVENT_PATTERNS=ADMIN:*:*:*:*


Event representation published to pub/sub 

## Message attributes
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
