package com.yuneec.test;

import android.os.Handler;
import android.util.Log;
import com.yuneec.uartcontroller.UARTController;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.json.JSONObject;

public class Transfer implements Runnable {
    private static final int MAX_BUFFER_BYTES = 128;
    public static final int TYPE_DATA_FLIGHT = 1001;
    public static final int TYPE_DATA_GPS = 1002;
    public static final int TYPE_DATA_TELE = 1003;
    private Socket client;
    private int dataLength = 0;
    private BufferedInputStream mRece = null;
    private BufferedOutputStream mSend = null;
    private UARTController mUBSUARTController = null;
    private Handler mUSBHandler = null;
    private byte[] outBytesBuf = null;
    byte[] tmpByte = new byte[4];
    private boolean transferRunning = true;

    public Transfer(Socket client) {
        this.client = client;
    }

    public void stopTransfer() {
        this.transferRunning = false;
    }

    public void setUARTService(UARTController mUARTController) {
        this.mUBSUARTController = mUARTController;
    }

    public void setData(byte[] sendBuf, int typeData) {
        if (sendBuf.length >= 123) {
            Log.i("TEST", "ERROR setData length is over 256 bytes, is " + sendBuf.length);
        } else if (this.mSend == null || this.mRece == null || this.outBytesBuf == null) {
            Log.i("TEST", "ERROR setData mSend == null || mRece == null || outBytesBuf == null");
        } else {
            this.tmpByte[0] = (byte) 100;
            this.tmpByte[1] = (byte) sendBuf.length;
            if (typeData < 1001 || typeData > TYPE_DATA_TELE) {
                Log.i("TEST", "ERROR setData typeData=" + typeData);
                return;
            }
            if (typeData == 1001) {
                this.tmpByte[2] = (byte) 1;
            } else if (typeData == 1002) {
                this.tmpByte[2] = (byte) 2;
            } else if (typeData == TYPE_DATA_TELE) {
                this.tmpByte[2] = (byte) 3;
            }
            byte crcNum = generateCRC8(sendBuf, sendBuf.length);
            System.arraycopy(this.tmpByte, 0, this.outBytesBuf, 0, 3);
            System.arraycopy(sendBuf, 0, this.outBytesBuf, 3, sendBuf.length);
            this.outBytesBuf[sendBuf.length + 3] = crcNum;
            this.dataLength = sendBuf.length + 4;
            try {
                this.mSend.write(this.outBytesBuf, 0, this.dataLength);
                this.mSend.flush();
                Thread.sleep(10);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
        }
    }

    static byte generateCRC8(byte[] buffer, int len) {
        byte crci = (byte) 119;
        for (byte b : buffer) {
            crci = (byte) ((b & 119) ^ crci);
            for (int i = 0; i < 8; i++) {
                if ((crci & 1) != 0) {
                    crci = (byte) (((byte) (crci >> 1)) ^ 119);
                } else {
                    crci = (byte) (crci >> 1);
                }
            }
        }
        return crci;
    }

    private String rtnStrResult(boolean result) {
        String tmpString = "";
        if (result) {
            return "REQ_OK";
        }
        return "REQ_ERROR";
    }

    public void run() {
        try {
            this.mSend = new BufferedOutputStream(this.client.getOutputStream());
            this.mRece = new BufferedInputStream(this.client.getInputStream());
            this.outBytesBuf = new byte[144];
            while (this.transferRunning) {
                byte[] tempbuffer = new byte[128];
                try {
                    if (!this.client.isConnected()) {
                        Log.i("TEST", "TEST Service socket is Disconnected");
                        break;
                    }
                    String strFormsocket = new String(tempbuffer, 0, this.mRece.read(tempbuffer, 0, tempbuffer.length), "utf-8");
                    Log.i("TEST", "TEST Rece Data:[" + strFormsocket + "]");
                    if (this.mUBSUARTController != null) {
                        JSONObject jsonObj = new JSONObject(strFormsocket);
                        String type = jsonObj.getString("type");
                        if (type == null) {
                            Log.i("TEST", "TEST ERROR read data is wrong!");
                        } else if (type.equals("setting")) {
                            tmpBool = this.mUBSUARTController.writeTransmitRate(jsonObj.getInt("rf_power"));
                            Log.i("TEST", "TEST send Data setting : [" + tmpBool + "]");
                            this.mSend.write(rtnStrResult(tmpBool).getBytes(), 0, rtnStrResult(tmpBool).getBytes().length);
                            this.mSend.flush();
                        } else if (type.equals("getting")) {
                            String tmpString = String.valueOf(this.mUBSUARTController.readTransmitRate());
                            Log.i("TEST", "TEST send Data getting : [" + tmpString + "]");
                            this.mSend.write(tmpString.getBytes(), 0, tmpString.getBytes().length);
                            this.mSend.flush();
                        } else if (type.equals("enter")) {
                            tmpBool = this.mUBSUARTController.PCenterTestRF(jsonObj.getInt("rf_channel"), jsonObj.getInt("rf_mode"));
                            Log.i("TEST", "TEST send Data enter : [" + tmpBool + "]");
                            this.mSend.write(rtnStrResult(tmpBool).getBytes(), 0, rtnStrResult(tmpBool).getBytes().length);
                            this.mSend.flush();
                        } else if (type.equals("exit")) {
                            tmpBool = this.mUBSUARTController.enterRun(true);
                            Log.i("TEST", "TEST send Data exit : [" + tmpBool + "]");
                            this.mSend.write(rtnStrResult(tmpBool).getBytes(), 0, rtnStrResult(tmpBool).getBytes().length);
                            this.mSend.flush();
                        } else {
                            Log.i("TEST", "TEST ERROR read type is wrong!");
                        }
                    } else {
                        Log.i("TEST", "TEST ERROR mUBSUARTController is null!");
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    this.transferRunning = false;
                    Log.i("TEST", "TEST Exception!!! " + e);
                }
            }
            if (this.mUSBHandler != null) {
                this.mUSBHandler = null;
            }
            if (this.mSend != null) {
                this.mSend.close();
            }
            if (this.mRece != null) {
                this.mRece.close();
            }
            if (this.mRece != null) {
                this.outBytesBuf = null;
            }
            if (this.tmpByte != null) {
                this.tmpByte = null;
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public void sendData(byte[] dataByte, int offset, int length) throws IOException {
        if (this.mSend != null) {
            this.mSend.write(dataByte, offset, length);
            this.mSend.flush();
        }
    }
}
