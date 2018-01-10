package portwatch.core.watch.threading;

import org.nmap4j.Nmap4j;
import org.nmap4j.core.nmap.NMapExecutionException;
import org.nmap4j.core.nmap.NMapInitializationException;
import org.nmap4j.data.NMapRun;

import org.nmap4j.parser.NMapXmlHandler;
import org.nmap4j.parser.OnePassParser;
import portwatch.core.config.ScanConfig;
import portwatch.core.config.profile.host.HostProfile;
import portwatch.core.logging.Logger;

import java.util.Observable;

/**
 * Thread for scanning a single host profile
 */
public class ScanThread extends Observable implements Runnable {

    private int id;
    private HostProfile hostProfile;
    private ScanConfig config;

    private NMapRun result; //scan result

    /**
     * @param id index in thread pool
     * @param hostProfile host profile to scan
     * @param config scanning configuration
     */
    public ScanThread(int id, HostProfile hostProfile, ScanConfig config) {
        this.id = id;
        this.hostProfile = hostProfile;
        this.config = config;
    }

    /**
     * Run host profile scan
     */
    public void run() {
        Logger.logDebug(String.format("[%d] Scanning %s", this.id, this.hostProfile.getHost()));

        //initialize nmap with scan settings
        Nmap4j nmap = new Nmap4j(this.config.getNmapPath());
        nmap.includeHosts(this.hostProfile.getHost());

        //add flags based on configuration
        if (this.config.getScanType() == ScanConfig.ScanType.VERSION) {
            nmap.addFlags(String.format("-sV -p%s --min-rate 10000", this.hostProfile.getPortProfile().toString()));
        } else if (this.config.getScanType() == ScanConfig.ScanType.STEALTH) {
            nmap.addFlags(String.format("-sS -p%s --min-rate 10000", this.hostProfile.getPortProfile().toString()));
        }

        //attempt execution of scan and retrieval of results
        try {
            nmap.execute();
            if (nmap.getResult() != null) {
                this.result = nmap.getResult();
            } else {
                /*
                Occasionally nmap4j will return a null result when one of the hosts being scanned is down (bug).
                When this happens, the raw output can be retrieved and fed through nmap4j's parser to retrieve the result.
                 */
                OnePassParser parser = new OnePassParser();
                this.result = parser.parse(nmap.getExecutionResults().getOutput(), OnePassParser.STRING_INPUT);
            }

            Logger.logDebug(String.format("[%d] Scan of %s completed", this.id, this.hostProfile.getHost()));

            //scan completed, notify thread manager
            this.setChanged();
            this.notifyObservers();
        } catch (NMapExecutionException | NMapInitializationException e) {
            Logger.logError(e.getMessage(), getClass());
        }
    }

    public int getId() {
        return this.id;
    }

    public HostProfile getHostProfile() {
        return this.hostProfile;
    }

    public NMapRun getResult() {
        return this.result;
    }
}
