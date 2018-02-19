package com.yuneec.flight_settings;

import android.util.Xml;
import com.yuneec.IPCameraManager.Cameras;
import com.yuneec.database.DBOpenHelper;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class PullCameraParser implements CameraParser {
    public List<Cameras> parse(InputStream is) throws Exception {
        List<Cameras> cameras = null;
        Cameras camera = null;
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(is, "UTF-8");
        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
            switch (eventType) {
                case 0:
                    cameras = new ArrayList();
                    break;
                case 2:
                    if (!parser.getName().equals("camera")) {
                        if (!parser.getName().equals("id")) {
                            if (!parser.getName().equals(DBOpenHelper.KEY_NAME)) {
                                if (!parser.getName().equals("type")) {
                                    if (!parser.getName().equals("dr")) {
                                        if (!parser.getName().equals("f")) {
                                            if (!parser.getName().equals("n")) {
                                                if (!parser.getName().equals("t1")) {
                                                    if (!parser.getName().equals("t2")) {
                                                        if (!parser.getName().equals("intervalp")) {
                                                            if (!parser.getName().equals("codep")) {
                                                                if (!parser.getName().equals("intervalv")) {
                                                                    if (!parser.getName().equals("codev")) {
                                                                        break;
                                                                    }
                                                                    eventType = parser.next();
                                                                    camera.setCodev(parser.getText());
                                                                    break;
                                                                }
                                                                eventType = parser.next();
                                                                camera.setIntervalv(parser.getText());
                                                                break;
                                                            }
                                                            eventType = parser.next();
                                                            camera.setCodep(parser.getText());
                                                            break;
                                                        }
                                                        eventType = parser.next();
                                                        camera.setIntervalp(parser.getText());
                                                        break;
                                                    }
                                                    eventType = parser.next();
                                                    camera.setT2(parser.getText());
                                                    break;
                                                }
                                                eventType = parser.next();
                                                camera.setT1(parser.getText());
                                                break;
                                            }
                                            eventType = parser.next();
                                            camera.setN(parser.getText());
                                            break;
                                        }
                                        eventType = parser.next();
                                        camera.setF(parser.getText());
                                        break;
                                    }
                                    eventType = parser.next();
                                    camera.setDr(parser.getText());
                                    break;
                                }
                                eventType = parser.next();
                                camera.setType(parser.getText());
                                break;
                            }
                            eventType = parser.next();
                            camera.setName(parser.getText());
                            break;
                        }
                        eventType = parser.next();
                        camera.setId(Integer.parseInt(parser.getText()));
                        break;
                    }
                    camera = new Cameras();
                    break;
                case 3:
                    if (!parser.getName().equals("camera")) {
                        break;
                    }
                    cameras.add(camera);
                    camera = null;
                    break;
                default:
                    break;
            }
        }
        return cameras;
    }

    public String serialize(List<Cameras> cameras) throws Exception {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        serializer.setOutput(writer);
        serializer.startDocument("UTF-8", Boolean.valueOf(true));
        serializer.startTag("", "cameras");
        for (Cameras camera : cameras) {
            serializer.startTag("", "camera");
            serializer.attribute("", "id", new StringBuilder(String.valueOf(camera.getId())).toString());
            serializer.startTag("", DBOpenHelper.KEY_NAME);
            serializer.text(camera.getName());
            serializer.endTag("", DBOpenHelper.KEY_NAME);
            serializer.startTag("", "type");
            serializer.text(new StringBuilder(String.valueOf(camera.getType())).toString());
            serializer.endTag("", "type");
            serializer.startTag("", "dr");
            serializer.text(new StringBuilder(String.valueOf(camera.getDr())).toString());
            serializer.endTag("", "dr");
            serializer.startTag("", "f");
            serializer.text(new StringBuilder(String.valueOf(camera.getF())).toString());
            serializer.endTag("", "f");
            serializer.startTag("", "n");
            serializer.text(new StringBuilder(String.valueOf(camera.getN())).toString());
            serializer.endTag("", "n");
            serializer.startTag("", "t1");
            serializer.text(new StringBuilder(String.valueOf(camera.getT1())).toString());
            serializer.endTag("", "t1");
            serializer.startTag("", "t2");
            serializer.text(new StringBuilder(String.valueOf(camera.getT2())).toString());
            serializer.endTag("", "t2");
            serializer.startTag("", "intervalp");
            serializer.text(new StringBuilder(String.valueOf(camera.getIntervalp())).toString());
            serializer.endTag("", "intervalp");
            serializer.startTag("", "codep");
            serializer.text(new StringBuilder(String.valueOf(camera.getCodep())).toString());
            serializer.endTag("", "codep");
            serializer.startTag("", "intervalv");
            serializer.text(new StringBuilder(String.valueOf(camera.getIntervalv())).toString());
            serializer.endTag("", "intervalv");
            serializer.startTag("", "codev");
            serializer.text(new StringBuilder(String.valueOf(camera.getCodev())).toString());
            serializer.endTag("", "codev");
            serializer.endTag("", "camera");
        }
        serializer.endTag("", "cameras");
        serializer.endDocument();
        return writer.toString();
    }
}
