package portwatch.core.watch;

import org.nmap4j.data.NMapRun;
import org.nmap4j.data.host.ports.Port;

import portwatch.core.config.ScanConfig;
import portwatch.core.config.profile.host.HostProfile;
import portwatch.core.logging.Logger;
import portwatch.core.watch.model.HostUpdate;
import portwatch.core.watch.model.PortStatus;
import portwatch.core.watch.model.PortUpdate;
import portwatch.core.watch.threading.ScanThread;
import portwatch.core.watch.threading.ScanThreadManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

/**
 * Handles invocation of scanning, update detection
 */
public class PortWatcher extends Observable implements Observer, Runnable {

    private ScanConfig config;

    private HashMap<String, ArrayList<NMapRun>> scanHistory;

    private ScanThreadManager threadManager;
    private Thread threadManagerThread;

    /**
     * @param config scanning configuration
     */
    public PortWatcher(ScanConfig config) {
        this.config = config;

        this.scanHistory = new HashMap<String, ArrayList<NMapRun>>();
        for (HostProfile hostProfile : config.getHostProfiles()) {
            this.scanHistory.put(hostProfile.getHost(), new ArrayList<NMapRun>());
        }

        this.threadManager = new ScanThreadManager(this.config);
        this.threadManager.addObserver(this);
    }

    /**
     * Begin scanning
     */
    public void run() {
        Logger.logDebug("Starting port watching");
        this.threadManagerThread = new Thread(this.threadManager);
        this.threadManagerThread.start();
    }

    /**
     * Stop all scanning
     */
    public void stop() {
        Logger.logDebug("Stoppiong port watching");
        threadManager.stop();
        threadManagerThread.interrupt();
    }

    /**
     * Convert Port objects to PortStatus object data structure
     * @param ports Port objects to convert
     * @return map of port numbers to PortStatus objects
     */
    private HashMap<Integer, PortStatus> convertPorts(ArrayList<Port> ports) {
        HashMap<Integer, PortStatus> newPorts = new HashMap<Integer, PortStatus>();
        for (Port port : ports) {
            PortStatus portStatus = new PortStatus(port);
            newPorts.put((int) port.getPortId(), portStatus);
        }
        return newPorts;
    }

    /**
     * Create update with host profile's initial statuses
     * @param hostProfile host profile to create update for
     * @param initialStatuses initial scan result
     */
    private void createInitialStatus(HostProfile hostProfile, HashMap<Integer, PortStatus> initialStatuses) {
        ArrayList<PortUpdate> updates = new ArrayList<PortUpdate>();

        for (int port : initialStatuses.keySet()) {
            updates.add(new PortUpdate(port, null, initialStatuses.get(port)));
        }

        sendUpdate(new HostUpdate(HostUpdate.UpdateType.INITIAL, hostProfile.getHost(), updates));
    }

    /**
     * Create update with host profile's initial status for this uptime period
     * @param hostProfile host profile to create update for
     * @param initialStatuses initial uptime scan result
     */
    private void createUpStatus(HostProfile hostProfile, HashMap<Integer, PortStatus> initialStatuses) {
        ArrayList<PortUpdate> updates = new ArrayList<PortUpdate>();

        for (int port : initialStatuses.keySet()) {
            updates.add(new PortUpdate(port, null, initialStatuses.get(port)));
        }

        sendUpdate(new HostUpdate(HostUpdate.UpdateType.UP, hostProfile.getHost(), updates));
    }

    /**
     * Compare scan results, determine if any port statuses have changed
     * @param hostProfile host profile being checked
     * @param oldStatuses old scan result for host profile
     * @param newStatuses new scan result for host profile
     */
    private void compareStatuses(HostProfile hostProfile, HashMap<Integer, PortStatus> oldStatuses, HashMap<Integer, PortStatus> newStatuses) {
        ArrayList<PortUpdate> updates = new ArrayList<PortUpdate>();

        //check port statuses returned by old scan
        for (int port : oldStatuses.keySet()) {
            if (newStatuses.containsKey(port)) {
                if (!oldStatuses.get(port).equals(newStatuses.get(port))) {
                    updates.add(new PortUpdate(port, oldStatuses.get(port), newStatuses.get(port)));
                }
            } else {
                //port closed
                updates.add(new PortUpdate(port, oldStatuses.get(port), new PortStatus("closed", null)));
            }
        }
        //check port statuses returned by new scan
        for (int port : newStatuses.keySet()) {
            if (!oldStatuses.keySet().contains(port)) {
                //port opened
                updates.add(new PortUpdate(port, new PortStatus("closed", null), newStatuses.get(port)));
            }
        }

        if (updates.size() > 0) {
            HostUpdate hostUpdate = new HostUpdate(HostUpdate.UpdateType.UPDATE, hostProfile.getHost(), updates);
            sendUpdate(hostUpdate);
        }
    }

    /**
     * Notify integrations of host update
     * @param update update to notify with
     */
    public void sendUpdate(HostUpdate update) {
        this.setChanged();
        this.notifyObservers(update);
    }

    @Override
    public void update(Observable observed, Object object) {
        if (object instanceof ScanThread) {
            //thread finished scan
            ScanThread thread = (ScanThread) object;

            //add result to host profile's scan history
            this.scanHistory.get(thread.getHostProfile().getHost()).add(thread.getResult());

            boolean hostDown = thread.getResult().getHosts().size() == 0;

            ArrayList<NMapRun> hostRuns = this.scanHistory.get(thread.getHostProfile().getHost());
            if (hostRuns.size() > 1) { //another result for this host profile exists to compare this one to
                if (hostDown) {
                    if (hostRuns.get(hostRuns.size() - 2).getHosts().size() > 0) {
                        //only create host down update if one hasn't been issued already for this downtime period
                        sendUpdate(new HostUpdate(HostUpdate.UpdateType.DOWN, thread.getHostProfile().getHost()));
                    }
                } else {
                    if (hostRuns.get(hostRuns.size() - 2).getHosts().size() == 0) {
                        //host previously down, results are initial status for this uptime period
                        createUpStatus(thread.getHostProfile(), convertPorts(hostRuns.get(hostRuns.size() - 1).getHosts().get(0).getPorts().getPorts()));
                    } else {
                        HashMap<Integer, PortStatus> oldStatuses = convertPorts(hostRuns.get(hostRuns.size() - 2).getHosts().get(0).getPorts().getPorts());
                        HashMap<Integer, PortStatus> newStatuses = convertPorts(hostRuns.get(hostRuns.size() - 1).getHosts().get(0).getPorts().getPorts());
                        compareStatuses(thread.getHostProfile(), oldStatuses, newStatuses);
                    }
                }
            } else { //first result for this host profile
                if (hostDown) {
                    sendUpdate(new HostUpdate(HostUpdate.UpdateType.DOWN, thread.getHostProfile().getHost()));
                } else {
                    createInitialStatus(thread.getHostProfile(), convertPorts(hostRuns.get(hostRuns.size() - 1).getHosts().get(0).getPorts().getPorts()));
                }
            }
        }
    }
}