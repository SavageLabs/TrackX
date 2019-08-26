package net.savagellc.trackx;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;

public class TrackX {

    private static HashMap<String, TrackX> created = new HashMap<>();
    private Vector<String> reportedTraces = new Vector<>();
    private String pluginId;
    private String version;
    private String includes;
    private static boolean reporting = false;

    private TrackX(String pluginId, String version, String inludes) {
        this.pluginId = pluginId;
        this.version = version;
        this.includes = inludes;
    }

    static {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            new Thread(() -> {
                String asString = Utils.throwableToString(e);
                for (String key : created.keySet()) {
                    if (asString.contains(created.get(key).includes)) {
                        created.get(key).handleException(asString);
                        break;
                    }
                }
            }).start();
            e.printStackTrace();
        });
    }

    private void pushUpdate(String preparedMessage) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("https://savage-error-tracker.appspot.com/report").openConnection();
        connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.getOutputStream().write(preparedMessage.getBytes());
        connection.getOutputStream().flush();
        connection.getResponseCode();
        connection.disconnect();
    }

    private void handleException(String s) {
        if(reporting) return;
        reporting = true;
        if (s.contains(includes)) {
            if (!reportedTraces.contains(s)) {
                reportedTraces.add(s);
                if (reportedTraces.size() > 30) reportedTraces.remove(0);
                StringBuilder builder = new StringBuilder();
                try {
                    builder.append(Utils.encodeParam("pluginId", pluginId));
                } catch (UnsupportedEncodingException e) {
                    //should never happen
                }
                builder.append("&");
                try {
                    builder.append(Utils.encodeParam("version", version));
                } catch (UnsupportedEncodingException e) {
                    //should never happen
                }
                builder.append("&");
                try {
                    builder.append(Utils.encodeParam("trace", s));
                } catch (UnsupportedEncodingException e) {
                    //should never happen
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
        if (created.containsKey(pluginId)) return;
        TrackX instance = new TrackX(pluginId, version, includes);
        created.put(pluginId, instance);
    }
}
