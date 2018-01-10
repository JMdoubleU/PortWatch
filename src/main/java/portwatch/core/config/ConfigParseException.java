package portwatch.core.config;

/**
 * Exception to be thrown when an error is found while parsing the application configuration.
 */
public class ConfigParseException extends Exception {
    public ConfigParseException(String message) {
        super(message);
    }
}