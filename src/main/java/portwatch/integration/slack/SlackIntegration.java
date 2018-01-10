package portwatch.integration.slack;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;

import portwatch.core.config.IntegrationConfig;
import portwatch.core.logging.Logger;
import portwatch.core.watch.model.HostUpdate;
import portwatch.core.watch.model.PortUpdate;
import portwatch.integration.Integration;

import java.io.IOException;
import java.util.Observable;

/**
 * Slack integration. Sends messages to a specified slack channel when updates are observed.
 */
public class SlackIntegration extends Integration {

    private SlackSession session; //current session
    private SlackChannel channel; //channel messages are sent to

    private boolean connectionFailed = false; //true if a slack session could not be created

    /**
     * @param integrationConfig integration configuration
     */
    public SlackIntegration(IntegrationConfig integrationConfig) {
        super(integrationConfig);
        setup();
    }

    /**
     * Attempt initialization of integration
     */
    private void setup() {
        try {
            this.session = SlackSessionFactory.createWebSocketSlackSession(integrationConfig.get("apiKey"));
            this.session.connect();
            this.channel = session.findChannelByName(integrationConfig.get("channel"));
        } catch (IOException e) {
            Logger.logError(e.getMessage(), getClass());
            this.connectionFailed = true;
        }
    }

    /**
     * @param update update object to format
     * @return formatted slack message string for update
     */
    private String formatUpdate(HostUpdate update) {
        StringBuilder updateStr = new StringBuilder();
        if (update.getType() == HostUpdate.UpdateType.INITIAL || update.getType() == HostUpdate.UpdateType.UP) {
            if (update.getType() == HostUpdate.UpdateType.INITIAL) {
                updateStr.append(String.format("Host *%s* initial report:\n", update.getHost()));
            } else if (update.getType() == HostUpdate.UpdateType.UP) {
                updateStr.append(String.format("Host *%s* is now up, initial report:\n", update.getHost()));
            }
            if (update.getPortUpdates().size() == 0) {
                updateStr.append(">No open ports.");
            } else {
                for (PortUpdate portUpdate : update.getPortUpdates()) {
                    updateStr.append(String.format(">*%d* (%s) *%s*\n", portUpdate.getPort(), portUpdate.getService(),
                            portUpdate.getNewStatus().getState()));
                }
            }
        } else if (update.getType() == HostUpdate.UpdateType.UPDATE) {
            updateStr.append(String.format("Host *%s* update:\n", update.getHost()));
            for (PortUpdate portUpdate : update.getPortUpdates()) {
                updateStr.append(String.format(">*%d* (%s) %s -> *%s*\n", portUpdate.getPort(), portUpdate.getService(),
                        portUpdate.getOldStatus().getState(), portUpdate.getNewStatus().getState()));
            }
        } else if (update.getType() == HostUpdate.UpdateType.DOWN) {
            updateStr.append(String.format("Host *%s* is down.\n", update.getHost()));
        }
        return updateStr.toString();
    }

    @Override
    public void update(Observable observed, Object object) {
        if (connectionFailed) {
            //integration connection failed, no need to continue observing
            observed.deleteObserver(this);
            return;
        }

        if (object instanceof HostUpdate) {
            //PortWatcher detected update
            Logger.logDebug("Sending slack message");
            session.sendMessage(channel, formatUpdate((HostUpdate) object));
        }
    }
}