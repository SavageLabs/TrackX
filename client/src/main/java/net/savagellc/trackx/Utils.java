package net.savagellc.trackx;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

class Utils {

    static String throwableToString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return  sw.toString();
    }
    static String encodeParam(String key,String value) throws UnsupportedEncodingException {
        return( URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8"));
    }
}
