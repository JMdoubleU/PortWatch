package portwatch.core.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    enum MessageType { NORMAL, ERROR }
    public enum Verbosity { NORMAL, DEBUG }

    public static File logFile;
    public static Verbosity logVerbosity = Verbosity.NORMAL;

    public static synchronized void log(String message, Verbosity verbosity, MessageType type, Class context) {
        if (logVerbosity == Verbosity.NORMAL && verbosity == Verbosity.DEBUG) {
            return;
        }

        String logMessage = null;
        if (type == MessageType.NORMAL) {
            logMessage = String.format("[%s] %s", getDateString(), message);
        } else if (type == MessageType.ERROR) {
            logMessage = String.format("[%s] %s ERROR: %s", getDateString(), context.getSimpleName(), message);
        }

        if (logFile == null) {
            System.out.println(logMessage);
        } else {
            logToFile(logMessage);
        }
    }

    public static void logNormal(String message) {
        log(message, Verbosity.NORMAL, MessageType.NORMAL, null);
    }

    public static void logError(String message, Class context) {
        log(message, Verbosity.NORMAL, MessageType.ERROR, context);
    }

    public static void logDebug(String message) {
        log(message, Verbosity.DEBUG, MessageType.NORMAL, null);
    }

    private static String getDateString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private static void logToFile(String logMessage) {
        PrintWriter fileOut = null;
        try {
            fileOut = new PrintWriter(logFile);
            fileOut.append(logMessage + "\n");
        } catch (FileNotFoundException e) {
            //revert to console logging
            Logger.logFile = null;
            logError(e.getMessage(), Logger.class);
        } finally {
            if (fileOut != null) {
                fileOut.close();
            }
        }
    }
}
