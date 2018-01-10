package portwatch.integration;

import portwatch.core.config.IntegrationConfig;

import java.util.Observer;

/**
 * Abstract integration definition.
 */
public abstract class Integration implements Observer {
    protected IntegrationConfig integrationConfig;

    /**
     * @param integrationConfig integration's configuration
     */
    public Integration(IntegrationConfig integrationConfig) {
        this.integrationConfig = integrationConfig;
    }
}
