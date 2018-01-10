package portwatch.core.watch.threading;

import portwatch.core.config.ScanConfig;
import portwatch.core.config.profile.host.HostProfile;
import portwatch.core.logging.Logger;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * Manages scan cycles, pool of scanning threads.
 */
public class ScanThreadManager extends Observable implements Observer, Runnable {

    private ScanConfig config;
    private Thread[] threads; //thread pool

    private ArrayList<HostProfile> hostPool; //pool of host profiles for a single scan cycle
    private int threadsCompleted = 0; //threads completed for a single scan cycle

    /**
     * @param config scanning configuration to handle
     */
    public ScanThreadManager(ScanConfig config) {
        this.config = config;
        this.threads = new Thread[this.config.getMaxThreads()];
    }

    /**
     * Run scan cycles
     */
    public void run() {
        Logger.logDebug("Beginning scan cycles");
        while (!Thread.interrupted()) {
            try {
                runScans();

                Thread.sleep(this.config.getWaitSeconds() * 1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * Stop all running scan threads
     */
    public void stop() {
        Logger.logDebug("Stopping scan cycles");
        for (int i = 0; i < this.threads.length; i++) {
            if (this.threads[i] != null) {
                this.threads[i].interrupt();
            }
        }
        Logger.logDebug("Scan cycles stopped");
    }

    /**
     * Run a single scan cycle - scan each host profile once
     * @throws InterruptedException
     */
    private void runScans() throws InterruptedException {
        Logger.logDebug("Beginning scan cycle");
        //reset single-cycle variables
        this.threadsCompleted = 0;
        this.hostPool = new ArrayList<HostProfile>(this.config.getHostProfiles());

        //fill thread pool
        for (int i = 0; i < this.threads.length; i++) {
            if (!this.hostPool.isEmpty()) {
                createThread(i);
            } else {
                break;
            }
        }

        //wait until this cycle is completed to begin next
        while (this.threadsCompleted != this.config.getHostProfiles().size()) {
            Thread.sleep(500);
        }
        Logger.logDebug("Scan cycle completed");
    }

    /**
     * Remove thread from thread pool, stop observing it
     * @param thread thread to remove and stop observing
     */
    private void removeThread(ScanThread thread) {
        this.threads[thread.getId()] = null;
        thread.deleteObservers();
    }

    /**
     * Create and start new thread with next host profile in host pool
     * Removes selected host profile from host pool
     * @param id index in thread pool to insert new thread
     */
    private synchronized void createThread(int id) {
        ScanThread scanThread = new ScanThread(id, this.hostPool.remove(0), this.config);
        scanThread.addObserver(this);
        this.threads[id] = new Thread(scanThread);
        this.threads[id].start();
    }


    @Override
    public synchronized void update(Observable observed, Object object) {
        if (observed instanceof ScanThread) {
            //thread completed scan
            ScanThread thread = (ScanThread) observed;

            //remove from pool, replace if any host profiles remain
            removeThread(thread);
            if (!this.hostPool.isEmpty()) {
                createThread(thread.getId());
            }

            this.threadsCompleted++;

            //notify PortWatcher of scan completion
            this.setChanged();
            this.notifyObservers(thread);
        }
    }
}