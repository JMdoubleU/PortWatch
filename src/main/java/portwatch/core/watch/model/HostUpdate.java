package portwatch.core.watch.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Host update, contains all new port updates that have occurred for the host.
 */
public class HostUpdate {

    public enum UpdateType { INITIAL, UPDATE, DOWN, UP }

    private UpdateType type;
    private String host;
    private ArrayList<PortUpdate> portUpdates;

    /**
     * @param type update type
     * @param host host that the update was observed on
     * @param portUpdates port updates that occurred on host
     */
    public HostUpdate(UpdateType type, String host, ArrayList<PortUpdate> portUpdates) {
        this.type = type;
        this.host = host;
        this.portUpdates = portUpdates;
        //sort updates by port number, ascending
        Collections.sort(this.portUpdates, new Comparator<PortUpdate>() {
            @Override
            public int compare(PortUpdate o1, PortUpdate o2) {
                return o1.getPort() - o2.getPort();
            }
        });
    }

    /**
     * Initialize update with no port updates.
     * Used for creating DOWN updates.
     * @param type
     * @param host
     */
    public HostUpdate(UpdateType type, String host) {
        this(type, host, new ArrayList<PortUpdate>());
    }

    public UpdateType getType() {
        return this.type;
    }

    public String getHost() {
        return this.host;
    }

    public ArrayList<PortUpdate> getPortUpdates() {
        return this.portUpdates;
    }

    @Override
    public String toString() {
        String result = "";
        if (this.type == UpdateType.INITIAL) {
            if (portUpdates.size() > 0) {
                for (PortUpdate portUpdate : portUpdates) {
                    result += String.format("%s initial: %s\n", this.host, portUpdate.toString());
                }
            } else {
                result = String.format("%s initial: no ports open\n", this.host);
            }
        } else if (this.type == UpdateType.UPDATE) {
            for (PortUpdate portUpdate : portUpdates) {
                result += String.format("%s update: %s\n", this.host, portUpdate.toString());
            }
        } else if (this.type == UpdateType.DOWN) {
            result = String.format("%s: host down\n", this.host);
        } else if (this.type == UpdateType.UP) {
            if (portUpdates.size() > 0) {
                for (PortUpdate portUpdate : portUpdates) {
                    result += String.format("%s is now up, initial: %s\n", this.host, portUpdate.toString());
                }
            } else {
                result = String.format("%s now up, initial: no ports open\n", this.host);
            }
        }
        return result.substring(0, result.length() - 1); //remove trailing newline
    }
}