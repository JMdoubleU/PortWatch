package portwatch.core.config;

import java.util.HashMap;

/**
 * The application's configuration. Holds one scanning config and many integration configs.
 */
public class AppConfig {
    private ScanConfig scanConfig;
    private HashMap<String, IntegrationConfig> integrations;

    /**
     * @param scanConfig scanning configuration
     * @param integrations integration configurations
     */
    public AppConfig(ScanConfig scanConfig, HashMap<String, IntegrationConfig> integrations) {
        this.scanConfig = scanConfig;
        this.integrations = integrations;
    }

    /**
     * @param scanConfig scanning configuration
     */
    public AppConfig(ScanConfig scanConfig) {
        this(scanConfig, new HashMap<String, IntegrationConfig>()); //no integrations, initialize as empty
    }

    public ScanConfig getScanConfig() {
        return this.scanConfig;
    }

    public HashMap<String, IntegrationConfig> getIntegrations() {
        return this.integrations;
    }
}