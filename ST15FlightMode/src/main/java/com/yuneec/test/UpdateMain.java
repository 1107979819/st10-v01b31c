package com.yuneec.test;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
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
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;
import com.yuneec.uartcontroller.UARTController;
import java.io.File;
import java.io.FilenameFilter;

public class UpdateMain extends Activity implements OnClickListener, OnItemClickListener {
    private static final String FIRMWARELOCATION = (Environment.getExternalStorageDirectory() + "/firmware");
    private static final String FIRMWARELOCATION_SD = "/storage/sdcard1/firmware";
    private static final String TAG = "rcfirmwareupdater";
    private Button mBtnFinish;
    private Button mBtnUpdateRF;
    private Button mBtnUpdateTX;
    private FilenameFilter mFirmwareFilter = new FilenameFilter() {
        public boolean accept(File dir, String filename) {
            if (filename == null || filename.lastIndexOf(".bin") == -1) {
                return false;
            }
            return true;
        }
    };
    private Dialog mGuardDialog;
    private ProgressDialog mProgressDialog;
    private ListView mRFFileList;
    private String mSelectedRFFile;
    private TextView mSelectedRFLabel;
    private String mSelectedTXFile;
    private TextView mSelectedTXLabel;
    private ListView mTXFileList;
    private UARTController mUARTController;

    private static class FileAdapter extends ArrayAdapter<File> {
        private int mFieldId;
        private LayoutInflater mInflater;
        private int mResource;

        public FileAdapter(Context context, int resource, int textViewResourceId, File[] objects) {
            super(context, resource, textViewResourceId, objects);
            this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
            this.mResource = resource;
            this.mFieldId = textViewResourceId;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = this.mInflater.inflate(this.mResource, parent, false);
            } else {
                view = convertView;
            }
            try {
                TextView text;
                if (this.mFieldId == 0) {
                    text = (TextView) view;
                } else {
                    text = (TextView) view.findViewById(this.mFieldId);
                }
                text.setText(((File) getItem(position)).getName());
                return view;
            } catch (ClassCastException e) {
                Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
                throw new IllegalStateException("ArrayAdapter requires the resource ID to be a TextView", e);
            }
        }
    }

    private class UpdateTask extends AsyncTask<String, Void, String> {
        private UpdateTask() {
        }

        protected String doInBackground(String... params) {
            String type = params[0];
            String file = params[1];
            if ("TX".equals(type)) {
                return UpdateMain.this.mUARTController.updateTxVersion(file);
            }
            if ("RF".equals(type)) {
                return UpdateMain.this.mUARTController.updateRfVersion(file);
            }
            return UpdateMain.this.getString(R.string.unknown_type);
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            UpdateMain.this.mProgressDialog.dismiss();
            Builder ab = new Builder(UpdateMain.this);
            ab.setTitle(UpdateMain.this.getString(R.string.title_update_result));
            if ("OK".equals(result)) {
                ab.setMessage(UpdateMain.this.getString(R.string.update_successed));
            } else {
                ab.setMessage(new StringBuilder(String.valueOf(UpdateMain.this.getString(R.string.update_failed))).append(result).toString());
            }
            ab.setPositiveButton(17039370, null);
            ab.create().show();
        }
    }

    private File[] loadList(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            return dir.listFiles(this.mFirmwareFilter);
        }
        Log.i(TAG, "dir is not existed :" + FIRMWARELOCATION);
        return null;
    }

    private File[] combineTwoList(File[] list1, File[] list2) {
        if (list1 == null && list2 == null) {
            return new File[0];
        }
        File[] out;
        if (list1 != null) {
            if (list2 != null) {
                out = new File[(list1.length + list2.length)];
            } else {
                out = new File[list1.length];
            }
        } else if (list2 != null) {
            out = new File[list2.length];
        } else {
            out = new File[0];
        }
        int i;
        if (list1 != null) {
            for (i = 0; i < list1.length; i++) {
                out[i] = list1[i];
            }
            if (list2 == null) {
                return out;
            }
            for (i = 0; i < list2.length; i++) {
                out[list1.length + i] = list2[i];
            }
            return out;
        } else if (list2 == null) {
            return out;
        } else {
            for (i = 0; i < list2.length; i++) {
                out[i] = list2[i];
            }
            return out;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_main);
        getWindow().addFlags(128);
        this.mBtnUpdateTX = (Button) findViewById(R.id.update_tx);
        this.mBtnUpdateRF = (Button) findViewById(R.id.update_rf);
        this.mBtnFinish = (Button) findViewById(R.id.update_finish);
        this.mTXFileList = (ListView) findViewById(R.id.tx_file_list);
        this.mRFFileList = (ListView) findViewById(R.id.rf_file_list);
        this.mSelectedTXLabel = (TextView) findViewById(R.id.selected_tx_file);
        this.mSelectedRFLabel = (TextView) findViewById(R.id.selected_rf_file);
        this.mBtnUpdateTX.setOnClickListener(this);
        this.mBtnUpdateRF.setOnClickListener(this);
        this.mBtnFinish.setOnClickListener(this);
        File[] tx_firmware_list = loadList(FIRMWARELOCATION + "/tx");
        File[] rf_firmware_list = loadList(FIRMWARELOCATION + "/rf");
        File[] tx_firmware_list_2 = loadList("/storage/sdcard1/firmware/tx");
        File[] rf_firmware_list_2 = loadList("/storage/sdcard1/firmware/rf");
        File[] tx_final_list = combineTwoList(tx_firmware_list, tx_firmware_list_2);
        File[] rf_final_list = combineTwoList(rf_firmware_list, rf_firmware_list_2);
        FileAdapter tx_adapter = new FileAdapter(this, 17367043, 16908308, tx_final_list);
        FileAdapter rf_adapter = new FileAdapter(this, 17367043, 16908308, rf_final_list);
        this.mTXFileList.setAdapter(tx_adapter);
        this.mRFFileList.setAdapter(rf_adapter);
        this.mTXFileList.setOnItemClickListener(this);
        this.mRFFileList.setOnItemClickListener(this);
        this.mUARTController = UARTController.getInstance();
        if (this.mUARTController == null) {
            CharSequence package_name = UARTController.getPackagenameByProcess(this);
            Builder ab = new Builder(this);
            if (Utilities.PROJECT_TAG.equals(Utilities.PROJECT_TAG)) {
                this.mGuardDialog = ab.setTitle(R.string.peripheral_occupied_title_st10).setMessage(getResources().getString(R.string.peripheral_occupied_message_st10, new Object[]{package_name})).setNeutralButton(17039370, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        UpdateMain.this.finish();
                    }
                }).setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        UpdateMain.this.finish();
                    }
                }).create();
            } else if (Utilities.PROJECT_TAG.equals("ST12")) {
                this.mGuardDialog = ab.setTitle(R.string.peripheral_occupied_title_st12).setMessage(getResources().getString(R.string.peripheral_occupied_message_st12, new Object[]{package_name})).setNeutralButton(17039370, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        UpdateMain.this.finish();
                    }
                }).setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        UpdateMain.this.finish();
                    }
                }).create();
            } else if (Utilities.PROJECT_TAG.equals("ST15")) {
                this.mGuardDialog = ab.setTitle(R.string.peripheral_occupied_title).setMessage(getResources().getString(R.string.peripheral_occupied_message, new Object[]{package_name})).setNeutralButton(17039370, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        UpdateMain.this.finish();
                    }
                }).setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        UpdateMain.this.finish();
                    }
                }).create();
            }
            this.mGuardDialog.show();
            return;
        }
        this.mUARTController.startReading();
        ((TextView) findViewById(R.id.tx_bl_version)).setText("Bootloader:" + this.mUARTController.getTxBootloaderVersion());
        this.mUARTController.stopReading();
    }

    protected void onDestroy() {
        if (this.mGuardDialog != null) {
            this.mGuardDialog.dismiss();
        }
        if (this.mUARTController != null) {
            this.mUARTController = null;
        }
        super.onDestroy();
    }

    public void onClick(View v) {
        if (v.equals(this.mBtnUpdateTX)) {
            if (TextUtils.isEmpty(this.mSelectedTXFile)) {
                Log.e(TAG, "No TX File selected");
                showNoSelectionDialog(getString(R.string.dialog_no_selection));
                return;
            }
            this.mProgressDialog = ProgressDialog.show(this, getString(R.string.title_updating_tx), getString(R.string.str_dialog_waiting), false, false);
            new UpdateTask().execute(new String[]{"TX", this.mSelectedTXFile});
        } else if (v.equals(this.mBtnUpdateRF)) {
            if (TextUtils.isEmpty(this.mSelectedRFFile)) {
                Log.e(TAG, "No RF File selected");
                showNoSelectionDialog(getString(R.string.dialog_select_rf));
                return;
            }
            this.mProgressDialog = ProgressDialog.show(this, getString(R.string.title_updating_rf), getString(R.string.str_dialog_waiting), false, false);
            new UpdateTask().execute(new String[]{"RF", this.mSelectedRFFile});
        } else if (v.equals(this.mBtnFinish)) {
            finish();
        }
    }

    private void showNoSelectionDialog(String message) {
        new Builder(this).setTitle(getString(R.string.title_no_selected)).setMessage(message).setPositiveButton(17039370, null).create().show();
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File file;
        if (parent.equals(this.mTXFileList)) {
            file = (File) parent.getItemAtPosition(position);
            this.mSelectedTXFile = file.getAbsolutePath();
            this.mSelectedTXLabel.setText(file.getName());
        } else if (parent.equals(this.mRFFileList)) {
            file = (File) parent.getItemAtPosition(position);
            this.mSelectedRFFile = file.getAbsolutePath();
            this.mSelectedRFLabel.setText(file.getName());
        }
    }
}
