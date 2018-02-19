package com.yuneec.flightmode15;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.view.MotionEventCompat;
import com.yuneec.IPCameraManager.Amba2;
import com.yuneec.IPCameraManager.BindResponse;
import com.yuneec.IPCameraManager.BindStateResponse;
import com.yuneec.uartcontroller.UARTInfoMessage;
import com.yuneec.uartcontroller.UARTInfoMessage.Channel;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Locale;

public class ChannelDataForward {
    private static final String MAC_ADDRESS = getMacAddress();
    private static final int REMOTE_PORT = 49156;
    private static final int[] TABLE;
    private static int count = 0;
    private static ChannelDataForward instance = null;
    private final String TAG = ChannelDataForward.class.getSimpleName();
    private Amba2 bindCamera;
    private ForwardCallback callback;
    private boolean isBindedCamera = false;
    private HttpRequestHandler mHandler = new HttpRequestHandler();
    private Messenger mHttpResponseMessenger = new Messenger(this.mHandler);
    private Handler sendHandler;
    private HandlerThread sendThread;
    private InetAddress serverAddress;
    private DatagramSocket socket;

    public interface ForwardCallback {
        void onBindResult(boolean z);
    }

    private class HttpRequestHandler extends Handler {
        private String serverMacAddress;

        private HttpRequestHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    switch (msg.arg1) {
                        case 1001:
                            BindResponse bindResponse = msg.obj;
                            if (bindResponse.bindedResult) {
                                this.serverMacAddress = bindResponse.serverMacAddress;
                                if (ChannelDataForward.this.bindCamera != null) {
                                    ChannelDataForward.this.bindCamera.getBindState(ChannelDataForward.this.mHttpResponseMessenger);
                                    return;
                                }
                                return;
                            }
                            ChannelDataForward.this.isBindedCamera = false;
                            if (ChannelDataForward.this.callback != null) {
                                ChannelDataForward.this.callback.onBindResult(false);
                                return;
                            }
                            return;
                        case 1002:
                            BindStateResponse bindStateResponse = msg.obj;
                            if (!bindStateResponse.isOk) {
                                ChannelDataForward.this.unbind();
                                if (ChannelDataForward.this.callback != null) {
                                    ChannelDataForward.this.callback.onBindResult(false);
                                    return;
                                }
                                return;
                            } else if (bindStateResponse.isBinded) {
                                if (ChannelDataForward.MAC_ADDRESS.equals(bindStateResponse.bindedClientAddress)) {
                                    try {
                                        if (!ChannelDataForward.this.isBindedCamera) {
                                            ChannelDataForward.this.setupSocket();
                                            ChannelDataForward.this.sendThread = new HandlerThread("forward thread");
                                            ChannelDataForward.this.sendThread.start();
                                            ChannelDataForward.this.sendHandler = new Handler(ChannelDataForward.this.sendThread.getLooper());
                                        }
                                        ChannelDataForward.count = 0;
                                        ChannelDataForward.this.isBindedCamera = true;
                                        if (ChannelDataForward.this.callback != null) {
                                            ChannelDataForward.this.callback.onBindResult(true);
                                            return;
                                        }
                                        return;
                                    } catch (SocketException e) {
                                        ChannelDataForward.this.unbind();
                                        if (ChannelDataForward.this.callback != null) {
                                            ChannelDataForward.this.callback.onBindResult(false);
                                        }
                                        e.printStackTrace();
                                        return;
                                    } catch (UnknownHostException e2) {
                                        ChannelDataForward.this.unbind();
                                        if (ChannelDataForward.this.callback != null) {
                                            ChannelDataForward.this.callback.onBindResult(false);
                                        }
                                        e2.printStackTrace();
                                        return;
                                    }
                                }
                                ChannelDataForward.this.unbind();
                                if (ChannelDataForward.this.callback != null) {
                                    ChannelDataForward.this.callback.onBindResult(false);
                                    return;
                                }
                                return;
                            } else if (ChannelDataForward.this.bindCamera != null) {
                                ChannelDataForward.this.bindCamera.requestBind(ChannelDataForward.this.mHttpResponseMessenger, ChannelDataForward.MAC_ADDRESS);
                                return;
                            } else {
                                return;
                            }
                        default:
                            return;
                    }
                default:
                    return;
            }
        }
    }

    static {
        int[] iArr = new int[256];
        iArr[1] = 4489;
        iArr[2] = 8978;
        iArr[3] = 12955;
        iArr[4] = 17956;
        iArr[5] = 22445;
        iArr[6] = 25910;
        iArr[7] = 29887;
        iArr[8] = 35912;
        iArr[9] = 40385;
        iArr[10] = 44890;
        iArr[11] = 48851;
        iArr[12] = 51820;
        iArr[13] = 56293;
        iArr[14] = 59774;
        iArr[15] = 63735;
        iArr[16] = 4225;
        iArr[17] = 264;
        iArr[18] = 13203;
        iArr[19] = 8730;
        iArr[20] = 22181;
        iArr[21] = 18220;
        iArr[22] = 30135;
        iArr[23] = 25662;
        iArr[24] = 40137;
        iArr[25] = 36160;
        iArr[26] = 49115;
        iArr[27] = 44626;
        iArr[28] = 56045;
        iArr[29] = 52068;
        iArr[30] = 63999;
        iArr[31] = 59510;
        iArr[32] = 8450;
        iArr[33] = 12427;
        iArr[34] = 528;
        iArr[35] = 5017;
        iArr[36] = 26406;
        iArr[37] = 30383;
        iArr[38] = 17460;
        iArr[39] = 21949;
        iArr[40] = 44362;
        iArr[41] = 48323;
        iArr[42] = 36440;
        iArr[43] = 40913;
        iArr[44] = 60270;
        iArr[45] = 64231;
        iArr[46] = 51324;
        iArr[47] = 55797;
        iArr[48] = 12675;
        iArr[49] = 8202;
        iArr[50] = 4753;
        iArr[51] = 792;
        iArr[52] = 30631;
        iArr[53] = 26158;
        iArr[54] = 21685;
        iArr[55] = 17724;
        iArr[56] = 48587;
        iArr[57] = 44098;
        iArr[58] = 40665;
        iArr[59] = 36688;
        iArr[60] = 64495;
        iArr[61] = 60006;
        iArr[62] = 55549;
        iArr[63] = 51572;
        iArr[64] = 16900;
        iArr[65] = 21389;
        iArr[66] = 24854;
        iArr[67] = 28831;
        iArr[68] = 1056;
        iArr[69] = 5545;
        iArr[70] = 10034;
        iArr[71] = 14011;
        iArr[72] = 52812;
        iArr[73] = 57285;
        iArr[74] = 60766;
        iArr[75] = 64727;
        iArr[76] = 34920;
        iArr[77] = 39393;
        iArr[78] = 43898;
        iArr[79] = 47859;
        iArr[80] = 21125;
        iArr[81] = 17164;
        iArr[82] = 29079;
        iArr[83] = 24606;
        iArr[84] = 5281;
        iArr[85] = 1320;
        iArr[86] = 14259;
        iArr[87] = 9786;
        iArr[88] = 57037;
        iArr[89] = 53060;
        iArr[90] = 64991;
        iArr[91] = 60502;
        iArr[92] = 39145;
        iArr[93] = 35168;
        iArr[94] = 48123;
        iArr[95] = 43634;
        iArr[96] = 25350;
        iArr[97] = 29327;
        iArr[98] = 16404;
        iArr[99] = 20893;
        iArr[100] = 9506;
        iArr[101] = 13483;
        iArr[102] = 1584;
        iArr[103] = 6073;
        iArr[104] = 61262;
        iArr[105] = 65223;
        iArr[106] = 52316;
        iArr[107] = 56789;
        iArr[108] = 43370;
        iArr[109] = 47331;
        iArr[110] = 35448;
        iArr[111] = 39921;
        iArr[112] = 29575;
        iArr[113] = 25102;
        iArr[114] = 20629;
        iArr[115] = 16668;
        iArr[116] = 13731;
        iArr[117] = 9258;
        iArr[118] = 5809;
        iArr[119] = 1848;
        iArr[120] = 65487;
        iArr[121] = 60998;
        iArr[122] = 56541;
        iArr[123] = 52564;
        iArr[124] = 47595;
        iArr[Utilities.OFFSET_SWITCH_MAX_1] = 43106;
        iArr[126] = 39673;
        iArr[127] = 35696;
        iArr[128] = 33800;
        iArr[129] = 38273;
        iArr[130] = 42778;
        iArr[131] = 46739;
        iArr[132] = 49708;
        iArr[133] = 54181;
        iArr[134] = 57662;
        iArr[135] = 61623;
        iArr[136] = 2112;
        iArr[137] = 6601;
        iArr[138] = 11090;
        iArr[139] = 15067;
        iArr[140] = 20068;
        iArr[UARTInfoMessage.FEEDBACK_TELEMETRY_COORDINATES_INFO] = 24557;
        iArr[142] = 28022;
        iArr[143] = 31999;
        iArr[144] = 38025;
        iArr[145] = 34048;
        iArr[146] = 47003;
        iArr[147] = 42514;
        iArr[148] = 53933;
        iArr[149] = 49956;
        iArr[Utilities.OFFSET_SWITCH_MAX_2] = 61887;
        iArr[151] = 57398;
        iArr[152] = 6337;
        iArr[153] = 2376;
        iArr[154] = 15315;
        iArr[155] = 10842;
        iArr[156] = 24293;
        iArr[157] = 20332;
        iArr[158] = 32247;
        iArr[159] = 27774;
        iArr[160] = 42250;
        iArr[161] = 46211;
        iArr[162] = 34328;
        iArr[163] = 38801;
        iArr[164] = 58158;
        iArr[165] = 62119;
        iArr[166] = 49212;
        iArr[167] = 53685;
        iArr[168] = 10562;
        iArr[169] = 14539;
        iArr[170] = 2640;
        iArr[171] = 7129;
        iArr[172] = 28518;
        iArr[173] = 32495;
        iArr[174] = 19572;
        iArr[175] = 24061;
        iArr[176] = 46475;
        iArr[177] = 41986;
        iArr[178] = 38553;
        iArr[179] = 34576;
        iArr[180] = 62383;
        iArr[181] = 57894;
        iArr[182] = 53437;
        iArr[183] = 49460;
        iArr[184] = 14787;
        iArr[185] = 10314;
        iArr[186] = 6865;
        iArr[187] = 2904;
        iArr[188] = 32743;
        iArr[189] = 28270;
        iArr[190] = 23797;
        iArr[191] = 19836;
        iArr[192] = 50700;
        iArr[193] = 55173;
        iArr[194] = 58654;
        iArr[195] = 62615;
        iArr[196] = 32808;
        iArr[197] = 37281;
        iArr[198] = 41786;
        iArr[199] = 45747;
        iArr[200] = 19012;
        iArr[201] = 23501;
        iArr[202] = 26966;
        iArr[203] = 30943;
        iArr[204] = 3168;
        iArr[205] = 7657;
        iArr[206] = 12146;
        iArr[207] = 16123;
        iArr[208] = 54925;
        iArr[209] = 50948;
        iArr[210] = 62879;
        iArr[211] = 58390;
        iArr[212] = 37033;
        iArr[213] = 33056;
        iArr[214] = 46011;
        iArr[215] = 41522;
        iArr[216] = 23237;
        iArr[217] = 19276;
        iArr[218] = 31191;
        iArr[219] = 26718;
        iArr[220] = 7393;
        iArr[221] = 3432;
        iArr[222] = 16371;
        iArr[223] = 11898;
        iArr[224] = 59150;
        iArr[225] = 63111;
        iArr[226] = 50204;
        iArr[227] = 54677;
        iArr[228] = 41258;
        iArr[229] = 45219;
        iArr[230] = 33336;
        iArr[231] = 37809;
        iArr[232] = 27462;
        iArr[233] = 31439;
        iArr[234] = 18516;
        iArr[235] = 23005;
        iArr[236] = 11618;
        iArr[237] = 15595;
        iArr[238] = 3696;
        iArr[239] = 8185;
        iArr[240] = 63375;
        iArr[241] = 58886;
        iArr[242] = 54429;
        iArr[243] = 50452;
        iArr[244] = 45483;
        iArr[245] = 40994;
        iArr[246] = 37561;
        iArr[247] = 33584;
        iArr[248] = 31687;
        iArr[249] = 27214;
        iArr[250] = 22741;
        iArr[251] = 18780;
        iArr[252] = 15843;
        iArr[253] = 11370;
        iArr[254] = 7921;
        iArr[MotionEventCompat.ACTION_MASK] = 3960;
        TABLE = iArr;
    }

    private ChannelDataForward() {
    }

    public static ChannelDataForward getInstance() {
        if (instance == null) {
            instance = new ChannelDataForward();
        }
        return instance;
    }

    public void bind() {
        if (this.bindCamera != null) {
            this.bindCamera.getBindState(this.mHttpResponseMessenger);
        } else if (this.callback != null) {
            this.callback.onBindResult(false);
        }
    }

    public void unbind() {
        this.isBindedCamera = false;
        if (this.socket != null) {
            this.socket.close();
            this.socket = null;
            this.sendHandler.removeCallbacksAndMessages(null);
            this.sendHandler.getLooper().quit();
        }
    }

    public void setCallback(ForwardCallback callback) {
        this.callback = callback;
    }

    public void setBindCamera(Amba2 camera) {
        this.bindCamera = camera;
    }

    public void exit() {
        unbind();
        instance = null;
    }

    private void setupSocket() throws SocketException, UnknownHostException {
        this.socket = new DatagramSocket(23602);
        this.serverAddress = InetAddress.getByName(Amba2.REMOTE_ADDRESS);
    }

    private static String getMacAddress() {
        try {
            return loadFileAsString("/sys/class/net/wlan0/address").toLowerCase(Locale.ENGLISH).substring(0, 17);
        } catch (IOException e) {
            return null;
        }
    }

    private static String loadFileAsString(String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        while (true) {
            int numRead = reader.read(buf);
            if (numRead == -1) {
                reader.close();
                return fileData.toString();
            }
            fileData.append(String.valueOf(buf, 0, numRead));
        }
    }

    public boolean isBindedCamera() {
        return this.isBindedCamera;
    }

    public void forwardMixChannelData(Channel channelData) {
        if (this.isBindedCamera) {
            sendToRemote(packToYMavlink(channelData));
        }
    }

    private byte[] packToYMavlink(Channel channelData) {
        byte[] yData = new byte[37];
        yData[0] = (byte) -2;
        yData[1] = (byte) 27;
        int i = count;
        count = i + 1;
        yData[2] = (byte) i;
        yData[3] = (byte) 4;
        yData[4] = (byte) 0;
        yData[5] = (byte) 2;
        yData[6] = (byte) 0;
        yData[7] = (byte) 6;
        byte previousHalf = (byte) 0;
        for (int i2 = 0; i2 < 12; i2++) {
            float chanelValue = ((Float) channelData.channels.get(i2)).floatValue();
            if (i2 % 2 == 0) {
                yData[(i2 + 8) + (i2 / 2)] = (byte) ((((int) chanelValue) >> 4) & MotionEventCompat.ACTION_MASK);
                previousHalf = (byte) (((int) chanelValue) & 15);
            } else {
                yData[(i2 + 8) + (i2 / 2)] = (byte) (((previousHalf << 4) & 240) | ((((int) chanelValue) >> 8) & 15));
                yData[((i2 + 8) + (i2 / 2)) + 1] = (byte) (((int) chanelValue) & MotionEventCompat.ACTION_MASK);
            }
        }
        yData[35] = (byte) 0;
        int crcResult = crc16(yData, 1, 36);
        yData[35] = (byte) (crcResult & MotionEventCompat.ACTION_MASK);
        yData[36] = (byte) ((crcResult >> 8) & MotionEventCompat.ACTION_MASK);
        return yData;
    }

    public static String byteArrayToString(byte[] byteValue) {
        StringBuilder s = new StringBuilder();
        int length = byteValue.length;
        for (int i = 0; i < length; i++) {
            s.append(String.format(" %02x", new Object[]{Byte.valueOf(byteValue[i])}));
        }
        return s.toString();
    }

    private void sendToRemote(final byte[] yMavlinkData) {
        if (this.socket != null) {
            this.sendHandler.post(new Runnable() {
                public void run() {
                    try {
                        ChannelDataForward.this.socket.send(new DatagramPacket(yMavlinkData, yMavlinkData.length, ChannelDataForward.this.serverAddress, ChannelDataForward.REMOTE_PORT));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private static int crc16(byte[] message, int offset, int count) {
        int crc = 65535;
        for (int i = offset; i < count; i++) {
            crc = (crc >> 8) ^ TABLE[((message[i] & MotionEventCompat.ACTION_MASK) ^ crc) & MotionEventCompat.ACTION_MASK];
        }
        return crc;
    }
}
