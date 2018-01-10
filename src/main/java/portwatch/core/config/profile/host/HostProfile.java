package portwatch.core.config.profile.host;

import portwatch.core.config.profile.port.PortProfile;

/**
 * Host profile for scanning. Contains hostname and port profile.
 */
public class HostProfile {

    private String host;
    private PortProfile portProfile;

    /**
     * @param host hostname
     * @param portProfile associated port profile
     */
    public HostProfile(String host, PortProfile portProfile) {
        this.host = host;
        this.portProfile = portProfile;
    }

    public String getHost() {
        return this.host;
    }

    public PortProfile getPortProfile() {
        return this.portProfile;
    }
}