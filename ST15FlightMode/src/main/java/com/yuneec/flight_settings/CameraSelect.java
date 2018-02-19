package com.yuneec.flight_settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.yuneec.IPCameraManager.Cameras;
import com.yuneec.IPCameraManager.IPCameraManager;
import com.yuneec.database.DataProviderHelper;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;
import com.yuneec.flightmode15.WeakHandler;
import com.yuneec.widget.MyToast;
import com.yuneec.widget.OneButtonPopDialog;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CameraSelect extends Activity {
    private static final String TAG = "CameraSelect";
    private OnItemClickListener adapterOnItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            CameraSelect.this.current_positon = position;
            Cameras cm = (Cameras) parent.getItemAtPosition(position);
            CameraSelect.this.camera_name.setText(cm.getName());
            CameraSelect.this.camera_type.setText(cm.getType());
            CameraSelect.this.current_type = CameraSelect.parseType(cm.getType());
            if (CameraSelect.this.mIPCameraManager != null) {
                CameraSelect.this.mIPCameraManager.finish();
                CameraSelect.this.mIPCameraManager = null;
            }
            if ((CameraSelect.this.current_type & 1) == 1) {
                CameraSelect.this.mIPCameraManager = IPCameraManager.getIPCameraManager(CameraSelect.this, 101);
            } else if ((CameraSelect.this.current_type & 4) == 4) {
                CameraSelect.this.mIPCameraManager = IPCameraManager.getIPCameraManager(CameraSelect.this, 102);
            } else if ((CameraSelect.this.current_type & 8) == 8 || (CameraSelect.this.current_type & 32) == 32) {
                CameraSelect.this.mIPCameraManager = IPCameraManager.getIPCameraManager(CameraSelect.this, 104);
            } else if ((CameraSelect.this.current_type & 16) == 16) {
                CameraSelect.this.mIPCameraManager = IPCameraManager.getIPCameraManager(CameraSelect.this, 105);
            } else {
                CameraSelect.this.mIPCameraManager = IPCameraManager.getIPCameraManager(CameraSelect.this, 100);
            }
        }
    };
    private TextView camera_name;
    private TextView camera_type;
    private List<Cameras> cameras;
    private ListView choose_camera_list;
    private Button choose_camera_selected;
    private int current_positon;
    private int current_type;
    private OnClickListener mButtonOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            BindWifiManage bwm = new BindWifiManage((WifiManager) CameraSelect.this.getSystemService("wifi"));
            Log.v(CameraSelect.TAG, " wifi -- " + bwm.getWifiStatus());
            if (bwm.getWifiStatus()) {
                CameraSelect.this.mCurrentCamera = (Cameras) CameraSelect.this.mCamerasList.get(CameraSelect.this.current_positon);
                CameraSelect.this.mPrefs.edit().putString(FlightSettings.CAMERA_SELECTED_INFO, CameraSelect.this.getCurrentmCurrentCameraTypeString(CameraSelect.this.mCurrentCamera)).commit();
                CameraSelect.this.mPrefs.edit().putString(FlightSettings.CAMERA_CURRENT_SELECTED, CameraSelect.this.mCurrentCamera.getName()).commit();
                CameraSelect.this.current_type = CameraSelect.parseType(CameraSelect.this.mCurrentCamera.getType());
                CameraSelect.this.mIPCameraManager.setCC4In1Config(CameraSelect.this.mHttpResponseMessenger, CameraSelect.this.mCurrentCamera);
                Utilities.showProgressDialog(CameraSelect.this, null, CameraSelect.this.getResources().getText(R.string.str_camera_connecting), false, false);
                return;
            }
            CameraSelect.this.showOneButtonDialog(DataProviderHelper.MODEL_TYPE_GLIDER_BASE, R.string.str_wifi_closed_title, R.string.str_wifi_closed);
        }
    };
    private CameraArrayAdapter mCameraArrayAdapter;
    private ArrayList<Cameras> mCamerasList = new ArrayList();
    private Cameras mCurrentCamera = null;
    private HttpRequestHandler mHttpHandler = new HttpRequestHandler(this);
    private Messenger mHttpResponseMessenger = new Messenger(this.mHttpHandler);
    private IPCameraManager mIPCameraManager;
    private InputStream mInputStream;
    private SharedPreferences mPrefs;
    private CameraParser parser;

    private class CameraArrayAdapter extends ArrayAdapter<Cameras> {
        private LayoutInflater mInflater;
        private int mResource;

        public CameraArrayAdapter(Context context, int textViewResourceId, List<Cameras> objects) {
            super(context, textViewResourceId, objects);
            this.mResource = textViewResourceId;
            this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = this.mInflater.inflate(this.mResource, parent, false);
            } else {
                view = convertView;
            }
            try {
                ((TextView) view).setText(((Cameras) getItem(position)).getName());
                return view;
            } catch (ClassCastException e) {
                Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
                throw new IllegalStateException("ArrayAdapter requires the resource ID to be a TextView", e);
            }
        }
    }

    private class HttpRequestHandler extends WeakHandler<CameraSelect> {
        public HttpRequestHandler(CameraSelect owner) {
            super(owner);
        }

        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case 12:
                    Log.v(CameraSelect.TAG, " ------ resp   " + msg.obj);
                    if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(msg.obj)) {
                        Utilities.dismissProgressDialog();
                        CameraSelect.this.showOneButtonDialog(380, R.string.str_set_status_title, R.string.str_set_complete);
                        CameraSelect.this.mPrefs.edit().putInt(FlightSettings.CAMERA_TYPE_VALUE, CameraSelect.this.current_type).commit();
                        CameraSelect.this.mPrefs.edit().putInt(FlightSettings.CAMERA_SELECTED_POSITION_VALUE, CameraSelect.this.current_positon).commit();
                        return;
                    }
                    Utilities.dismissProgressDialog();
                    CameraSelect.this.showOneButtonDialog(380, R.string.str_set_status_title, R.string.str_set_failure);
                    return;
                case 24:
                    if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(msg.obj)) {
                        Log.i(CameraSelect.TAG, "Init camera complete");
                        return;
                    } else {
                        MyToast.makeText(CameraSelect.this, CameraSelect.this.getString(R.string.init_camera_failed), 0, 1);
                        return;
                    }
                default:
                    Utilities.dismissProgressDialog();
                    return;
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(128);
        setContentView(R.layout.camera_select_main);
        this.mPrefs = getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0);
        this.current_type = this.mPrefs.getInt(FlightSettings.CAMERA_TYPE_VALUE, getResources().getInteger(R.integer.def_camera_type_value));
        this.current_positon = this.mPrefs.getInt(FlightSettings.CAMERA_SELECTED_POSITION_VALUE, getResources().getInteger(R.integer.def_camera_type_position));
        this.camera_name = (TextView) findViewById(R.id.camera_name);
        this.camera_type = (TextView) findViewById(R.id.camera_type);
        this.choose_camera_selected = (Button) findViewById(R.id.choose_camera_selected);
        this.choose_camera_list = (ListView) findViewById(R.id.choose_camera_list);
        loadXMLData();
        this.mCameraArrayAdapter = new CameraArrayAdapter(this, 17367062, this.mCamerasList);
        this.choose_camera_list.setAdapter(this.mCameraArrayAdapter);
        this.choose_camera_list.setOnItemClickListener(this.adapterOnItemClickListener);
        this.choose_camera_selected.setOnClickListener(this.mButtonOnClickListener);
    }

    private void loadXMLData() {
        try {
            if (Utilities.PROJECT_TAG.equals("ST12")) {
                this.mInputStream = getAssets().open("camera_command_ST12.xml");
            } else if (Utilities.PROJECT_TAG.equals(Utilities.PROJECT_TAG)) {
                this.mInputStream = getAssets().open("camera_command_ST10.xml");
            }
            this.parser = new PullCameraParser();
            this.cameras = this.parser.parse(this.mInputStream);
            for (Cameras camera : this.cameras) {
                this.mCamerasList.add(camera);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static int parseType(String type) {
        int flag = 0;
        String[] strings = type.split(",");
        int i = 0;
        while (i < strings.length) {
            if (strings[i].equals("cc4in1") || strings[i].equals("lk58")) {
                flag |= 1;
            } else if (strings[i].equals("remotezoom")) {
                flag |= 2;
            } else if (strings[i].equals("amba_cgo2")) {
                flag |= 4;
            } else if (strings[i].equals("amba_cgo3")) {
                flag |= 8;
            } else if (strings[i].equals("minilk58")) {
                flag |= 16;
            } else if (strings[i].equals("amba_cgo3_pro")) {
                flag |= 32;
            }
            i++;
        }
        return flag;
    }

    private String getCurrentmCurrentCameraTypeString(Cameras cameraInfo) {
        return "name:" + cameraInfo.getName() + "@" + "dr" + ":" + cameraInfo.getDr() + "@" + "f" + ":" + cameraInfo.getF() + "@" + "n" + ":" + cameraInfo.getN() + "@" + "t1" + ":" + cameraInfo.getT1() + "@" + "t2" + ":" + cameraInfo.getT2() + "@" + "intervalp" + ":" + cameraInfo.getIntervalp() + "@" + "codep" + ":" + cameraInfo.getCodep() + "@" + "intervalv" + ":" + cameraInfo.getIntervalv() + "@" + "codev" + ":" + cameraInfo.getCodev();
    }

    public void onResume() {
        super.onResume();
        if (this.mCamerasList.size() > 0) {
            this.mCurrentCamera = (Cameras) this.mCamerasList.get(this.current_positon);
            this.choose_camera_list.setItemChecked(this.current_positon, true);
            this.camera_name.setText(this.mCurrentCamera.getName());
            this.camera_type.setText(this.mCurrentCamera.getType());
            this.mPrefs.edit().putString(FlightSettings.CAMERA_CURRENT_SELECTED, this.mCurrentCamera.getName()).commit();
            if ((this.current_type & 1) == 1) {
                this.mIPCameraManager = IPCameraManager.getIPCameraManager(this, 101);
                Log.v(TAG, " -- IPCameraManager  current type is cc4in1");
            } else if ((this.current_type & 4) == 4) {
                this.mIPCameraManager = IPCameraManager.getIPCameraManager(this, 102);
                Log.v(TAG, " -- IPCameraManager  current type is amba");
            } else if ((this.current_type & 8) == 8 || (this.current_type & 32) == 32) {
                this.mIPCameraManager = IPCameraManager.getIPCameraManager(this, 104);
            } else if ((this.current_type & 16) == 16) {
                this.mIPCameraManager = IPCameraManager.getIPCameraManager(this, 105);
            } else {
                this.mIPCameraManager = IPCameraManager.getIPCameraManager(this, 100);
                Log.v(TAG, " -- IPCameraManager  current type is dm368");
            }
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mIPCameraManager != null) {
            this.mIPCameraManager.finish();
        }
        this.mIPCameraManager = null;
    }

    private void showOneButtonDialog(int height, int titleResId, int messageResId) {
        final OneButtonPopDialog dialog = new OneButtonPopDialog(this);
        dialog.adjustHeight(height);
        dialog.setMessageGravity(17);
        dialog.setTitle(titleResId);
        dialog.setMessage(messageResId);
        dialog.setPositiveButton(17039370, new OnClickListener() {
            public void onClick(View v) {
                CameraSelect.this.mIPCameraManager.initCamera(CameraSelect.this.mHttpResponseMessenger);
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
