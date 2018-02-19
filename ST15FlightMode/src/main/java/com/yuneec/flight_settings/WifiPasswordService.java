package com.yuneec.flight_settings;

import android.util.Xml;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class WifiPasswordService {
    public static List<WifiPassword> getWifiPasswords(InputStream xml) throws Exception {
        List<WifiPassword> wifiPasswords = null;
        WifiPassword wifiPassword = null;
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(xml, "UTF-8");
        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
            switch (eventType) {
                case 0:
                    wifiPasswords = new ArrayList();
                    break;
                case 2:
                    if (!parser.getName().equals("wifipassword")) {
                        if (!parser.getName().equals("ssid")) {
                            if (!parser.getName().equals("password")) {
                                break;
                            }
                            eventType = parser.next();
                            wifiPassword.setPassword(parser.getText());
                            break;
                        }
                        eventType = parser.next();
                        wifiPassword.setSsid(parser.getText());
                        break;
                    }
                    wifiPassword = new WifiPassword();
                    break;
                case 3:
                    if (!parser.getName().equals("wifipassword")) {
                        break;
                    }
                    wifiPasswords.add(wifiPassword);
                    wifiPassword = null;
                    break;
                default:
                    break;
            }
        }
        return wifiPasswords;
    }

    public static void save(List<WifiPassword> wifiPasswords, OutputStream out) throws Exception {
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(out, "UTF-8");
        serializer.startDocument("UTF-8", Boolean.valueOf(true));
        serializer.startTag(null, "wifipasswords");
        for (WifiPassword wifiPassword : wifiPasswords) {
            serializer.startTag(null, "wifipassword");
            serializer.startTag(null, "ssid");
            serializer.text(wifiPassword.getSsid());
            serializer.endTag(null, "ssid");
            serializer.startTag(null, "password");
            serializer.text(wifiPassword.getPassword());
            serializer.endTag(null, "password");
            serializer.endTag(null, "wifipassword");
        }
        serializer.endTag(null, "wifipasswords");
        serializer.endDocument();
        out.flush();
        out.close();
    }
}
