package com.yuneec.flight_settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.yuneec.IPCameraManager.IPCameraManager;
import com.yuneec.database.DBOpenHelper;
import com.yuneec.database.DataProvider;
import com.yuneec.database.DataProviderHelper;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;
import com.yuneec.flightmode15.Utilities.ReceiverInfomation;
import com.yuneec.model_select.ModelSelectMain;
import com.yuneec.uartcontroller.UARTController;
import com.yuneec.uartcontroller.UARTInfoMessage;
import com.yuneec.uartcontroller.UARTInfoMessage.RxBindInfo;
import com.yuneec.uartcontroller.UARTInfoMessage.UARTRelyMessage;
import com.yuneec.widget.BaseDialog;
import com.yuneec.widget.MyProgressDialog;
import com.yuneec.widget.MyToast;
import com.yuneec.widget.OneButtonPopDialog;
import com.yuneec.widget.TwoButtonPopDialog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Bind extends Activity {
    private static final int BIND_MODEL_FIRST_TIMEOUT = 800;
    private static final int BIND_MODEL_TIMEOUT = 500;
    private static final int MODEL_SELECT_REQ = 1001;
    private static final int REFRESH_MSG = 291;
    private static final String TAG = "Bind";
    private WifiArrayAdapter FPVWifiListArrayAdapter = null;
    private OnItemClickListener adapterOnItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (parent.getId()) {
                case R.id.bind_model_list:
                    RxBindInfo rx = (RxBindInfo) parent.getItemAtPosition(position);
                    if (Bind.this.checkRxCompatibility(rx)) {
                        Bind.this.mCurrentSelectedModel = rx;
                        Bind.this.bindRC();
                        return;
                    }
                    return;
                case R.id.bind_camera_list:
                    Bind.this.mCurrentFPVItem = (ScanResult) parent.getItemAtPosition(position);
                    if (Bind.this.mCurrentFPVItem != null) {
                        Bind.this.bindFPV();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private ListView bind_camera_list;
    private ListView bind_model_list;
    private BindWifiManage bwm;
    private String fpv_ssid_password = new String();
    private boolean isAuthenticationError;
    private boolean isModelBound = false;
    private boolean isWifiConnect;
    private TextView mAicraftCameraHeader;
    private TextView mAicraftModelHeader;
    private int mBindModelRetryTimes = 0;
    private Runnable mBindModelTimeoutRunnable = new Runnable() {
        private static final int RETRY_TIMES = 3;

        public void run() {
            if (Bind.this.isModelBound) {
                Log.i(Bind.TAG, "model has been bound,timeout ignored");
            } else if (Bind.this.mController == null) {
                Log.i(Bind.TAG, "user quits,don't care the timeout of the bind");
                if (Bind.this.mProgressDialog != null) {
                    Bind.this.dismissProgressDialog();
                }
            } else {
                int state = Bind.this.mController.queryBindState();
                Log.d(Bind.TAG, "M4 bind state: " + state);
                if (state > 0 && state == Bind.this.getRxAddr()) {
                    Bind.this.afterBindModel();
                } else if (Bind.this.mBindModelRetryTimes >= 3) {
                    Log.w(Bind.TAG, "too many attempts,bind failed");
                    Bind.this.dismissProgressDialog();
                    Bind.this.showResult("Bind Model Failed");
                } else {
                    int rxAddr = Bind.this.getRxAddr();
                    if (rxAddr == -1) {
                        Log.e(Bind.TAG, "mBindModelTimeoutRunnable shoud never be here");
                        Bind.this.dismissProgressDialog();
                        Bind.this.showResult("Bind Model Failed");
                        return;
                    }
                    Bind.this.mController.unbind(true, 3);
                    Bind.this.mController.bind(false, rxAddr);
                    Bind bind = Bind.this;
                    bind.mBindModelRetryTimes = bind.mBindModelRetryTimes + 1;
                    Bind.this.mHandler.postDelayed(Bind.this.mBindModelTimeoutRunnable, 500);
                }
            }
        }
    };
    private UARTController mController;
    private ScanResult mCurrentFPVItem = null;
    private int mCurrentRXType = -1;
    private RxBindInfo mCurrentSelectedModel = null;
    private final OnClickListener mGotoModelSelectListener = new OnClickListener() {
        public void onClick(View v) {
            Bind.this.startActivityForResult(new Intent(Bind.this, ModelSelectMain.class), 1001);
        }
    };
    private Handler mHandler = new Handler();
    private IPCameraManager mIPCameraManager;
    private InputStream mInputStream;
    private IntentFilter mIntentFilter = new IntentFilter();
    private SharedPreferences mPrefs;
    private MyProgressDialog mProgressDialog;
    private RxBindArrayAdapter mRxAdapter = null;
    private ArrayList<RxBindInfo> mRxList = new ArrayList();
    private ArrayList<ScanResult> mScanFPVWifiResultList = new ArrayList();
    private Handler mUartHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.obj instanceof UARTInfoMessage) {
                UARTInfoMessage umsg = msg.obj;
                if (umsg.what == 11) {
                    RxBindInfo rx = (RxBindInfo) umsg;
                    Log.d(Bind.TAG, "bind info, rxAddr: " + rx.node_id + ", txAddr: " + rx.tx_addr);
                    for (int i = 0; i < Bind.this.mRxAdapter.getCount(); i++) {
                        if (((RxBindInfo) Bind.this.mRxAdapter.getItem(i)).node_id == rx.node_id) {
                            Bind.this.mRxAdapter.remove((RxBindInfo) Bind.this.mRxAdapter.getItem(i));
                        }
                    }
                    Bind.this.mRxAdapter.add(rx);
                    Bind.this.bind_model_list.invalidateViews();
                } else if (umsg.what == UARTRelyMessage.REPLY_BIND_INFO) {
                    Log.d(Bind.TAG, "receiver bind rsp");
                    if (!Bind.this.isModelBound) {
                        Bind.this.mHandler.removeCallbacks(Bind.this.mBindModelTimeoutRunnable);
                        if (Bind.this.getRxAddr() > 0) {
                            Bind.this.afterBindModel();
                            return;
                        }
                        Log.e(Bind.TAG, "Receiver bind rsp ERROR: No RxAddr!");
                        Bind.this.dismissProgressDialog();
                        Bind.this.showResult("Bind Model Failed");
                    }
                }
            }
        }
    };
    private Runnable mWifiEnabledRunnable = new Runnable() {
        public void run() {
            Bind.this.refreshWiFiList();
        }
    };
    private Runnable mWifiTimoutRunnable = new Runnable() {
        int i = 0;

        public void run() {
            SupplicantState conn_state = Bind.this.wifiManager.getConnectionInfo().getSupplicantState();
            if (Bind.this.isAuthenticationError) {
                this.i = 0;
                Bind.this.dismissProgressDialog();
                Bind.this.mHandler.removeCallbacks(Bind.this.mWifiTimoutRunnable);
                Bind.this.showResult("Unable to connect to Camera");
            } else if (conn_state.equals(SupplicantState.COMPLETED)) {
                this.i = 0;
                Bind.this.isWifiConnect = true;
                Bind.this.dismissProgressDialog();
                Bind.this.mHandler.removeCallbacks(Bind.this.mWifiTimoutRunnable);
                if (406 == Bind.this.modeType) {
                    Bind.this.mPrefs.edit().putInt(FlightSettings.CAMERA_TYPE_VALUE, CameraSelect.parseType("amba_cgo3_pro")).commit();
                    Bind.this.mPrefs.edit().putInt(FlightSettings.CAMERA_SELECTED_POSITION_VALUE, 4).commit();
                }
                Bind.this.showResult("success");
            } else {
                if (Bind.this.mProgressDialog != null && Bind.this.mProgressDialog.isShowing()) {
                    Bind.this.mProgressDialog.setMessage(Bind.this.getResources().getText(R.string.str_bind_connecting) + "\n");
                }
                if (this.i < DataProviderHelper.MODEL_TYPE_GLIDER_BASE) {
                    this.i++;
                    Bind.this.mHandler.postDelayed(Bind.this.mWifiTimoutRunnable, 100);
                    return;
                }
                this.i = 0;
                Bind.this.dismissProgressDialog();
                Bind.this.mHandler.removeCallbacks(Bind.this.mWifiTimoutRunnable);
                Bind.this.showResult("Unable to connect to Camera");
            }
        }
    };
    private long modeType;
    private boolean noModel;
    private FileOutputStream outStream;
    private BroadcastReceiver receiverWifi = new BroadcastReceiver() {
        private SupplicantState mPreviousState = null;

        public void onReceive(Context context, Intent intent) {
            if (Bind.this.wifiManager.isWifiEnabled()) {
                String action = intent.getAction();
                Log.i(Bind.TAG, action);
                if (action.equals("android.net.wifi.SCAN_RESULTS")) {
                    Bind.this.mHandler.post(new Runnable() {
                        public void run() {
                            Bind.this.initFPVWifiSSIDList();
                        }
                    });
                } else if (action.equals("android.net.wifi.supplicant.STATE_CHANGE")) {
                    SupplicantState ss = (SupplicantState) intent.getParcelableExtra("newState");
                    int err_state = intent.getIntExtra("supplicantError", -1);
                    String err = String.valueOf(err_state);
                    Log.i(Bind.TAG, "--SUPPLICANT_STATE_CHANGED_ACTION:" + ss.name());
                    Log.i(Bind.TAG, " -- " + err);
                    if (err_state == 1) {
                        Bind.this.isAuthenticationError = true;
                        this.mPreviousState = null;
                        return;
                    }
                    if (this.mPreviousState == null) {
                        this.mPreviousState = ss;
                    } else if (this.mPreviousState.equals(SupplicantState.FOUR_WAY_HANDSHAKE) && ss.equals(SupplicantState.DISCONNECTED)) {
                        Bind.this.isAuthenticationError = true;
                    }
                    if (this.mPreviousState.equals(SupplicantState.COMPLETED)) {
                        this.mPreviousState = null;
                    }
                } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED") && intent.getIntExtra("wifi_state", 4) == 3) {
                    Bind.this.mHandler.postDelayed(Bind.this.mWifiEnabledRunnable, 5000);
                }
            }
        }
    };
    private Runnable refreshRunable = new Runnable() {
        public void run() {
            Message msg = new Message();
            msg.what = Bind.REFRESH_MSG;
            Bind.this.refreshThreadHandler.sendMessage(msg);
            Bind.this.refreshThreadHandler.postDelayed(Bind.this.refreshRunable, 300);
        }
    };
    private Handler refreshThreadHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == Bind.REFRESH_MSG && !Bind.this.noModel) {
                Bind.this.refreshAllList();
            }
        }
    };
    WifiConnect wc = null;
    WifiCipherType wct = null;
    private WifiManager wifiManager;
    private List<WifiPassword> wifiPasswords = new ArrayList();

    private class RxBindArrayAdapter extends ArrayAdapter<RxBindInfo> {
        private LayoutInflater mInflater;
        private int mResource;

        public RxBindArrayAdapter(Context context, int textViewResourceId, List<RxBindInfo> objects) {
            super(context, textViewResourceId, objects);
            this.mResource = textViewResourceId;
            this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            boolean z = true;
            if (convertView == null) {
                view = this.mInflater.inflate(this.mResource, parent, false);
            } else {
                view = convertView;
            }
            try {
                TextView text = (TextView) view;
                RxBindInfo item = (RxBindInfo) getItem(position);
                if (item.mode == 0) {
                    text.setText("SR12S_" + String.valueOf(item.node_id));
                } else if (item.mode == 1) {
                    text.setText("SR12E_" + String.valueOf(item.node_id));
                } else if (item.mode == 2) {
                    text.setText("SR24S_" + String.valueOf(item.node_id) + "v1.03");
                } else if (item.mode == 3) {
                    text.setText("RX24_" + String.valueOf(item.node_id));
                } else if (item.mode >= 105) {
                    text.setText("SR24S_" + String.format("%dv%.2f", new Object[]{Integer.valueOf(item.node_id), Float.valueOf(((float) item.mode) / 100.0f)}));
                } else {
                    text.setText(String.valueOf(item.node_id));
                }
                if (406 == Bind.this.modeType) {
                    z = false;
                }
                view.setEnabled(z);
                return view;
            } catch (ClassCastException e) {
                Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
                throw new IllegalStateException("ArrayAdapter requires the resource ID to be a TextView", e);
            }
        }
    }

    private class SyncModelDataTask extends AsyncTask<Long, Void, Void> {
        private SyncModelDataTask() {
        }

        protected Void doInBackground(Long... params) {
            long model_id = params[0].longValue();
            Utilities.sendAllDataToFlightControl(Bind.this, model_id, Bind.this.mController);
            Utilities.sendRxResInfoFromDatabase(Bind.this, Bind.this.mController, model_id);
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Bind.this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    Bind.this.dismissProgressDialog();
                    Bind.this.showResult("success");
                }
            }, 500);
        }
    }

    private class WifiArrayAdapter extends ArrayAdapter<ScanResult> {
        private LayoutInflater mInflater;
        private int mResource;

        public WifiArrayAdapter(Context context, int textViewResourceId, List<ScanResult> objects) {
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
                ((TextView) view).setText(String.valueOf(((ScanResult) getItem(position)).SSID));
                return view;
            } catch (ClassCastException e) {
                Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
                throw new IllegalStateException("ArrayAdapter requires the resource ID to be a TextView", e);
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(Utilities.FLAG_HOMEKEY_DISPATCHED);
        getWindow().addFlags(128);
        this.mPrefs = getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0);
        long model_id = getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0).getLong("current_model_id", -2);
        this.modeType = this.mPrefs.getLong("current_model_type", 0);
        this.mCurrentRXType = getCurrentRXType(model_id);
        if (model_id == -2) {
            setContentView(R.layout.bind_no_model);
            findViewById(R.id.go_to_model_select).setOnClickListener(this.mGotoModelSelectListener);
            this.noModel = true;
            return;
        }
        setContentView(R.layout.bind_main);
        this.bind_model_list = (ListView) findViewById(R.id.bind_model_list);
        this.bind_camera_list = (ListView) findViewById(R.id.bind_camera_list);
        setListHeaderView();
        initListAdapter();
        initWifi();
        try {
            loadXMLData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.bind_camera_list.setOnItemClickListener(this.adapterOnItemClickListener);
        if (406 == this.modeType) {
            this.bind_model_list.setEnabled(false);
        } else {
            this.bind_model_list.setOnItemClickListener(this.adapterOnItemClickListener);
        }
    }

    @SuppressLint({"ResourceAsColor"})
    private void setListHeaderView() {
        this.mAicraftModelHeader = (TextView) LayoutInflater.from(this).inflate(R.layout.bind_list_header, null).findViewById(R.id.list_header_text);
        this.mAicraftModelHeader.setText(R.string.str_bind_list_header_not_connected);
        if (406 == this.modeType) {
            this.mAicraftModelHeader.setTextColor(-13417421);
        }
        this.bind_model_list.addHeaderView(this.mAicraftModelHeader, null, false);
        this.mAicraftCameraHeader = (TextView) LayoutInflater.from(this).inflate(R.layout.bind_list_header, null).findViewById(R.id.list_header_text);
        this.mAicraftCameraHeader.setText(R.string.str_bind_list_header_not_connected);
        this.bind_camera_list.addHeaderView(this.mAicraftCameraHeader, null, false);
    }

    private void initListAdapter() {
        this.mRxAdapter = new RxBindArrayAdapter(this, 17367062, this.mRxList);
        this.bind_model_list.setAdapter(this.mRxAdapter);
        this.FPVWifiListArrayAdapter = new WifiArrayAdapter(this, 17367062, this.mScanFPVWifiResultList);
        this.bind_camera_list.setAdapter(this.FPVWifiListArrayAdapter);
    }

    private void refreshListHeaderView() {
        WifiInfo connectionInfo = this.wifiManager.getConnectionInfo();
        String mConnectSSID = connectionInfo.getSSID();
        if (this.isWifiConnect) {
            mConnectSSID = connectionInfo.getSSID();
        } else if (connectionInfo.getNetworkId() == -1 || connectionInfo.getIpAddress() == 0) {
            mConnectSSID = (String) getText(R.string.str_bind_list_header_not_connected);
        }
        if (mConnectSSID == null) {
            mConnectSSID = (String) getText(R.string.str_bind_list_header_not_connected);
        }
        if (TextUtils.isEmpty(mConnectSSID)) {
            mConnectSSID = (String) getText(R.string.str_bind_list_header_not_connected);
        }
        this.mAicraftCameraHeader.setText(mConnectSSID);
    }

    private void refreshModelHeaderView() {
        if (this.mController != null) {
            long model_id = getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0).getLong("current_model_id", -2);
            this.mCurrentRXType = getCurrentRXType(model_id);
            String strRxAddress = getCurrentRXNodeId(model_id);
            if (strRxAddress != null && "0".equals(strRxAddress)) {
                strRxAddress = (String) getText(R.string.str_bind_list_header_not_connected);
            }
            if (this.mCurrentRXType == -1) {
                this.mAicraftModelHeader.setText((String) getText(R.string.str_bind_list_header_not_connected));
            } else if (this.mCurrentRXType == 0) {
                if (strRxAddress.contains(getText(R.string.str_bind_list_header_not_connected))) {
                    this.mAicraftModelHeader.setText(strRxAddress);
                } else {
                    this.mAicraftModelHeader.setText("SR12S_" + strRxAddress);
                }
            } else if (this.mCurrentRXType == 1) {
                if (strRxAddress.contains(getText(R.string.str_bind_list_header_not_connected))) {
                    this.mAicraftModelHeader.setText(strRxAddress);
                } else {
                    this.mAicraftModelHeader.setText("SR12E_" + strRxAddress);
                }
            } else if (this.mCurrentRXType == 2) {
                if (strRxAddress.contains(getText(R.string.str_bind_list_header_not_connected))) {
                    this.mAicraftModelHeader.setText(strRxAddress);
                } else {
                    this.mAicraftModelHeader.setText("SR24S_" + strRxAddress + "v1.03");
                }
            } else if (this.mCurrentRXType == 3) {
                if (strRxAddress.contains(getText(R.string.str_bind_list_header_not_connected))) {
                    this.mAicraftModelHeader.setText(strRxAddress);
                } else {
                    this.mAicraftModelHeader.setText("RX24_" + strRxAddress);
                }
            } else if (this.mCurrentRXType < 105) {
                this.mAicraftModelHeader.setText(strRxAddress);
            } else if (strRxAddress.contains(getText(R.string.str_bind_list_header_not_connected))) {
                this.mAicraftModelHeader.setText(strRxAddress);
            } else {
                this.mAicraftModelHeader.setText("SR24S_" + strRxAddress + String.format("v%.2f", new Object[]{Float.valueOf(((float) this.mCurrentRXType) / 100.0f)}));
            }
        }
    }

    private void initWifi() {
        this.mIntentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        this.mIntentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.mIntentFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
        this.wifiManager = (WifiManager) getSystemService("wifi");
        this.bwm = new BindWifiManage(this.wifiManager);
    }

    private void refreshAllList() {
        if (this.mController == null) {
            this.mController = UARTController.getInstance();
            this.mController.registerReaderHandler(this.mUartHandler);
            this.mController.startReading();
            if (!Utilities.ensureBindState(this.mController)) {
                Log.e(TAG, "fails to enter bind state");
            }
        }
        this.mController.startBind(false);
        refreshWiFiList();
        refreshModelList();
    }

    private boolean checkRxCompatibility(RxBindInfo rx) {
        long model_id = getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0).getLong("current_model_id", -2);
        ReceiverInfomation rxInfo = Utilities.getReceiverInfo(this, model_id);
        Cursor c = getContentResolver().query(ContentUris.withAppendedId(DataProvider.MODEL_URI, model_id), new String[]{DBOpenHelper.KEY_ID, DBOpenHelper.KEY_NAME}, null, null, null);
        if (DataProviderHelper.isCursorValid(c)) {
            String model_name = "\"" + c.getString(c.getColumnIndex(DBOpenHelper.KEY_NAME)) + "\"";
            c.close();
            if (rxInfo.receiver == 0) {
                if (rxInfo.analogChNumber == rx.a_num && rxInfo.switchChNumber == rx.sw_num) {
                    Log.i(TAG, "User pre-defined rx info check OK");
                    return true;
                } else if (rxInfo.analogChNumber != 0 || rxInfo.switchChNumber != 0) {
                    return true;
                } else {
                    Log.i(TAG, "An empty model...save rx info");
                    if (rx.a_num < rxInfo.analogChNumber_min || rx.sw_num < rxInfo.switchChNumber_min) {
                        Log.i(TAG, "rx incompatibility minimum:" + rx.a_num + "," + rx.sw_num + ",require:" + rxInfo.analogChNumber_min + "," + rxInfo.switchChNumber_min);
                        showNoMatchDialog(getResources().getString(R.string.str_bind_rx_not_match_message_min_ch, new Object[]{Integer.valueOf(rxInfo.analogChNumber_min), Integer.valueOf(rxInfo.switchChNumber_min), Integer.valueOf(rx.a_num), Integer.valueOf(rx.sw_num), model_name}));
                        return false;
                    } else if (rx.a_num + rx.sw_num <= 12) {
                        return true;
                    } else {
                        Log.i(TAG, "rx incompatibility:" + rx.a_num + "," + rx.sw_num);
                        showIncompatibilityDialog(model_id, rx, getResources().getString(R.string.str_bind_rx_incompatibility_message, new Object[]{Integer.valueOf(10), Integer.valueOf(2), Integer.valueOf(rx.a_num), Integer.valueOf(rx.sw_num), Integer.valueOf(12)}));
                        return false;
                    }
                }
            } else if (rxInfo.receiver == rx.node_id) {
                int rx_analog_ch;
                int rx_switch_ch;
                if (rx.a_num + rx.sw_num > 12) {
                    rx_analog_ch = rx.a_num > 10 ? 10 : rx.a_num;
                    rx_switch_ch = rx.sw_num > 2 ? 2 : rx.sw_num;
                } else {
                    rx_analog_ch = rx.a_num;
                    rx_switch_ch = rx.sw_num;
                }
                if (rxInfo.analogChNumber != rx_analog_ch || rxInfo.switchChNumber != rx_switch_ch) {
                    return true;
                }
                Log.i(TAG, "rx params checked OK");
                return true;
            } else {
                Log.i(TAG, "rx address " + rxInfo.receiver + " not matched with saved " + rx.node_id);
                showNoMatchDialog(getResources().getString(R.string.str_bind_rx_not_match_message_addr, new Object[]{Integer.valueOf(rxInfo.receiver), Integer.valueOf(rx.node_id), model_name}));
                return false;
            }
        }
        Log.e(TAG, "Cann't get model name");
        return false;
    }

    private void saveRxInfo(long model_id, RxBindInfo rx) {
        Uri uri = ContentUris.withAppendedId(DataProvider.MODEL_URI, model_id);
        ContentValues cv = new ContentValues();
        cv.put(DBOpenHelper.KEY_RX, Integer.valueOf(rx.node_id));
        cv.put(DBOpenHelper.KEY_RX_ANALOG_NUM, Integer.valueOf(rx.a_num));
        cv.put(DBOpenHelper.KEY_RX_ANALOG_BIT, Integer.valueOf(rx.a_bit));
        cv.put(DBOpenHelper.KEY_RX_SWITCH_NUM, Integer.valueOf(rx.sw_num));
        cv.put(DBOpenHelper.KEY_RX_SWITCH_BIT, Integer.valueOf(rx.sw_bit));
        cv.put(DBOpenHelper.KEY_RX_TYPE, Integer.valueOf(rx.mode));
        cv.put(DBOpenHelper.KEY_PAN_ID, Integer.valueOf(rx.pan_id));
        cv.put(DBOpenHelper.KEY_TX_ADDR, Integer.valueOf(rx.tx_addr));
        getContentResolver().update(uri, cv, null, null);
    }

    private void saveCameraInfo(String ssid, String password, String capabilities) {
        if (this.mCurrentFPVItem != null) {
            try {
                addXMLData(ssid, password);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Utilities.saveWifiInfoToDatabase(this, getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0).getLong("current_model_id", -2), new StringBuilder(String.valueOf(ssid)).append(",").append(password).append(",").append(new WifiCipherType(capabilities).getType()).toString());
        }
    }

    private void saveRxResInfo(long model_id) {
        Utilities.saveRxResInfoToDatabase(this, this.mController, model_id, getRxAddr());
    }

    private void showIncompatibilityDialog(long model_id, RxBindInfo rx, String message) {
        int tx_analog_max = 10;
        int tx_switch_max = 2;
        RxBindInfo compatiable_rx = new RxBindInfo();
        compatiable_rx.mode = rx.mode;
        compatiable_rx.pan_id = rx.pan_id;
        compatiable_rx.node_id = rx.node_id;
        compatiable_rx.a_bit = rx.a_bit;
        compatiable_rx.sw_bit = rx.sw_bit;
        if (rx.a_num <= 10) {
            tx_analog_max = rx.a_num;
        }
        compatiable_rx.a_num = tx_analog_max;
        if (rx.sw_num <= 2) {
            tx_switch_max = rx.sw_num;
        }
        compatiable_rx.sw_num = tx_switch_max;
        saveRxInfo(model_id, compatiable_rx);
        this.mCurrentSelectedModel = compatiable_rx;
    }

    private void showNoMatchDialog(String message, int str_positive, int str_negative) {
        int resId_pos;
        int resId_neg;
        final TwoButtonPopDialog dialog = new TwoButtonPopDialog(this);
        if (str_positive == 0) {
            resId_pos = R.string.str_bind_rx_create_model;
        } else {
            resId_pos = str_positive;
        }
        if (str_negative == 0) {
            resId_neg = 17039360;
        } else {
            resId_neg = str_negative;
        }
        dialog.setTitle((int) R.string.str_bind_rx_not_match_title);
        dialog.setMessage((CharSequence) message);
        dialog.setPositiveButton(resId_pos, new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                Bind.this.bind_model_list.setItemChecked(-1, true);
                Bind.this.startActivity(new Intent(Bind.this, ModelSelectMain.class));
            }
        });
        dialog.setNegativeButton(resId_neg, new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                Bind.this.bind_model_list.setItemChecked(-1, true);
            }
        });
        dialog.show();
    }

    private void showNoMatchDialog(String message) {
        showNoMatchDialog(message, 0, 0);
    }

    public void refreshWiFiList() {
        if (!this.bwm.getWifiStatus()) {
            this.bwm.openWifi();
        }
        this.bwm.startScan();
        initFPVWifiSSIDList();
    }

    private boolean isItemContainsString(String item, String[] str) {
        if (item == null || str.length <= 0) {
            return false;
        }
        for (String startsWith : str) {
            if (item.startsWith(startsWith)) {
                return true;
            }
        }
        return false;
    }

    private void initFPVWifiSSIDList() {
        List<ScanResult> list = this.bwm.getWifiList();
        if (list != null) {
            String[] str = getResources().getStringArray(R.array.bind_wifi_camera_contents_strings);
            this.mScanFPVWifiResultList.clear();
            int i = 0;
            while (i < list.size()) {
                if (isItemContainsString(((ScanResult) list.get(i)).SSID, str) && !this.mScanFPVWifiResultList.contains(list.get(i))) {
                    this.mScanFPVWifiResultList.add((ScanResult) list.get(i));
                }
                i++;
            }
            refreshListHeaderView();
            this.FPVWifiListArrayAdapter.notifyDataSetChanged();
            this.bind_camera_list.invalidateViews();
        }
    }

    private void refreshModelList() {
        int i = 0;
        while (i < this.mRxAdapter.getCount()) {
            if (this.mCurrentSelectedModel != null && ((RxBindInfo) this.mRxAdapter.getItem(i)).node_id == this.mCurrentSelectedModel.node_id) {
                this.mRxAdapter.remove((RxBindInfo) this.mRxAdapter.getItem(i));
            }
            i++;
        }
        refreshListHeaderView();
        this.mRxAdapter.notifyDataSetChanged();
        this.bind_model_list.invalidateViews();
    }

    private void connectType() {
        this.wc = new WifiConnect(this.wifiManager);
        this.wct = new WifiCipherType(this.mCurrentFPVItem.capabilities);
        this.wc.Connect(this.mCurrentFPVItem.SSID, this.fpv_ssid_password, this.wct.getType());
    }

    private void connectWifi() {
        this.wifiManager.disableNetwork(this.wifiManager.getConnectionInfo().getNetworkId());
        connectType();
        this.mHandler.post(this.mWifiTimoutRunnable);
    }

    private void showResult(String result) {
        CharSequence hint;
        boolean success = false;
        if ("success".equals(result)) {
            hint = getResources().getString(R.string.str_bind_success, new Object[]{" "});
            success = true;
            this.isWifiConnect = true;
            refreshModelHeaderView();
        } else {
            hint = getResources().getString(R.string.str_bind_failed, new Object[]{result});
            this.mController.finishBind(false);
            this.mCurrentSelectedModel = null;
        }
        if (this.isWifiConnect && this.fpv_ssid_password != null) {
            try {
                saveCameraInfo(this.mCurrentFPVItem.SSID, this.fpv_ssid_password, this.mCurrentFPVItem.capabilities);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        refreshListHeaderView();
        if (this.isModelBound) {
            this.mRxList.remove(this.mCurrentSelectedModel);
            this.bind_model_list.invalidateViews();
        }
        if (hint != null) {
            final OneButtonPopDialog dialog = new OneButtonPopDialog(this);
            if (success) {
                dialog.adjustHeight(380);
                dialog.setMessageGravity(17);
            } else {
                try {
                    if (this.mCurrentFPVItem != null) {
                        removeXMLData(this.mCurrentFPVItem.SSID);
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            dialog.setTitle((int) R.string.str_bind_status);
            dialog.setMessage(hint);
            dialog.setCancelable(false);
            dialog.setPositiveButton(17039370, new OnClickListener() {
                public void onClick(View v) {
                    Bind.this.mIPCameraManager.initCamera(null);
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }

    private void showProgressDialog() {
        this.mProgressDialog = MyProgressDialog.show(this, getResources().getText(R.string.str_bind_connect_title), getResources().getText(R.string.str_bind_connecting), false, false);
    }

    private void showProgressDialog(int titleRes, int msgRes) {
        this.mProgressDialog = MyProgressDialog.show(this, getResources().getText(titleRes), getResources().getText(msgRes), false, false);
    }

    private void dismissProgressDialog() {
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            this.mProgressDialog.dismiss();
        }
    }

    private void showPasswordDailog() {
        final Dialog dialog = new BaseDialog(this, R.style.dialog_style);
        dialog.setContentView(R.layout.bind_login);
        CheckBox show_password = (CheckBox) dialog.findViewById(R.id.show_password);
        final EditText password = (EditText) dialog.findViewById(R.id.ssid_password);
        Button password_ok_button = (Button) dialog.findViewById(R.id.password_ok_button);
        Button password_cancel_button = (Button) dialog.findViewById(R.id.password_cancel_button);
        ((TextView) dialog.findViewById(R.id.ssid_name)).setText(this.mCurrentFPVItem.SSID);
        show_password.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    password.setInputType(145);
                } else {
                    password.setInputType(129);
                }
                password.setSelection(password.getText().length());
            }
        });
        password_ok_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Bind.this.fpv_ssid_password = password.getEditableText().toString();
                InputMethodManager imm = (InputMethodManager) Bind.this.getSystemService("input_method");
                if (imm.isActive()) {
                    imm.toggleSoftInput(1, 2);
                }
                dialog.dismiss();
                Bind.this.bindWifi();
            }
        });
        password_cancel_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                Bind.this.dismissKeyBoard();
            }
        });
        dialog.show();
    }

    public void dismissKeyBoard() {
        ((InputMethodManager) getSystemService("input_method")).toggleSoftInput(1, 2);
    }

    private void afterBindModel() {
        this.isModelBound = true;
        long model_id = getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0).getLong("current_model_id", -2);
        saveRxInfo(model_id, this.mCurrentSelectedModel);
        saveRxResInfo(model_id);
        syncModelData();
    }

    private void syncModelData() {
        if (this.mProgressDialog == null || !this.mProgressDialog.isShowing()) {
            showProgressDialog();
        } else {
            this.mProgressDialog.setMessage(getResources().getString(R.string.str_bind_sync_model_data));
        }
        long modelId = this.mPrefs.getLong("current_model_id", -2);
        new SyncModelDataTask().execute(new Long[]{Long.valueOf(modelId)});
    }

    public void bindWifi() {
        showProgressDialog();
        this.wifiManager.disableNetwork(this.wifiManager.getConnectionInfo().getNetworkId());
        connectType();
        this.mHandler.post(this.mWifiTimoutRunnable);
    }

    private int getRxAddr() {
        if (this.mCurrentSelectedModel != null) {
            return this.mCurrentSelectedModel.node_id;
        }
        return -1;
    }

    public boolean bindRC() {
        int rxAddr = getRxAddr();
        if (rxAddr == -1) {
            MyToast.makeText((Context) this, (int) R.string.str_bind_select_a_rx, 0, 0).show();
            return false;
        }
        if (this.mProgressDialog == null || !this.mProgressDialog.isShowing()) {
            showProgressDialog(R.string.str_bind_connect_title, R.string.str_bind_connecting_rx);
        }
        this.mController.unbind(true, 3);
        this.isModelBound = false;
        this.mController.bind(false, rxAddr);
        this.mBindModelRetryTimes = 0;
        this.mHandler.removeCallbacks(this.mBindModelTimeoutRunnable);
        this.mHandler.postDelayed(this.mBindModelTimeoutRunnable, 800);
        return true;
    }

    public void bindFPV() {
        String str_ssid = this.wifiManager.getConnectionInfo().getSSID();
        boolean isSaveSsid = false;
        if (str_ssid == null) {
            str_ssid = "";
        }
        for (WifiPassword wifiPassword : this.wifiPasswords) {
            if (wifiPassword.getSsid().equals(this.mCurrentFPVItem.SSID)) {
                isSaveSsid = true;
                this.fpv_ssid_password = wifiPassword.getPassword();
            }
        }
        this.wct = new WifiCipherType(this.mCurrentFPVItem.capabilities);
        this.wct = new WifiCipherType(this.mCurrentFPVItem.capabilities);
        if (isSaveSsid && str_ssid.equals(this.mCurrentFPVItem.SSID)) {
            this.isWifiConnect = true;
            showResult("success");
        } else if (isSaveSsid || 2 == this.wct.getType()) {
            bindWifi();
        } else {
            showPasswordDailog();
        }
    }

    public void onStart() {
        super.onStart();
        if (!this.noModel) {
            this.mIPCameraManager = IPCameraManager.getIPCameraManager(this, 102);
            this.mIPCameraManager.initCamera(null);
            registerReceiver(this.receiverWifi, this.mIntentFilter);
        }
    }

    public void onStop() {
        super.onStop();
        if (!this.noModel) {
            this.mHandler.removeCallbacks(this.mWifiTimoutRunnable);
            this.mHandler.removeCallbacks(this.mWifiEnabledRunnable);
            this.mHandler.removeCallbacks(this.mBindModelTimeoutRunnable);
            this.mIPCameraManager.finish();
            this.mIPCameraManager = null;
            unregisterReceiver(this.receiverWifi);
        }
    }

    public void onResume() {
        super.onResume();
        this.refreshThreadHandler.post(this.refreshRunable);
        if (!this.noModel) {
            this.mController = UARTController.getInstance();
            this.mController.registerReaderHandler(this.mUartHandler);
            this.mController.startReading();
            if (!Utilities.ensureBindState(this.mController)) {
                Log.e(TAG, "fails to enter bind state");
            }
            this.mController.startBind(false);
            refreshWiFiList();
            refreshModelHeaderView();
        }
        try {
            loadXMLData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPause() {
        super.onPause();
        this.refreshThreadHandler.removeCallbacks(this.refreshRunable);
        if (!this.noModel) {
            this.mController.finishBind(false);
            if (!Utilities.ensureAwaitState(this.mController)) {
                Log.e(TAG, "fails to enter await state");
            }
            Utilities.UartControllerStandBy(this.mController);
            this.mController = null;
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        this.refreshThreadHandler = null;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            recreate();
        }
    }

    private void loadXMLData() throws Exception {
        this.mInputStream = new FileInputStream(new File(new StringBuilder(String.valueOf(getDir("assets", 0).toString())).append("/wifi_ssid_password.xml").toString()));
        this.wifiPasswords = WifiPasswordService.getWifiPasswords(this.mInputStream);
    }

    private void addXMLData(String ssid, String password) throws Exception {
        for (WifiPassword wifiPassword : this.wifiPasswords) {
            if (wifiPassword.getSsid().equals(ssid)) {
                this.wifiPasswords.remove(wifiPassword);
            }
        }
        this.wifiPasswords.add(new WifiPassword(ssid, password));
        writeXMLData();
    }

    private void removeXMLData(String ssid) throws Exception {
        for (WifiPassword wifiPassword : this.wifiPasswords) {
            if (wifiPassword.getSsid().equals(ssid)) {
                this.wifiPasswords.remove(wifiPassword);
            }
        }
        writeXMLData();
    }

    private void writeXMLData() throws Exception {
        this.outStream = new FileOutputStream(new File(new StringBuilder(String.valueOf(getDir("assets", 0).toString())).append("/wifi_ssid_password.xml").toString()));
        WifiPasswordService.save(this.wifiPasswords, this.outStream);
    }

    private int getCurrentRXType(long model_id) {
        return Utilities.getCurrentRxTypeFromDB(this, model_id);
    }

    private String getCurrentRXNodeId(long model_id) {
        return Utilities.getCurrentRxAddrFromDB(this, model_id);
    }
}
