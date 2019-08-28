package net.savagellc.trackx;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.bukkit.Bukkit;

public class TrackX {

    private static HashMap<String, TrackX> created = new HashMap<>();
    private Vector<String> reportedTraces = new Vector<>();
    private String pluginId;
    private String version;
    private String includes;
    private static boolean reporting = false;
    private static boolean failed = false;

    private TrackX(String pluginId, String version, String inludes) {
        this.pluginId = pluginId;
        this.version = version;
        this.includes = inludes;
    }

    static void handle(String asString) {
        for (String key : created.keySet()) {
            if (asString.contains(created.get(key).includes)) {
                new Thread(() -> {
                    created.get(key).handleException(asString);
                }).start();
                break;
            }
        }
    }

    static {
        try {
            Handler handler = new Handler() {

                @Override
                public void publish(LogRecord record) {
                    if (record.getThrown() != null) {
                        String handleError = record.getMessage() + "\n" + Utils.throwableToString(record.getThrown());
                        handle(handleError);
                    }
                }

                @Override
                public void flush() {
                }

                @Override
                public void close() throws SecurityException {
                }
            };
            Bukkit.getLogger().addHandler(handler);
        } catch (Exception e) {
            System.out.println("[TrackX] Failed to attach handler");
            failed = true;
        }
    }

    private void pushUpdate(String preparedMessage) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("https://savage-error-tracker.appspot.com/report")
                .openConnection();
        connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.getOutputStream().write(preparedMessage.getBytes());
        connection.getOutputStream().flush();
        int responseCode = connection.getResponseCode();
        System.out.println("[TrackX] Response code: " + responseCode);
        byte[] available = new byte[connection.getInputStream().available()];
        connection.getInputStream().read(available);
        System.out.println("[TrackX] Response Message: " + new String(available));
        connection.disconnect();
    }

    private void handleException(String s) {
        if (reporting)
            return;
        reporting = true;
        if (s.contains(includes)) {
            if (!reportedTraces.contains(s)) {
                System.out.println("[TrackX] Reporting issue...");
                reportedTraces.add(s);
                if (reportedTraces.size() > 30)
                    reportedTraces.remove(0);
                StringBuilder builder = new StringBuilder();
                try {
                    builder.append(Utils.encodeParam("pluginId", pluginId));
                } catch (UnsupportedEncodingException e) {
                    // should never happen
                }
                builder.append("&");
                try {
                    builder.append(Utils.encodeParam("version", version));
                } catch (UnsupportedEncodingException e) {
                    // should never happen
                }
                builder.append("&");
                try {
                    builder.append(Utils.encodeParam("trace", s));
                } catch (UnsupportedEncodingException e) {
                    // should never happen
                }
                try {
                    pushUpdate(builder.toString());
                    reporting = false;
                } catch (IOException e) {
                    // error during connection
                }
            }
        }
    }

    public static void startTracking(String pluginId, String version, String includes) {
        if (failed)
            return;
        if (created.containsKey(pluginId))
            created.remove(pluginId);
        System.out.println("[TrackX] Tracker starting for " + pluginId);
        TrackX instance = new TrackX(pluginId, version, includes);
        created.put(pluginId, instance);
    }
}
