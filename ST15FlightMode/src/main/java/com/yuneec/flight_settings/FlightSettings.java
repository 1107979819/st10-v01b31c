package com.yuneec.flight_settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.yuneec.channelsettings.ChannelSettings;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;
import com.yuneec.galleryloader.Gallery;
import com.yuneec.widget.MyToast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlightSettings extends Activity {
    public static final int AMBA_2_FLAG = 8;
    public static final int AMBA_2_FLAG_PRO = 32;
    public static final int AMBA_FLAG = 4;
    public static final String CAMERA_CURRENT_SELECTED = "camera_current_selected";
    public static final String CAMERA_SELECTED_INFO = "camera_selected_info";
    public static final String CAMERA_SELECTED_POSITION_VALUE = "camera_selected_position_value";
    public static final String CAMERA_TYPE_VALUE = "camera_type_flag";
    public static final int CC4IN1_FLAG = 1;
    public static final String FLIGHT_SETTINGS_FILE = "flight_setting_value";
    public static final String FLIGHT_SETTINGS_MODE = "mode_select_value";
    public static final int GOPRO_FLAG = 16;
    public static final String MASTER_UNIT = "master_unit";
    public static final int MODE_SELECT_1 = 1;
    public static final int MODE_SELECT_2 = 2;
    public static final int MODE_SELECT_3 = 3;
    public static final int MODE_SELECT_4 = 4;
    public static final int REMOTE_ZOOM_FLAG = 2;
    private static final int ST10_BIND = 0;
    private static final int ST10_CAMERA_SELECT = 1;
    private static final int ST10_HARDWARE_MONITOR = 3;
    private static final int ST10_MODE_SELECT = 2;
    private static final int ST10_OTHER_SETTINGS = 4;
    private static final int ST12_BIND = 0;
    private static final int ST12_CAMERA_SELECT = 1;
    private static final int ST12_HARDWARE_MONITOR = 3;
    private static final int ST12_MODE_SELECT = 2;
    private static final int ST12_OTHER_SETTINGS = 4;
    private static final int ST15_BIND = 0;
    private static final int ST15_CAMERA_SELECT = 3;
    private static final int ST15_CHANNEL_SETTINGS = 1;
    private static final int ST15_HARDWARE_MONITOR = 6;
    private static final int ST15_MODE_SELECT = 5;
    private static final int ST15_ONLINE_PICTURE = 7;
    private static final int ST15_QUICK_REVIEW = 4;
    private static final int ST15_SWITCH_SELECT = 2;
    public static final String TAG = "FlightSettings";
    public static final int UNIT_BSW = 2;
    public static final int UNIT_MASTER = 2;
    public static final int UNIT_METRIC = 1;
    public static final int UNIT_SERVANT = 1;
    public static final String VELOCITY_UNIT = "velocity_unit";
    private List<Map<String, Object>> data = new ArrayList();
    private ListView mListView;
    private OnItemClickListener mST10OnClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            switch (position) {
                case 0:
                    FlightSettings.this.startActivity(new Intent(FlightSettings.this, Bind.class));
                    return;
                case 1:
                    FlightSettings.this.startActivity(new Intent(FlightSettings.this, CameraSelect.class));
                    return;
                case 2:
                    FlightSettings.this.startActivity(new Intent(FlightSettings.this, ModeSelect.class));
                    return;
                case 3:
                    FlightSettings.this.startActivity(new Intent(FlightSettings.this, HardwareMonitorST10.class));
                    return;
                case 4:
                    FlightSettings.this.startActivity(new Intent(FlightSettings.this, OtherSettings.class));
                    return;
                default:
                    return;
            }
        }
    };
    private OnItemClickListener mST12OnClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            switch (position) {
                case 0:
                    FlightSettings.this.startActivity(new Intent(FlightSettings.this, Bind.class));
                    return;
                case 1:
                    FlightSettings.this.startActivity(new Intent(FlightSettings.this, CameraSelect.class));
                    return;
                case 2:
                    FlightSettings.this.startActivity(new Intent(FlightSettings.this, ModeSelect.class));
                    return;
                case 3:
                    FlightSettings.this.startActivity(new Intent(FlightSettings.this, HardwareMonitorST12.class));
                    return;
                case 4:
                    FlightSettings.this.startActivity(new Intent(FlightSettings.this, OtherSettings.class));
                    return;
                default:
                    return;
            }
        }
    };
    private OnItemClickListener mST15OnClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            switch (position) {
                case 0:
                    FlightSettings.this.startActivity(new Intent(FlightSettings.this, Bind.class));
                    return;
                case 1:
                    if (FlightSettings.this.modelId > 0) {
                        FlightSettings.this.startActivity(new Intent(FlightSettings.this, ChannelSettings.class));
                        return;
                    }
                    MyToast.makeText(FlightSettings.this, (int) R.string.select_fpv_model, 0, 0).show();
                    return;
                case 2:
                    if (FlightSettings.this.modelId > 0) {
                        FlightSettings.this.startActivity(new Intent(FlightSettings.this, SwitchSelect.class));
                        return;
                    }
                    MyToast.makeText(FlightSettings.this, (int) R.string.select_fpv_model, 0, 0).show();
                    return;
                case 3:
                    FlightSettings.this.startActivity(new Intent(FlightSettings.this, CameraSelect.class));
                    return;
                case 4:
                    FlightSettings.this.startActivity(new Intent(FlightSettings.this, QuickReview.class));
                    return;
                case 5:
                    FlightSettings.this.startActivity(new Intent(FlightSettings.this, ModeSelect.class));
                    return;
                case 6:
                    FlightSettings.this.startActivity(new Intent(FlightSettings.this, HardwareMonitor.class));
                    return;
                case 7:
                    FlightSettings.this.startActivity(new Intent(FlightSettings.this, Gallery.class));
                    return;
                default:
                    return;
            }
        }
    };
    private long modelId;

    class MyAdapter extends BaseAdapter {
        private List<Map<String, Object>> data;
        private List<View> holder;
        private ImageView imageView;
        private LayoutInflater inflater;
        private TextView titleText;

        public MyAdapter(Context context, List<Map<String, Object>> data) {
            this.inflater = LayoutInflater.from(context);
            this.data = data;
        }

        public int getCount() {
            return this.data.size();
        }

        public Object getItem(int arg0) {
            return this.data.get(arg0);
        }

        public long getItemId(int arg0) {
            return (long) arg0;
        }

        public View getView(int p, View v, ViewGroup parent) {
            if (v == null) {
                v = this.inflater.inflate(R.layout.date_list, null);
                this.imageView = (ImageView) v.findViewById(R.id.icon);
                this.titleText = (TextView) v.findViewById(R.id.text);
                this.holder = new ArrayList();
                this.holder.add(this.imageView);
                this.holder.add(this.titleText);
                v.setTag(this.holder);
            } else {
                this.holder = (ArrayList) v.getTag();
            }
            ((ImageView) this.holder.get(0)).setImageResource(((Integer) ((Map) this.data.get(p)).get("drawable_id")).intValue());
            ((TextView) this.holder.get(1)).setText((String) ((Map) this.data.get(p)).get("title_string"));
            return v;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(128);
        setContentView(R.layout.flight_settings_main);
        this.modelId = getSharedPreferences(FLIGHT_SETTINGS_FILE, 0).getLong("current_model_id", -2);
        Map<String, Object> d1;
        Map<String, Object> d2;
        Map<String, Object> d3;
        Map<String, Object> d4;
        Map<String, Object> d5;
        Map<String, Object> d6;
        if (Utilities.PROJECT_TAG.equals(Utilities.PROJECT_TAG) || Utilities.PROJECT_TAG.equals("ST12")) {
            d1 = new HashMap();
            d2 = new HashMap();
            d3 = new HashMap();
            d4 = new HashMap();
            d5 = new HashMap();
            d6 = new HashMap();
            d1.put("title_string", getResources().getString(R.string.str_mi_bind));
            d1.put("drawable_id", Integer.valueOf(R.drawable.mi_bind));
            d2.put("title_string", getResources().getString(R.string.str_mi_camera_select));
            d2.put("drawable_id", Integer.valueOf(R.drawable.mi_camera_select));
            d3.put("title_string", getResources().getString(R.string.str_mi_mode_select));
            d3.put("drawable_id", Integer.valueOf(R.drawable.mi_mode_select));
            d4.put("title_string", getResources().getString(R.string.str_mi_hardware_monitor));
            d4.put("drawable_id", Integer.valueOf(R.drawable.mi_hardware_monitor));
            d5.put("title_string", getResources().getString(R.string.str_mi_other_settings));
            d5.put("drawable_id", Integer.valueOf(R.drawable.mi_other_settings));
            this.data.add(d1);
            this.data.add(d2);
            this.data.add(d3);
            this.data.add(d4);
            this.data.add(d5);
        } else if (Utilities.PROJECT_TAG.equals("ST15")) {
            d1 = new HashMap();
            d2 = new HashMap();
            d3 = new HashMap();
            d4 = new HashMap();
            d5 = new HashMap();
            d6 = new HashMap();
            Map<String, Object> d7 = new HashMap();
            Map<String, Object> d8 = new HashMap();
            d1.put("title_string", getResources().getString(R.string.str_mi_bind));
            d1.put("drawable_id", Integer.valueOf(R.drawable.mi_bind));
            d2.put("title_string", getResources().getString(R.string.str_mi_channel_settings));
            d2.put("drawable_id", Integer.valueOf(R.drawable.mi_channel_settings));
            d3.put("title_string", getResources().getString(R.string.str_mi_switch_select));
            d3.put("drawable_id", Integer.valueOf(R.drawable.mi_switch_select));
            d4.put("title_string", getResources().getString(R.string.str_mi_camera_select));
            d4.put("drawable_id", Integer.valueOf(R.drawable.mi_camera_select));
            d5.put("title_string", getResources().getString(R.string.str_mi_quick_review));
            d5.put("drawable_id", Integer.valueOf(R.drawable.mi_quick_review));
            d6.put("title_string", getResources().getString(R.string.str_mi_mode_select));
            d6.put("drawable_id", Integer.valueOf(R.drawable.mi_mode_select));
            d7.put("title_string", getResources().getString(R.string.str_mi_hardware_monitor));
            d7.put("drawable_id", Integer.valueOf(R.drawable.mi_hardware_monitor));
            d8.put("title_string", getResources().getString(R.string.str_mi_online_picture));
            d8.put("drawable_id", Integer.valueOf(R.drawable.mi_quick_review));
            this.data.add(d1);
            this.data.add(d2);
            this.data.add(d3);
            this.data.add(d4);
            this.data.add(d5);
            this.data.add(d6);
            this.data.add(d7);
            this.data.add(d8);
        }
        this.mListView = (ListView) findViewById(R.id.system_settings_list);
        this.mListView.setAdapter(new MyAdapter(this, this.data));
        if (Utilities.PROJECT_TAG.equals(Utilities.PROJECT_TAG)) {
            this.mListView.setOnItemClickListener(this.mST10OnClickListener);
        } else if (Utilities.PROJECT_TAG.equals("ST15")) {
            this.mListView.setOnItemClickListener(this.mST15OnClickListener);
        } else if (Utilities.PROJECT_TAG.equals("ST12")) {
            this.mListView.setOnItemClickListener(this.mST12OnClickListener);
        }
    }
}
