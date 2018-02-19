package com.spreadtrum.android.eng;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OTA_mode extends Activity {
    private String OTA_MODE = "persist.yulong.defaultmode";
    private MyMainAdapter adapter = null;
    private List<HashMap<String, String>> list = null;
    private ListView menulist = null;

    public class MyMainAdapter extends SimpleAdapter {
        LayoutInflater mInflater;
        private List<? extends Map<String, ?>> mList;
        Map<Integer, Boolean> map = new HashMap();
        private int resource;

        public MyMainAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            this.mInflater = LayoutInflater.from(context);
            this.mList = data;
            this.resource = resource;
        }

        public int getCount() {
            return this.mList.size();
        }

        public Object getItem(int position) {
            return Integer.valueOf(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = this.mInflater.inflate(this.resource, null);
            }
            ((TextView) convertView.findViewById(R.id.ota_settinglist_title)).setText((String) ((Map) this.mList.get(position)).get("title"));
            TextView txt_subname = (TextView) convertView.findViewById(R.id.ota_settinglist_subtitle);
            CheckBox check = (CheckBox) convertView.findViewById(R.id.ota_setting_checkbox);
            if (((Map) this.mList.get(position)).get("showCheckbox").equals("1")) {
                check.setVisibility(0);
            } else {
                check.setVisibility(8);
            }
            txt_subname.setEnabled(false);
            if (((Map) this.mList.get(position)).get("subtitle").equals("")) {
                txt_subname.setVisibility(8);
            } else {
                txt_subname.setVisibility(0);
            }
            txt_subname.setText((String) ((Map) this.mList.get(position)).get("subtitle"));
            return convertView;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.ota_test_mode);
        this.list = new ArrayList();
        HashMap<String, String> info = new HashMap();
        info.put("title", getResources().getString(R.string.ota_service_setting));
        info.put("subtitle", "");
        info.put("showCheckbox", "0");
        this.list.add(info);
        HashMap<String, String> info3 = new HashMap();
        info3.put("title", getResources().getString(R.string.ota_test));
        info3.put("showCheckbox", "0");
        info3.put("subtitle", "");
        this.list.add(info3);
        String result = getResources().getStringArray(R.array.ota_mode_array)[SystemProperties.getInt(this.OTA_MODE, 0)];
        HashMap<String, String> info2 = new HashMap();
        info2.put("title", getResources().getString(R.string.ota_mode));
        info2.put("subtitle", result);
        info2.put("showCheckbox", "0");
        this.list.add(info2);
        this.menulist = (ListView) findViewById(R.id.ota_test_list);
        this.adapter = new MyMainAdapter(getApplicationContext(), this.list, R.layout.ota_test_mode_list_item, new String[]{"title", "subtitle"}, new int[]{R.id.ota_settinglist_title, R.id.ota_settinglist_subtitle});
        this.menulist.setAdapter(this.adapter);
        this.menulist.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View arg1, int position, long arg3) {
                Intent intent;
                switch (position) {
                    case 0:
                        intent = new Intent();
                        intent.setComponent(new ComponentName("com.yulong.android.ota", "com.yulong.android.ota.ui.LoginActivity"));
                        OTA_mode.this.startActivity(intent);
                        return;
                    case 1:
                        intent = new Intent();
                        intent.setComponent(new ComponentName("com.yulong.android.ota", "com.yulong.android.ota.ui.TestActivity"));
                        OTA_mode.this.startActivity(intent);
                        return;
                    case 2:
                        new Builder(OTA_mode.this).setTitle(R.string.ota_mode_change_dlg_title).setItems(R.array.ota_mode_array, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String result = OTA_mode.this.getResources().getStringArray(R.array.ota_mode_array)[which];
                                SystemProperties.set(OTA_mode.this.OTA_MODE, which + "");
                                ((HashMap) OTA_mode.this.list.get(2)).put("subtitle", result);
                                OTA_mode.this.adapter = new MyMainAdapter(OTA_mode.this.getApplicationContext(), OTA_mode.this.list, R.layout.ota_test_mode_list_item, new String[]{"title", "subtitle"}, new int[]{R.id.ota_settinglist_title, R.id.ota_settinglist_subtitle});
                                OTA_mode.this.menulist.setAdapter(OTA_mode.this.adapter);
                            }
                        }).create().show();
                        return;
                    default:
                        return;
                }
            }
        });
        super.onCreate(savedInstanceState);
    }
}
