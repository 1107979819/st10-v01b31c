package com.spreadtrum.android.eng;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class phoneinfo extends ListActivity {
    static final String[] pts = new String[]{"Version Info", "Third Party Version Info", "Net Info", "Phone Info", "Adc Calibration Info", "Restore Info", "Para Set", "App Set", "Layer1 Monitor", "IQ Modem"};

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new ArrayAdapter(this, R.layout.list_item, pts));
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                Log.e("engphoneinfo", "phoneinfo: p=" + i + "id=" + j);
                switch (i) {
                    case 2:
                        phoneinfo.this.startActivity(new Intent(phoneinfo.this.getApplicationContext(), netinfo.class));
                        break;
                }
                if (view instanceof TextView) {
                    Toast.makeText(phoneinfo.this.getApplicationContext(), ((TextView) view).getText(), 0).show();
                }
            }
        });
    }
}
