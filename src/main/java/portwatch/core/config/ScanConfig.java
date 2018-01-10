package portwatch.core.config;

import portwatch.core.config.profile.host.HostProfile;

import java.util.ArrayList;

/**
 * Scanning configuration.
 */
public class ScanConfig {

    public enum ScanType { STEALTH, VERSION }

    private String nmapPath;
    private ScanType scanType;
    private ArrayList<HostProfile> hostProfiles;
    private int maxThreads;
    private int waitSeconds;

    /**
     * @param nmapPath path of directory containing nmap binary
     * @param scanType nmap scan type (stealth or version)
     * @param hostProfiles list of host profiles to scan
     * @param maxThreads maximum number of threads to use for scanning
     * @param waitSeconds seconds to wait between scan cycles
     */
    public ScanConfig(String nmapPath, ScanType scanType, ArrayList<HostProfile> hostProfiles, int maxThreads, int waitSeconds) {
        this.nmapPath = nmapPath;
        this.scanType = scanType;
        this.hostProfiles = hostProfiles;
        this.maxThreads = maxThreads;
        this.waitSeconds = waitSeconds;
    }

    public String getNmapPath() {
        return this.nmapPath;
    }

    public ScanType getScanType() {
        return this.scanType;
    }

    public ArrayList<HostProfile> getHostProfiles() {
        return this.hostProfiles;
    }

    public int getWaitSeconds() {
        return this.waitSeconds;
    }

    public int getMaxThreads() {
        return this.maxThreads;
    }
}