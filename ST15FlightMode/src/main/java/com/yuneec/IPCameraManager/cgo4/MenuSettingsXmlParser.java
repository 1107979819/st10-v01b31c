package com.yuneec.IPCameraManager.cgo4;

import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class MenuSettingsXmlParser {
    static final String IS_ENABLE = "yes";
    static final String ITEM_ENABLE_TAG = "enable";
    static final String ITEM_ID_TAG = "id";
    static final String ITEM_NODE_NAME = "item";
    static final String ITEM_OPTION2_TAG = "option2";
    static final String ITEM_OPTION_TAG = "option";
    static final String ITEM_VALUE2_TAG = "value2";
    static final String ITEM_VALUE_TAG = "value";
    static final String MAIN_MENU_NODE_NAME = "mainmenu";
    static final String MENU_INFO_NODE_NAME = "menuinfo";
    static final String PHOTOSETTINGS_NODE_NAME = "photosettings";
    static final String QMENU2_NODE_NAME = "qmenu2";
    static final String RESULT_NODE_NAME = "result";
    static final String RESULT_OK = "ok";
    private static final String TAG = MenuSettingsXmlParser.class.getSimpleName();
    private static final String ns = null;

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.yuneec.IPCameraManager.cgo4.MenuSettingsResponse parse(java.lang.String r7) {
        /*
        r6 = this;
        r1 = new java.io.ByteArrayInputStream;
        r3 = r7.getBytes();
        r1.<init>(r3);
        r2 = android.util.Xml.newPullParser();	 Catch:{ Exception -> 0x0027 }
        r3 = "http://xmlpull.org/v1/doc/features.html#process-namespaces";
        r4 = 0;
        r2.setFeature(r3, r4);	 Catch:{ Exception -> 0x0027 }
        r3 = 0;
        r2.setInput(r1, r3);	 Catch:{ Exception -> 0x0027 }
        r2.nextTag();	 Catch:{ Exception -> 0x0027 }
        r3 = r6.readResponseMenu(r2);	 Catch:{ Exception -> 0x0027 }
        r1.close();	 Catch:{ IOException -> 0x0022 }
    L_0x0021:
        return r3;
    L_0x0022:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x0021;
    L_0x0027:
        r0 = move-exception;
        r3 = new com.yuneec.IPCameraManager.cgo4.MenuSettingsResponse;	 Catch:{ all -> 0x0038 }
        r4 = 0;
        r5 = 0;
        r3.<init>(r4, r5);	 Catch:{ all -> 0x0038 }
        r1.close();	 Catch:{ IOException -> 0x0033 }
        goto L_0x0021;
    L_0x0033:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x0021;
    L_0x0038:
        r3 = move-exception;
        r1.close();	 Catch:{ IOException -> 0x003d }
    L_0x003c:
        throw r3;
    L_0x003d:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x003c;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.yuneec.IPCameraManager.cgo4.MenuSettingsXmlParser.parse(java.lang.String):com.yuneec.IPCameraManager.cgo4.MenuSettingsResponse");
    }

    private MenuSettingsResponse readResponseMenu(XmlPullParser parser) throws XmlPullParserException, IOException {
        String NODE_NAME = "camrply";
        parser.require(2, ns, "camrply");
        boolean resultIsOk = false;
        List<MenuSettingsNode> menuNodes = null;
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                String name = parser.getName();
                if (name.equals(RESULT_NODE_NAME)) {
                    resultIsOk = readResult(parser);
                } else if (name.equals(MENU_INFO_NODE_NAME)) {
                    menuNodes = readMenuInfo(parser);
                    break;
                } else {
                    skip(parser);
                }
            }
        }
        return new MenuSettingsResponse(resultIsOk, menuNodes);
    }

    private boolean readResult(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, ns, RESULT_NODE_NAME);
        String result = readText(parser);
        parser.require(3, ns, RESULT_NODE_NAME);
        return result.equals(RESULT_OK);
    }

    private List<MenuSettingsNode> readMenuInfo(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, ns, MENU_INFO_NODE_NAME);
        List<MenuSettingsNode> menuNodes = new ArrayList();
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                String name = parser.getName();
                if (name.equals(MAIN_MENU_NODE_NAME)) {
                    menuNodes.add(readMenuNode(parser, MAIN_MENU_NODE_NAME));
                } else if (name.equals(PHOTOSETTINGS_NODE_NAME)) {
                    menuNodes.add(readMenuNode(parser, PHOTOSETTINGS_NODE_NAME));
                } else if (name.equals(QMENU2_NODE_NAME)) {
                    menuNodes.add(readMenuNode(parser, QMENU2_NODE_NAME));
                } else {
                    skip(parser);
                }
            }
        }
        parser.require(3, ns, MENU_INFO_NODE_NAME);
        return menuNodes;
    }

    private MenuSettingsNode readMenuNode(XmlPullParser parser, String nodeName) throws XmlPullParserException, IOException {
        MenuSettingsNode menuNode = new MenuSettingsNode(nodeName);
        parser.require(2, ns, nodeName);
        MenuSettingsItemContainer previousContainer = null;
        String previousContainerCompareString = " ";
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                if (parser.getName().equals(ITEM_NODE_NAME)) {
                    parser.require(2, ns, ITEM_NODE_NAME);
                    if (parser.getName().equals(ITEM_NODE_NAME)) {
                        String itemId = parser.getAttributeValue(null, ITEM_ID_TAG);
                        String itemEnable = parser.getAttributeValue(null, ITEM_ENABLE_TAG);
                        if (itemId == null || itemEnable == null) {
                            Log.e(TAG, "itemId or itemEnable is null!");
                        } else {
                            String itemValue = parser.getAttributeValue(null, ITEM_VALUE_TAG);
                            if (itemValue != null) {
                                MenuSettingsItemContainer itemContainer = new MenuSettingsItemContainer(itemId, itemEnable.equals(IS_ENABLE), itemValue, parser.getAttributeValue(null, ITEM_VALUE2_TAG));
                                menuNode.containers.put(itemId, itemContainer);
                                previousContainer = itemContainer;
                                if (previousContainer.id.length() > "menu_item_id_f".length()) {
                                    previousContainerCompareString = previousContainer.id.substring(0, previousContainer.id.length() - 1);
                                } else {
                                    previousContainerCompareString = previousContainer.id;
                                }
                            } else {
                                MenuSettingsItem itemNode = new MenuSettingsItem(itemId, itemEnable.equals(IS_ENABLE), parser.getAttributeValue(null, ITEM_OPTION_TAG), parser.getAttributeValue(null, ITEM_OPTION2_TAG));
                                if (previousContainer == null || !itemNode.id.contains(previousContainerCompareString)) {
                                    menuNode.items.add(itemNode);
                                } else {
                                    previousContainer.itemList.add(itemNode);
                                }
                            }
                        }
                        parser.nextTag();
                    }
                    parser.require(3, ns, ITEM_NODE_NAME);
                } else {
                    skip(parser);
                }
            }
        }
        parser.require(3, ns, nodeName);
        return menuNode;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() != 4) {
            return result;
        }
        result = parser.getText();
        parser.nextTag();
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
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
