package portwatch;

import org.apache.commons.cli.*;

import portwatch.core.config.*;
import portwatch.core.logging.Logger;
import portwatch.core.watch.PortWatcher;
import portwatch.core.watch.model.HostUpdate;
import portwatch.integration.Integration;
import portwatch.integration.slack.SlackIntegration;

import java.io.File;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

/**
 * System invoker. Parses program arguments and invokes subsystems accordingly.
 */
public class PortWatch implements Observer {
    public static void main(String[] args) {
        new PortWatch(args);
    }

    /**
     * Class constructor. Invokes argument parsing.
     * @param args arguments to parse
     */
    public PortWatch(String[] args) {
        parseArgs(args);
    }

    /**
     * Parses arguments, ensures required arguments given.
     * If required arguments are given, invokes handling of arguments.
     * If required arguments not given, error printed and execution stopped.
     * @param args arguments to parse
     */
    private void parseArgs(String[] args) {
        Options options = createOptions();

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        try {
            handleInput(parser.parse(options, args));
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            formatter.printHelp(getClass().getSimpleName(), options);
            System.exit(1);
        }
    }

    /**
     * Create command-line options for argument parsing
     * @return command-line options object
     */
    private Options createOptions() {
        Options options = new Options();

        Option optConfig = new Option("c", "config", true, "config path - required for 'cli' mode");
        optConfig.setRequired(true);
        options.addOption(optConfig);

        Option optLog = new Option("l", "log", true, "log path");
        optLog.setRequired(false);
        options.addOption(optLog);

        Option optDebug = new Option("d", "debug", false, "enable debug message logging");
        optDebug.setRequired(false);
        options.addOption(optDebug);

        return options;
    }

    /**
     * Handle command-line input, invoke subsystems accordingly.
     * @param cmd command-line input object
     */
    private void handleInput(CommandLine cmd) {
        if (cmd.hasOption("log")) {
            Logger.logFile = new File(cmd.getOptionValue("log"));
        }
        if (cmd.hasOption("debug")) {
            Logger.logVerbosity = Logger.Verbosity.DEBUG;
        }

        AppConfig appConfig = AppConfigLoader.load(cmd.getOptionValue("config"));

        //create scanning subsystem
        PortWatcher portWatcher = new PortWatcher(appConfig.getScanConfig());

        //create integration subsystems
        Logger.logDebug("Loading integrations");
        HashMap<String, IntegrationConfig> integrations = appConfig.getIntegrations();
        for (String key : integrations.keySet()) {
            Integration integration;
            if (key.equals("slack")) {
                integration = new SlackIntegration(integrations.get(key));
            } else {
                Logger.logError("Unknown integration: " + key, getClass());
                continue;
            }
            portWatcher.addObserver(integration);
        }

        portWatcher.addObserver(this);
        new Thread(portWatcher).start(); //begin scanning
    }

    @Override
    public void update(Observable observed, Object object) {
        if (object instanceof HostUpdate) {
            //PortWatcher observed host update

            String[] logMessages = object.toString().split("\n");
            for (int i = 0; i < logMessages.length; i++) {
                Logger.logNormal(logMessages[i]);
            }
        }
    }
}