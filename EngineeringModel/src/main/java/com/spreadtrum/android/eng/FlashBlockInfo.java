package com.spreadtrum.android.eng;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FlashBlockInfo extends Activity {
    private TextView mFlashblock;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flashblockinfo);
        this.mFlashblock = (TextView) findViewById(R.id.flashblock);
    }

    protected void onResume() {
        Throwable th;
        super.onResume();
        BufferedReader bufferedReader = null;
        try {
            String tmpRead;
            BufferedReader reader = new BufferedReader(new FileReader(new File("/proc/mtd")));
            String tmpString = null;
            int line = 1;
            while (true) {
                try {
                    tmpRead = reader.readLine();
                    if (tmpRead == null) {
                        break;
                    } else if (line == 2) {
                        break;
                    } else {
                        tmpString = tmpRead;
                        line++;
                    }
                } catch (IOException e) {
                    bufferedReader = reader;
                } catch (Throwable th2) {
                    th = th2;
                    bufferedReader = reader;
                }
            }
            tmpString = tmpString + "\n" + tmpRead;
            reader.close();
            bufferedReader = null;
            this.mFlashblock.setText(tmpString);
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e2) {
                    Log.d("FlashBlockInfo", "Read file failed.");
                }
            }
        } catch (IOException e3) {
            try {
                Log.d("FlashBlockInfo", "Read file failed.");
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e4) {
                        Log.d("FlashBlockInfo", "Read file failed.");
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e5) {
                        Log.d("FlashBlockInfo", "Read file failed.");
                    }
                }
                throw th;
            }
        }
    }
}
