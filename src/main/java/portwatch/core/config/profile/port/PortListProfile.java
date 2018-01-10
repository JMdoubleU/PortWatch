package portwatch.core.config.profile.port;

import java.util.ArrayList;

/**
 * Port profile for specifying a list of ports.
 */
public class PortListProfile implements PortProfile {

    private ArrayList<Integer> ports;

    /**
     * @param ports list of ports
     */
    public PortListProfile(ArrayList<Integer> ports) {
        this.ports = ports;
    }

    @Override
    public String toString() {
        String result = "";
        for (int i = 0; i < ports.size(); i++) {
            if (i < ports.size() - 1) {
                result += ports.get(i) + ",";
            } else {
                result += ports.get(i);
            }
        }
        return result;
    }
}