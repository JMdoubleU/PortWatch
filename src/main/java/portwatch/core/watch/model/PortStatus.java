package portwatch.core.watch.model;

import org.nmap4j.data.host.ports.Port;
import org.nmap4j.data.nmaprun.host.ports.port.Service;

import java.util.ArrayList;

/**
 * Port status, contains port's state and associated service.
 */
public class PortStatus {

    private String state; //(open, filtered, closed)
    private String service; //identified service

    /**
     * Parse port status information from Port object
     * @param port Port to parse
     */
    public PortStatus(Port port) {
        this.state = port.getState().getState();
        this.service = parseService(port.getService());
    }

    /**
     * @param state port's state (open, filtered, closed)
     * @param service corresponding service for port
     */
    public PortStatus(String state, String service) {
        this.state = state;
        if (service == null) { service = "?"; } //prevent NPE when port state is closed
        this.service = service;
    }

    /**
     * Parse service information from Service object
     * @param service Service object to parse
     * @return formatted service string
     */
    private String parseService(Service service) {
        ArrayList<String> serviceDetails = new ArrayList<String>();
        if (service.getName() != null) {
            serviceDetails.add(service.getName());
        }
        if (service.getProduct() != null) {
            serviceDetails.add(service.getProduct());
        }
        if (service.getVersion() != null) {
            serviceDetails.add(service.getVersion());
        }
        return String.join(" ", serviceDetails);
    }

    public String getState() {
        return this.state;
    }

    public String getService() {
        return this.service;
    }

    @Override
    public String toString() {
        return this.state + "," + this.service;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof PortStatus) {
            PortStatus portStatus = (PortStatus) object;
            return portStatus.state.equals(this.state) && portStatus.service.equals(this.service);
        } else {
            return false;
        }
    }
}