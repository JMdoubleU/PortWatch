package portwatch.core.watch.model;

/**
 * Port update, contains new and old status objects for comparison.
 */
public class PortUpdate {

    private int portNumber;
    private PortStatus oldStatus;
    private PortStatus newStatus;

    /**
     * @param portNumber port number the update was observed on
     * @param oldStatus old port status
     * @param newStatus new port status
     */
    public PortUpdate(int portNumber, PortStatus oldStatus, PortStatus newStatus) {
        this.portNumber = portNumber;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public int getPort() {
        return this.portNumber;
    }

    /**
     * Retrieve a service string from statuses if possible
     * @return service string if found, "?" otherwise
     */
    public String getService() {
        if (!this.newStatus.getService().equals("?")) {
            return this.newStatus.getService();
        } else if (!this.oldStatus.getService().equals("?")) {
            return this.oldStatus.getService();
        } else {
            return "?";
        }
    }

    public PortStatus getOldStatus() {
        return this.oldStatus;
    }

    public PortStatus getNewStatus() {
        return this.newStatus;
    }

    @Override
    public String toString() {
        if (oldStatus == null) { //initial updates
            return String.format("%d (%s) %s", this.portNumber, this.getService(), this.newStatus.getState());
        } else {
            return String.format("%d (%s) %s -> %s", this.portNumber, this.getService(), this.oldStatus.getState(), this.newStatus.getState());
        }
    }
}