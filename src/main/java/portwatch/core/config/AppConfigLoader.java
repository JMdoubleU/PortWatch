package portwatch.core.config;

import com.google.gson.*;
import portwatch.core.config.profile.host.HostProfile;
import portwatch.core.config.profile.port.PortListProfile;
import portwatch.core.config.profile.port.PortProfile;
import portwatch.core.config.profile.port.PortRangeProfile;
import portwatch.core.logging.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Application configuration loader. Responsible for parsing JSON-formatted configurations.
 */
public class AppConfigLoader {

    /**
     * Read and parse a JSON-formatted application configuration
     * @param configPath path of the config to parse
     * @return AppConfig object containing objects parsed from the config
     */
    public static AppConfig load(String configPath) {
        Logger.logDebug("Loading config");
        try {
            JsonElement json = new JsonParser().parse(new FileReader(configPath));
            //retrieve root object
            JsonObject configObject = json.getAsJsonObject().get("config").getAsJsonObject();

            //parse sub-configs
            ScanConfig scanConfig = parseScanConfig(configObject.get("scan").getAsJsonObject());
            if (configObject.get("integrations") == null) { //integrations config is optional
                return new AppConfig(scanConfig);
            } else {
                HashMap<String, IntegrationConfig> integrations = parseIntegrationsConfig(configObject.get("integrations").getAsJsonArray());
                return new AppConfig(scanConfig, integrations);
            }
        } catch (Exception e) { //too many different exceptions to catch, so catch 'em all
            Logger.logError(e.getMessage(), AppConfigLoader.class);
            System.exit(1);
            return null;
        }
    }


    /**
     * Parse full integrations config
     * @param config JsonArray of individual integration configs
     * @return map of integration types to parsed IntegrationConfig objects
     */
    private static HashMap<String, IntegrationConfig> parseIntegrationsConfig(JsonArray config) {
        HashMap<String, IntegrationConfig> integrations = new HashMap<String, IntegrationConfig>();

        for (JsonElement integrationElement : config) {
            IntegrationConfig integrationConfig = new IntegrationConfig();

            JsonObject integrationObject = integrationElement.getAsJsonObject();
            String type = integrationObject.get("type").getAsString();
            for (Map.Entry<String, JsonElement> entry : integrationObject.entrySet()) {
                if (!entry.getKey().equals("type")) { //type is only used for the data structure
                    integrationConfig.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
            integrations.put(type, integrationConfig);
        }
        return integrations;
    }


    /**
     * Parse scan config into ScanConfig object
     * @param config scanning config JsonObject
     * @return parsed ScanConfig object
     * @throws ConfigParseException
     */
    private static ScanConfig parseScanConfig(JsonObject config) throws ConfigParseException {
        //get top-level variables
        String scanTypeStr = config.get("type").getAsString();
        ScanConfig.ScanType scanType = null;
        if (scanTypeStr.equals("stealth")) {
            scanType = ScanConfig.ScanType.STEALTH;
        } else if (scanTypeStr.equals("version")) {
            scanType = ScanConfig.ScanType.VERSION;
        } else {
            throw new ConfigParseException("Invalid scan type: " + scanTypeStr);
        }

        String nmapPath = config.get("nmapPath").getAsString();
        int maxThreads = config.get("maxThreads").getAsInt();
        int waitSeconds = config.get("waitSeconds").getAsInt();

        //parse host list into HostProfiles
        ArrayList<HostProfile> hostProfiles = new ArrayList<HostProfile>();
        JsonArray hosts = config.get("hosts").getAsJsonArray();
        for (JsonElement hostElement : hosts) {
            JsonObject hostObject = hostElement.getAsJsonObject();
            String host = hostObject.get("host").getAsString();

            JsonObject ports = hostObject.getAsJsonObject("ports");
            String portProfileType = ports.get("type").getAsString();
            PortProfile portProfile;
            if (portProfileType.equals("range")) {
                int lower = ports.get("lower").getAsInt();
                int upper = ports.get("upper").getAsInt();

                portProfile = new PortRangeProfile(lower, upper);
            } else if (portProfileType.equals("list")) {
                ArrayList<Integer> portList = new ArrayList<Integer>();
                JsonArray portListArray = ports.get("list").getAsJsonArray();
                for (JsonElement portElement : portListArray) {
                    portList.add(portElement.getAsInt());
                }
                portProfile = new PortListProfile(portList);
            } else {
                throw new ConfigParseException("Invalid port profile type: " + portProfileType);
            }

            hostProfiles.add(new HostProfile(host, portProfile));
        }
        return new ScanConfig(nmapPath, scanType, hostProfiles, maxThreads, waitSeconds);
    }
}