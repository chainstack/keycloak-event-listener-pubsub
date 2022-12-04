package com.softrizon.keycloak.providers.events.google.cloud.pubsub;

import com.google.cloud.pubsub.v1.Publisher;
import com.softrizon.keycloak.providers.events.google.cloud.pubsub.config.PubSubConfig;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.softrizon.keycloak.providers.events.google.cloud.pubsub.config.PubSubConfig.PLUGIN_NAME;

public class PubSubEventListenerProviderFactory implements EventListenerProviderFactory {

    private static final Logger logger = Logger.getLogger(PubSubEventListenerProviderFactory.class);

    private PubSubConfig config;
    private Publisher publisher;

    @Override
    public synchronized EventListenerProvider create(KeycloakSession keycloakSession) {
        try {
            if (publisher == null)
                publisher = Publisher.newBuilder(config.getTopicId()).build();
        } catch (IOException exception) {
            logger.warnf(exception, "%s: failed to create Pub/Sub publisher.", PLUGIN_NAME);
            return null;
        }

        return new PubSubEventListenerProvider(publisher, keycloakSession, config);
    }

    @Override
    public void init(Config.Scope scope) {
        config = PubSubConfig.create(scope);
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
        // Intentionally left blank
    }

    @Override
    public void close() {
        try {
            if (publisher != null) {
                publisher.shutdown();
                publisher.awaitTermination(1, TimeUnit.MINUTES);
            }
        } catch (InterruptedException exception) {
            logger.debugf(exception, "%s: awaiting pub/sub publisher termination interrupted.", PLUGIN_NAME);
        }
    }

    @Override
    public String getId() {
        return PLUGIN_NAME;
    }
}
