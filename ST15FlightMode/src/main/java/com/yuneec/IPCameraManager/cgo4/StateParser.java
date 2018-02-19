package com.yuneec.IPCameraManager.cgo4;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class StateParser {
    static final String RESULT_NODE = "result";
    static final String RESULT_OK = "ok";
    static final String STATE_NODE = "state";
    private static final String TAG = StateParser.class.getSimpleName();
    private static final String ns = null;

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.yuneec.IPCameraManager.cgo4.StateResponse parse(java.lang.String r9) {
        /*
        r7 = new java.io.ByteArrayInputStream;
        r1 = r9.getBytes();
        r7.<init>(r1);
        r8 = android.util.Xml.newPullParser();	 Catch:{ Exception -> 0x0027 }
        r1 = "http://xmlpull.org/v1/doc/features.html#process-namespaces";
        r2 = 0;
        r8.setFeature(r1, r2);	 Catch:{ Exception -> 0x0027 }
        r1 = 0;
        r8.setInput(r7, r1);	 Catch:{ Exception -> 0x0027 }
        r8.nextTag();	 Catch:{ Exception -> 0x0027 }
        r1 = readResponseState(r8);	 Catch:{ Exception -> 0x0027 }
        r7.close();	 Catch:{ IOException -> 0x0022 }
    L_0x0021:
        return r1;
    L_0x0022:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x0021;
    L_0x0027:
        r0 = move-exception;
        r1 = new com.yuneec.IPCameraManager.cgo4.StateResponse;	 Catch:{ all -> 0x003b }
        r2 = 0;
        r3 = 0;
        r4 = 0;
        r6 = 0;
        r1.<init>(r2, r3, r4, r6);	 Catch:{ all -> 0x003b }
        r7.close();	 Catch:{ IOException -> 0x0036 }
        goto L_0x0021;
    L_0x0036:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x0021;
    L_0x003b:
        r1 = move-exception;
        r7.close();	 Catch:{ IOException -> 0x0040 }
    L_0x003f:
        throw r1;
    L_0x0040:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x003f;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.yuneec.IPCameraManager.cgo4.StateParser.parse(java.lang.String):com.yuneec.IPCameraManager.cgo4.StateResponse");
    }

    private static StateResponse readResponseState(XmlPullParser parser) throws XmlPullParserException, IOException {
        String CAMRPLY_NODE = "camrply";
        parser.require(2, ns, "camrply");
        boolean resultIsOk = false;
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                String name = parser.getName();
                if (name.equals(RESULT_NODE)) {
                    resultIsOk = RESULT_OK.equals(readTextNode(parser, name));
                } else if (name.equals(STATE_NODE)) {
                    return readInfo(parser, resultIsOk);
                } else {
                    skip(parser);
                }
            }
        }
        return new StateResponse(false, false, 0.0d, false);
    }

    private static StateResponse readInfo(XmlPullParser parser, boolean isOk) throws XmlPullParserException, IOException {
        String BATTERY_NODE = "batt";
        String SD_MEMORY_NODE = "sd_memory";
        String SD_INSERTED = "set";
        String RECORDING_STATUS = "rec";
        String RECORDING_ON = "on";
        String RECORDED_TIME = "progress_time";
        String PHOTO_CAPACITY = "remaincapacity";
        String VIDEO_CAPACITY = "video_remaincapacity";
        String batteryCapacity = "0";
        String sdCardSatus = "unset";
        String recordStatus = "off";
        String recordedTime = "0";
        String phtotoCapacity = "-1";
        String videoCapacity = "-1";
        parser.require(2, ns, STATE_NODE);
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                String name = parser.getName();
                if (name.equals("batt")) {
                    batteryCapacity = readTextNode(parser, name);
                } else {
                    if (name.equals("sd_memory")) {
                        sdCardSatus = readTextNode(parser, name);
                    } else {
                        if (name.equals("rec")) {
                            recordStatus = readTextNode(parser, name);
                        } else {
                            if (name.equals("progress_time")) {
                                recordedTime = readTextNode(parser, name);
                            } else {
                                if (name.equals("remaincapacity")) {
                                    phtotoCapacity = readTextNode(parser, name);
                                } else {
                                    if (name.equals("video_remaincapacity")) {
                                        videoCapacity = readTextNode(parser, name);
                                    } else {
                                        skip(parser);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        parser.require(3, ns, STATE_NODE);
        StateResponse response = new StateResponse(isOk, "set".equals(sdCardSatus), fractionToDouble(batteryCapacity), "on".equals(recordStatus));
        response.recordedTime = Integer.parseInt(recordedTime);
        response.phtotoCapacity = Integer.parseInt(phtotoCapacity);
        response.videoCapacity = Integer.parseInt(videoCapacity);
        return response;
    }

    private static double fractionToDouble(String ratio) {
        if (!ratio.contains("/")) {
            return Double.parseDouble(ratio);
        }
        String[] rat = ratio.split("/");
        return Double.parseDouble(rat[0]) / Double.parseDouble(rat[1]);
    }

    private static String readTextNode(XmlPullParser parser, String noteNme) throws XmlPullParserException, IOException {
        parser.require(2, ns, noteNme);
        String result = readText(parser);
        parser.require(3, ns, noteNme);
        return result;
    }

    private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() != 4) {
            return result;
        }
        result = parser.getText();
        parser.nextTag();
        return result;
    }

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != 2) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case 2:
                    depth++;
                    break;
                case 3:
                    depth--;
                    break;
                default:
                    break;
            }
        }
    }
}
