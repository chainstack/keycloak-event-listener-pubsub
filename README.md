# keycloak-event-listener-google-cloud-pubsub

Configuration

Environment variables
GOOGLE_APPLICATION_CREDENTIALS=/path/to/google/service_account_key_file
SN_KC_PUBSUB_TOPIC_ID=topic_id
SN_KC_PUBSUB_USER_EVENT_TYPES=REGISTER,DELETE_ACCOUNT,UPDATE_EMAIL
SN_KC_PUBSUB_ADMIN_OPERATION_TYPES=CREATE,DELETE,UPDATE


Event representation published to pub/sub 

Message attributes
format = JSON_API_V1
event =
    USER_<REALM_ID>_<RESULT = SUCCESS | ERROR>_<CLIENT_ID>_<EVENT_TYPE>
    ADMIN_<REALM_ID>_<RESULT = SUCCESS | ERROR>_<RESOURCE_TYPE>_<OPERATION>
