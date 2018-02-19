package com.spreadtrum.android.eng;

import android.app.Activity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UPAActivity extends Activity {
    private Button mBtnOk;
    private EditText mEditor;
    private engfetch mEf;
    private int mSocketID = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upasetting);
        this.mEditor = (EditText) findViewById(R.id.upa);
        this.mBtnOk = (Button) findViewById(R.id.upasetting);
        initView();
    }

    private void initView() {
        this.mEditor.setFocusableInTouchMode(false);
        this.mEditor.clearFocus();
        ((InputMethodManager) getSystemService("input_method")).hideSoftInputFromWindow(this.mEditor.getWindowToken(), 0);
        this.mEf = new engfetch();
        this.mSocketID = this.mEf.engopen();
        this.mEditor.append("Engmode socket open, id:" + this.mSocketID + "\n");
        this.mBtnOk.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                UPAActivity.this.mEditor.setText("");
                UPAActivity.this.selectCard();
                ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
                DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);
                Log.d("upasetting", "begin to  send the first cmd : at+cgeqreq=1,1,0,0");
                UPAActivity.this.mEditor.append("begin to send the first cmd : at+cgeqreq=1,1,0,0\n");
                try {
                    outputBufferStream.writeBytes(39 + ",1,at+cgeqreq=1,1,0,0");
                    UPAActivity.this.mEf.engwrite(UPAActivity.this.mSocketID, outputBuffer.toByteArray(), outputBuffer.toByteArray().length);
                    byte[] inputBytes = new byte[128];
                    String mATResponse1 = new String(inputBytes, 0, UPAActivity.this.mEf.engread(UPAActivity.this.mSocketID, inputBytes, 128));
                    Log.d("upasetting", "AT response:" + mATResponse1);
                    UPAActivity.this.mEditor.append("AT response:" + mATResponse1 + "\n");
                    if (outputBufferStream != null) {
                        try {
                            outputBufferStream.close();
                        } catch (IOException e) {
                            if (outputBufferStream != null) {
                                outputBufferStream = null;
                            }
                            if (outputBuffer != null) {
                                outputBuffer = null;
                            }
                            if (outputBufferStream != null) {
                            }
                            if (outputBuffer != null) {
                            }
                        } catch (Throwable th) {
                            if (outputBufferStream != null) {
                            }
                            if (outputBuffer != null) {
                            }
                        }
                    }
                    if (outputBuffer != null) {
                        outputBuffer.close();
                    }
                    if (outputBufferStream != null) {
                    }
                    if (outputBuffer != null) {
                    }
                    Log.d("upasetting", "begin to send the second cmd : at+cgcmod=1");
                    UPAActivity.this.mEditor.append("begin to  send the second cmd : at+cgcmod=1\n");
                    ByteArrayOutputStream outputBuffer2 = new ByteArrayOutputStream();
                    DataOutputStream outputBufferStream2 = new DataOutputStream(outputBuffer2);
                    try {
                        outputBufferStream2.writeBytes(39 + ",1,at+cgcmod=1");
                        UPAActivity.this.mEf.engwrite(UPAActivity.this.mSocketID, outputBuffer2.toByteArray(), outputBuffer2.toByteArray().length);
                        byte[] inputBytes2 = new byte[128];
                        String mATResponse2 = new String(inputBytes2, 0, UPAActivity.this.mEf.engread(UPAActivity.this.mSocketID, inputBytes2, 128));
                        Log.d("upasetting", "AT response:" + mATResponse2);
                        UPAActivity.this.mEditor.append("AT response:" + mATResponse2 + "\n\n");
                        UPAActivity.this.mEf.engclose(UPAActivity.this.mSocketID);
                        if (outputBufferStream2 != null) {
                            try {
                                outputBufferStream2.close();
                            } catch (IOException e2) {
                                if (outputBufferStream2 != null) {
                                    outputBufferStream2 = null;
                                }
                                if (outputBuffer2 != null) {
                                    outputBuffer2 = null;
                                }
                                if (outputBufferStream2 != null) {
                                }
                                if (outputBuffer2 == null) {
                                    return;
                                }
                                return;
                            } catch (Throwable th2) {
                                if (outputBufferStream2 != null) {
                                }
                                if (outputBuffer2 != null) {
                                }
                            }
                        }
                        if (outputBuffer2 != null) {
                            outputBuffer2.close();
                        }
                        if (outputBufferStream2 != null) {
                        }
                        if (outputBuffer2 == null) {
                        }
                    } catch (IOException e3) {
                        Log.e("upasetting", "writeBytes() error!");
                    }
                } catch (IOException e4) {
                    Log.e("upasetting", "writeBytes() error!");
                }
            }
        });
    }

    public void selectCard() {
        String mATCgeqreq;
        boolean isCard1Ready = TelephonyManager.getDefault(0).hasIccCard();
        boolean isCard2Ready = TelephonyManager.getDefault(1).hasIccCard();
        this.mEditor.append("begin to select card \n");
        if (isCard1Ready) {
            mATCgeqreq = 39 + ",1,AT+SPACTCARD=0";
            this.mEditor.append("SIM Card1 is ready!! Select Card1 \n AT+SPACTCARD=0\n");
        } else if (isCard2Ready) {
            mATCgeqreq = 39 + ",1,AT+SPACTCARD=1";
            this.mEditor.append("SIM Card2 is ready!! Select Card2 \n AT+SPACTCARD=1\n");
        } else {
            this.mBtnOk.setEnabled(false);
            this.mEditor.append("NO SIM Card!! \n");
            return;
        }
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);
        try {
            outputBufferStream.writeBytes(mATCgeqreq);
            this.mEf.engwrite(this.mSocketID, outputBuffer.toByteArray(), outputBuffer.toByteArray().length);
            byte[] inputBytes = new byte[128];
            String mATResponse1 = new String(inputBytes, 0, this.mEf.engread(this.mSocketID, inputBytes, 128));
            Log.d("upasetting", "AT response:" + mATResponse1);
            this.mEditor.append("AT response:" + mATResponse1 + "\n");
            if (outputBufferStream != null) {
                try {
                    outputBufferStream.close();
                } catch (IOException e) {
                    if (outputBufferStream != null) {
                        outputBufferStream = null;
                    }
                    if (outputBuffer != null) {
                        outputBuffer = null;
                    }
                    if (outputBufferStream != null) {
                    }
                    if (outputBuffer == null) {
                        return;
                    }
                    return;
                } catch (Throwable th) {
                    if (outputBufferStream != null) {
                    }
                    if (outputBuffer != null) {
                    }
                }
            }
            if (outputBuffer != null) {
                outputBuffer.close();
            }
            if (outputBufferStream != null) {
            }
            if (outputBuffer == null) {
            }
        } catch (IOException e2) {
            Log.e("upasetting", "writeBytes() error!");
        }
    }
}
