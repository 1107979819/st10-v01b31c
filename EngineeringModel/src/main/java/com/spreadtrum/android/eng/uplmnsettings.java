package com.spreadtrum.android.eng;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class uplmnsettings extends Activity {
    private int at_read_lenth = 0;
    private boolean bNeedSet = true;
    private List<String> data = new ArrayList();
    private int lenth_get = 0;
    private ListView listView = null;
    private EditText mEditText01;
    private EditText mEditText02;
    private EditText mEditText03;
    private engfetch mEf;
    private EventHandler mHandler;
    private int[] order = null;
    private String[] originalUPLMN = null;
    private byte[][] part = ((byte[][]) null);
    private byte[] setByte = new byte[6];
    private String[] showUPLMN = null;
    private int sockid = 0;
    private String str;
    private String[] strUorG = null;
    private int uplmn_list_num = 0;

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int m = 0;
            ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
            DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);
            byte[] inputBytes;
            int showlen;
            if (msg.what == 50) {
                uplmnsettings.this.str = msg.what + "," + 1 + "," + msg.arg1;
                try {
                    outputBufferStream.writeBytes(uplmnsettings.this.str);
                    uplmnsettings.this.mEf.engwrite(uplmnsettings.this.sockid, outputBuffer.toByteArray(), outputBuffer.toByteArray().length);
                    inputBytes = new byte[512];
                    showlen = uplmnsettings.this.mEf.engread(uplmnsettings.this.sockid, inputBytes, 512);
                    uplmnsettings.this.lenth_get = (showlen / 10) * 5;
                    if (showlen <= 3) {
                        uplmnsettings.this.DisplayToast(uplmnsettings.this.getString(R.string.no_sim_card_prompt));
                        uplmnsettings.this.listView.setVisibility(8);
                        uplmnsettings.this.finish();
                        return;
                    }
                    for (int i = 0; i < uplmnsettings.this.uplmn_list_num; i++) {
                        uplmnsettings.this.originalUPLMN[i] = new String(inputBytes, i * 10, 10, Charset.defaultCharset());
                    }
                    uplmnsettings.this.handleUTRANorGSM(inputBytes);
                    uplmnsettings.this.handleShowStrUPLMN(inputBytes);
                    int n = 0;
                    while (n < showlen / 10) {
                        if (uplmnsettings.this.strUorG[n].equals("G") || uplmnsettings.this.strUorG[n].equals("U")) {
                            uplmnsettings.this.data.add(uplmnsettings.this.showUPLMN[n].replaceAll("F", "") + ": " + uplmnsettings.this.strUorG[n]);
                            int m2 = m + 1;
                            uplmnsettings.this.order[m] = n;
                            m = m2;
                        }
                        n++;
                    }
                } catch (IOException e) {
                    Log.e("uplmnsettings", "writebytes error");
                }
            } else if (msg.what == 60) {
                uplmnsettings.this.str = String.format("%d,%d", new Object[]{Integer.valueOf(msg.what), Integer.valueOf(0)});
                uplmnsettings.this.str = msg.what + "," + 0;
                try {
                    outputBufferStream.writeBytes(uplmnsettings.this.str);
                    uplmnsettings.this.mEf.engwrite(uplmnsettings.this.sockid, outputBuffer.toByteArray(), outputBuffer.toByteArray().length);
                    inputBytes = new byte[512];
                    showlen = uplmnsettings.this.mEf.engread(uplmnsettings.this.sockid, inputBytes, 512);
                    String str = new String(inputBytes, 0, showlen);
                    if (showlen <= 3) {
                        uplmnsettings.this.DisplayToast(uplmnsettings.this.getString(R.string.no_sim_card_prompt));
                        uplmnsettings.this.listView.setVisibility(8);
                        uplmnsettings.this.finish();
                        return;
                    }
                    uplmnsettings.this.at_read_lenth = Integer.parseInt(getLenStringFromResponse(str), 16);
                    if (uplmnsettings.this.at_read_lenth >= 250) {
                        uplmnsettings.this.at_read_lenth = 100;
                    }
                    uplmnsettings.this.uplmn_list_num = uplmnsettings.this.at_read_lenth / 5;
                    setAllParameters(uplmnsettings.this.uplmn_list_num);
                    sendMessageToGetUPLMNList(uplmnsettings.this.at_read_lenth);
                } catch (IOException e2) {
                    Log.e("uplmnsettings", "writebytes error");
                }
            } else if (msg.what == 59) {
                uplmnsettings.this.str = msg.what + "," + 7 + "," + 214 + "," + 28512 + "," + 0 + "," + 0 + "," + (uplmnsettings.this.lenth_get < uplmnsettings.this.at_read_lenth ? uplmnsettings.this.lenth_get : uplmnsettings.this.at_read_lenth) + "," + uplmnsettings.this.getPacketData() + "," + "3F007FFF";
                Log.e("uplmnsettings", "setuplmn" + uplmnsettings.this.str);
                try {
                    outputBufferStream.writeBytes(uplmnsettings.this.str);
                    uplmnsettings.this.mEf.engwrite(uplmnsettings.this.sockid, outputBuffer.toByteArray(), outputBuffer.toByteArray().length);
                    byte[] inputBytes01 = new byte[64];
                    if (new String(inputBytes01, 0, uplmnsettings.this.mEf.engread(uplmnsettings.this.sockid, inputBytes01, 64)).equals("144")) {
                        uplmnsettings.this.finish();
                    } else {
                        uplmnsettings.this.DisplayToast("Failed and try again.");
                    }
                } catch (IOException e3) {
                    Log.e("uplmnsettings", "writebytes error");
                }
            }
        }

        private void setAllParameters(int len) {
            uplmnsettings.this.showUPLMN = new String[len];
            uplmnsettings.this.originalUPLMN = new String[len];
            uplmnsettings.this.strUorG = new String[len];
            uplmnsettings.this.part = new byte[len][];
            uplmnsettings.this.order = new int[len];
            for (int i = 0; i < len; i++) {
                uplmnsettings.this.part[i] = new byte[6];
            }
        }

        private void sendMessageToGetUPLMNList(int length) {
            uplmnsettings.this.mHandler.removeMessages(0);
            uplmnsettings.this.mHandler.sendMessage(uplmnsettings.this.mHandler.obtainMessage(50, length, 0, Integer.valueOf(0)));
        }

        private String getLenStringFromResponse(String str) {
            int index = 0;
            String strBody = str.substring(4, str.length());
            String result = "0";
            while (index < strBody.length()) {
                int len = transferStringToInt(strBody.substring(index + 2, index + 4));
                if (strBody.substring(index, index + 2).equals("80")) {
                    return strBody.substring(index + 4, (index + 4) + (len * 2));
                }
                index = ((len * 2) + index) + 4;
            }
            return result;
        }

        private int transferStringToInt(String str) {
            return Integer.parseInt(str, 16);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uplmnlist);
        initialPara();
        this.listView = (ListView) findViewById(R.id.ListView01);
        this.listView.setAdapter(new ArrayAdapter(this, 17367043, this.data));
        this.listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View arg1, int position, long id) {
                uplmnsettings.this.showEditDialog(position);
            }
        });
    }

    private void initialPara() {
        int phoneId = 0;
        Intent intent = getIntent();
        if (intent != null) {
            phoneId = intent.getIntExtra("sub_id", 0);
        }
        this.mEf = new engfetch();
        this.sockid = this.mEf.engopen(phoneId);
        this.mHandler = new EventHandler(Looper.myLooper());
        this.mHandler.removeMessages(0);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(60, 0, 0, Integer.valueOf(0)));
    }

    private void showEditDialog(int pos) {
        View view = LayoutInflater.from(this).inflate(R.layout.uplmn_edit, null);
        this.mEditText01 = (EditText) view.findViewById(R.id.index_value);
        this.mEditText02 = (EditText) view.findViewById(R.id.id_value);
        this.mEditText03 = (EditText) view.findViewById(R.id.type_value);
        this.mEditText01.setText("" + this.order[pos]);
        this.mEditText02.setText(this.showUPLMN[this.order[pos]].replaceAll("F", ""));
        this.mEditText03.setText("" + getvalueofUTRANorGSM(this.order[pos]));
        new Builder(this).setTitle("UPLMN set").setView(view).setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String editIndex = uplmnsettings.this.mEditText01.getText().toString();
                String editId = uplmnsettings.this.mEditText02.getText().toString();
                if (uplmnsettings.this.checkInputParametersIsWrong(editIndex, editId, uplmnsettings.this.mEditText03.getText().toString())) {
                    Log.d("uplmnsettings", "bNeedSet is set to false");
                    uplmnsettings.this.bNeedSet = false;
                } else {
                    uplmnsettings.this.changeDataFromEdit(Integer.parseInt(uplmnsettings.this.mEditText01.getText().toString()), editId, Integer.parseInt(uplmnsettings.this.mEditText03.getText().toString()));
                }
                if (uplmnsettings.this.bNeedSet) {
                    Log.d("uplmnsettings", "ENG_AT_SETUPLMN");
                    uplmnsettings.this.mHandler.sendMessage(uplmnsettings.this.mHandler.obtainMessage(59, 0, 0, Integer.valueOf(0)));
                }
            }
        }).setNegativeButton(17039360, null).create().show();
    }

    private String getPacketData() {
        String str = "";
        for (int i = 0; i < this.lenth_get / 5; i++) {
            str = str + this.originalUPLMN[i];
        }
        return str;
    }

    private boolean checkInputParametersIsWrong(String str01, String str02, String str03) {
        if (str01.length() == 0) {
            DisplayToast("Index is empty. try again!");
            return true;
        } else if (str03.length() == 0) {
            DisplayToast("Type is empty. try again!");
            return true;
        } else if (str02.length() < 5) {
            DisplayToast("Id number " + str02 + " is too short. try again!");
            return true;
        } else if (Integer.parseInt(str03) <= 1 && Integer.parseInt(str03) >= 0) {
            return false;
        } else {
            DisplayToast("Type should be 1 or 0. try again!");
            return true;
        }
    }

    private void changeDataFromEdit(int index, String strId, int tag) {
        int i;
        if (strId.length() == 5) {
            String compare = "";
            byte[] byteId05 = (strId + "F").getBytes();
            for (i = 0; i < byteId05.length; i++) {
                this.setByte[i] = byteId05[transferSpecialIntPlus(i, 0)];
            }
            if (tag == 0) {
                compare = new String(this.setByte) + "8000";
            } else if (tag == 1) {
                compare = new String(this.setByte) + "0080";
            }
            if (this.originalUPLMN[index].equals(compare)) {
                this.bNeedSet = false;
                return;
            }
            this.bNeedSet = true;
            this.originalUPLMN[index] = compare;
        } else if (strId.length() == 6) {
            String compare01 = "";
            byte[] byteId06 = strId.getBytes();
            for (i = 0; i < byteId06.length; i++) {
                this.setByte[i] = byteId06[transferSpecialIntPlus(i, 0)];
            }
            if (tag == 0) {
                compare01 = new String(this.setByte) + "8000";
            } else if (tag == 1) {
                compare01 = new String(this.setByte) + "0080";
            }
            if (this.originalUPLMN[index].equals(compare01)) {
                this.bNeedSet = false;
                return;
            }
            this.bNeedSet = true;
            this.originalUPLMN[index] = compare01;
        }
    }

    private void DisplayToast(String str) {
        Toast.makeText(this, str, 0).show();
    }

    private int getvalueofUTRANorGSM(int index) {
        if (this.strUorG[index] == "G") {
            return 1;
        }
        return 0;
    }

    private void handleUTRANorGSM(byte[] input) {
        for (int i = 0; i < this.uplmn_list_num; i++) {
            if (input[(i * 10) + 6] == (byte) 56) {
                this.strUorG[i] = "U";
            } else if (input[(i * 10) + 8] == (byte) 56) {
                this.strUorG[i] = "G";
            } else {
                this.strUorG[i] = "NULL";
            }
        }
    }

    private int transferSpecialInt(int i, int offset) {
        switch (i) {
            case 0:
                return offset + 1;
            case 1:
                return offset + 0;
            case 2:
                return offset + 3;
            case 3:
                return offset + 5;
            case 4:
                return offset + 4;
            case 5:
                return offset + 2;
            default:
                return offset + 0;
        }
    }

    private int transferSpecialIntPlus(int i, int offset) {
        switch (i) {
            case 0:
                return offset + 1;
            case 1:
                return offset + 0;
            case 2:
                return offset + 5;
            case 3:
                return offset + 2;
            case 4:
                return offset + 4;
            case 5:
                return offset + 3;
            default:
                return offset + 0;
        }
    }

    private void handleShowStrUPLMN(byte[] input) {
        for (int j = 0; j < this.uplmn_list_num; j++) {
            for (int i = 0; i < 6; i++) {
                this.part[j][i] = input[transferSpecialInt(i, j * 10)];
            }
            this.showUPLMN[j] = new String(this.part[j]);
        }
    }
}
