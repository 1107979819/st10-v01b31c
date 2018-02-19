package com.spreadtrum.android.eng;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class phonetest extends ListActivity {
    static final String[] pts = new String[]{"Full phone test", "View phone test result", "Item test"};

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new ArrayAdapter(this, R.layout.list_item, pts));
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                switch (i) {
                    case 2:
                        phonetest.this.startActivity(new Intent(phonetest.this.getApplicationContext(), wifitest.class));
                        break;
                }
                if (view instanceof TextView) {
                    Toast.makeText(phonetest.this.getApplicationContext(), ((TextView) view).getText(), 0).show();
                }
            }
        });
    }
}
