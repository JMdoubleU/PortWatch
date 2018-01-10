package portwatch.core.config;

import java.util.HashMap;

/**
 * Individual integration configuration. Holds map of integration variables.
 */
public class IntegrationConfig {
    private HashMap<String, String> config; //map of integration variables

    public IntegrationConfig() {
        this.config = new HashMap<String, String>();
    }

    /**
     * Store key-value pair in config
     * @param key key to store
     * @param value value associated with key
     */
    public void put(String key, String value) {
        this.config.put(key, value);
    }

    /**
     * Retrieve value for given key
     * @param key key to retrieve value for
     * @return key's associated value
     */
    public String get(String key) {
        return this.config.get(key);
    }
}
