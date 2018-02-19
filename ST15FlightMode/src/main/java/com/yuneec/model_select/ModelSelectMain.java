package com.yuneec.model_select;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore.Images.Media;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.yuneec.channelsettings.DRData;
import com.yuneec.channelsettings.ServoData;
import com.yuneec.channelsettings.ThrottleData;
import com.yuneec.database.DBOpenHelper;
import com.yuneec.database.DataProvider;
import com.yuneec.database.DataProviderHelper;
import com.yuneec.database.ModelBrief;
import com.yuneec.flight_settings.ChannelMap;
import com.yuneec.flight_settings.FlightSettings;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.SyncModelDataTask;
import com.yuneec.flightmode15.SyncModelDataTask.SyncModelDataCompletedAction;
import com.yuneec.flightmode15.Utilities;
import com.yuneec.flightmode15.Utilities.ReceiverInfomation;
import com.yuneec.uartcontroller.UARTController;
import com.yuneec.widget.BaseDialog;
import com.yuneec.widget.MyProgressDialog;
import com.yuneec.widget.MyToast;
import com.yuneec.widget.StatusbarView;
import com.yuneec.widget.TwoButtonPopDialog;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ModelSelectMain extends Activity {
    private static final int APPS_PER_PAGE = 8;
    public static final String IMAGE_SUFFIX = ".jpg";
    private static final String TAG = "ModelSelectMain";
    private int mAircraftType;
    private UARTController mController;
    private long mCurrModelId;
    private OnItemClickListener mElementClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final ModelBrief element = (ModelBrief) parent.getItemAtPosition(position);
            if (element._id == -1) {
                ModelSelectMain.this.AddImportAction(element);
                return;
            }
            int cur_rx = ModelSelectMain.this.getCurrentModelReceiverAddress(element._id);
            int bind_rx = ModelSelectMain.this.mController.queryBindState();
            if (bind_rx <= 0) {
                ModelSelectMain.this.switchModel(parent, element);
            } else if (bind_rx != cur_rx) {
                final AdapterView<?> inner = parent;
                final TwoButtonPopDialog dialog = new TwoButtonPopDialog(ModelSelectMain.this);
                dialog.adjustHeight(380);
                dialog.setTitle((int) R.string.str_switch_model_title);
                dialog.setMessage((int) R.string.str_switch_model_warning);
                dialog.setPositiveButton(17039379, new OnClickListener() {
                    public void onClick(View v) {
                        ModelSelectMain.this.mController.unbind(true, 3);
                        ModelSelectMain.this.switchModel(inner, element);
                        dialog.dismiss();
                    }
                });
                dialog.setNegativeButton(17039369, new OnClickListener() {
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
                dialog.show();
            } else {
                ModelSelectMain.this.switchModel(parent, element);
            }
        }
    };
    private OnItemLongClickListener mElementLongClickListener = new OnItemLongClickListener() {
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            ModelBrief element = (ModelBrief) parent.getItemAtPosition(position);
            if (element._id == -1) {
                ModelSelectMain.this.AddImportAction(element);
            } else {
                ModelSelectMain.this.ModelAction(element);
            }
            return false;
        }
    };
    private ArrayList<GridView> mGridList = new ArrayList(3);
    private ArrayList<ModelBrief> mGridViewList;
    private Handler mHandler = new Handler();
    private PageIndicator mIndicator;
    private Runnable mInvalidateOtherPages = new Runnable() {
        public void run() {
            int N = ModelSelectMain.this.mGridList.size();
            for (int i = 0; i < N; i++) {
                ((GridView) ModelSelectMain.this.mGridList.get(i)).invalidateViews();
            }
        }
    };
    private ModelBrief mModelToAsyncOperate;
    private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {
        public void onPageSelected(int position) {
            ModelSelectMain.this.mIndicator.setCurrentPage(position);
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        public void onPageScrollStateChanged(int arg0) {
        }
    };
    private SharedPreferences mPrefs;
    private MyProgressDialog mProgressDialog;
    private StatusbarView mStatus;
    private ViewPager mViewPager;

    private class GridViewAdapter extends ArrayAdapter<ModelBrief> {
        private ArrayList<ModelBrief> mElement;

        public GridViewAdapter(Context context, ArrayList<ModelBrief> element) {
            super(context, 0, element);
            this.mElement = element;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ModelBrief info = (ModelBrief) this.mElement.get(position);
            if (convertView == null) {
                convertView = ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.model_select_element, parent, false);
            }
            ImageView image_item = (ImageView) convertView.findViewById(R.id.image_item);
            TextView text_item = (TextView) convertView.findViewById(R.id.text_item);
            RelativeLayout rl = (RelativeLayout) convertView.findViewById(R.id.element_frame);
            if (info._id == ModelSelectMain.this.mCurrModelId) {
                rl.setBackgroundResource(R.drawable.model_type_selected);
                text_item.setSelected(true);
            } else {
                rl.setBackgroundResource(R.drawable.model_type_normal);
                text_item.setSelected(false);
            }
            image_item.setImageResource(info.iconResourceId);
            text_item.setText(info.name);
            return convertView;
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, String> {
        private String what;

        private MyAsyncTask() {
        }

        private void cleanupOnFailure(String path) {
            File dir = new File(path);
            File[] files = dir.listFiles();
            if (files != null) {
                for (File delete : files) {
                    delete.delete();
                }
            }
            dir.delete();
        }

        private void showResult(String result) {
            CharSequence hint = null;
            if (this.what.equals("copy")) {
                if ("OK".equals(result)) {
                    hint = ModelSelectMain.this.getResources().getString(R.string.str_model_copied, new Object[]{" "});
                } else {
                    hint = ModelSelectMain.this.getResources().getString(R.string.str_model_copy_failed, new Object[]{" "});
                }
            } else if (this.what.equals("export")) {
                if (result.equals("Prepare Error")) {
                    hint = "Create Export Folder Failed";
                } else if (result.startsWith("IO Error")) {
                    hint = "An Error Occurred while exporting the model";
                } else {
                    hint = ModelSelectMain.this.getResources().getString(R.string.str_model_exported, new Object[]{" ", result});
                }
            }
            if (hint != null) {
                MyToast.makeText(ModelSelectMain.this, hint, 0, 1).show();
            }
        }

        protected String doInBackground(String... params) {
            this.what = params[0];
            if (this.what.equals("copy")) {
                return ModelSelectMain.this.copyModel();
            }
            if (!this.what.equals("export")) {
                return null;
            }
            String path = ModelSelectMain.this.exportModel();
            if (path.startsWith("IO Error")) {
                String[] spilt = path.split("@");
                if (spilt.length == 2) {
                    cleanupOnFailure(spilt[1]);
                }
            }
            return path;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            final String params = result;
            ModelSelectMain.this.mModelToAsyncOperate = null;
            ModelSelectMain.this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    ModelSelectMain.this.mProgressDialog.dismiss();
                    MyAsyncTask.this.showResult(params);
                }
            }, 500);
            ModelSelectMain.this.refresh();
        }
    }

    private class AircraftPagerAdapter extends PagerAdapter {
        private AircraftPagerAdapter() {
        }

        public int getCount() {
            int pages = (int) Math.ceil(((double) ModelSelectMain.this.mGridViewList.size()) / 8.0d);
            ModelSelectMain.this.mIndicator.setPageCount(pages);
            return pages;
        }

        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0.equals(arg1);
        }

        public Object instantiateItem(ViewGroup container, int position) {
            int start = position * 8;
            int end = (position + 1) * 8;
            ArrayList<ModelBrief> page_list = new ArrayList(8);
            int i = start;
            while (i < end && i < ModelSelectMain.this.mGridViewList.size()) {
                page_list.add((ModelBrief) ModelSelectMain.this.mGridViewList.get(i));
                i++;
            }
            View v = ModelSelectMain.this.getLayoutInflater().inflate(R.layout.model_select_grid_view, null);
            GridView gv = (GridView) v;
            gv.setAdapter(new GridViewAdapter(ModelSelectMain.this, page_list));
            gv.setOnItemClickListener(ModelSelectMain.this.mElementClickListener);
            gv.setOnItemLongClickListener(ModelSelectMain.this.mElementLongClickListener);
            ModelSelectMain.this.mGridList.add(gv);
            container.addView(v);
            return v;
        }

        public void destroyItem(ViewGroup container, int position, Object object) {
            GridView v = (GridView) object;
            container.removeView(v);
            ModelSelectMain.this.mGridList.remove(v);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(128);
        setContentView(R.layout.model_select_main);
        this.mPrefs = getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0);
        this.mStatus = (StatusbarView) findViewById(R.id.statusbar);
    }

    public void onStart() {
        super.onStart();
        this.mAircraftType = 4;
        this.mIndicator = (PageIndicator) findViewById(R.id.pageIndicator);
        this.mViewPager = (ViewPager) findViewById(R.id.view_pager);
        this.mCurrModelId = this.mPrefs.getLong("current_model_id", -2);
        refresh();
    }

    private void refresh() {
        loadPage();
        this.mViewPager.setAdapter(new AircraftPagerAdapter());
        this.mViewPager.setOnPageChangeListener(this.mPageChangeListener);
    }

    public void onResume() {
        super.onResume();
        Utilities.readSDcard(this);
        Utilities.setStatusBarLeftText(this, this.mStatus);
        this.mController = UARTController.getInstance();
        this.mController.startReading();
    }

    public void onPause() {
        super.onPause();
        this.mController.stopReading();
        this.mController = null;
    }

    private Uri getImagePath(int id) {
        return Uri.parse("android.resource://" + getResources().getResourcePackageName(id) + "/" + getResources().getResourceTypeName(id) + "/" + getResources().getResourceEntryName(id));
    }

    private void loadPage() {
        this.mGridViewList = DataProviderHelper.getModelsByType(this, this.mAircraftType, Utilities.getCurrentMode());
        ModelBrief fake_model = new ModelBrief();
        fake_model._id = -1;
        fake_model.name = getResources().getString(R.string.str_new_model);
        fake_model.iconResourceId = R.drawable.model_select_add;
        fake_model.type = this.mAircraftType;
        if (this.mGridViewList == null) {
            this.mGridViewList = new ArrayList(8);
        }
        this.mGridViewList.add(0, fake_model);
    }

    private int getCurrentModelReceiverAddress(long model_id) {
        Uri uri = ContentUris.withAppendedId(DataProvider.MODEL_URI, model_id);
        Cursor c = getContentResolver().query(uri, new String[]{DBOpenHelper.KEY_ID, DBOpenHelper.KEY_RX}, null, null, null);
        if (!DataProviderHelper.isCursorValid(c)) {
            return 0;
        }
        int address = c.getInt(c.getColumnIndex(DBOpenHelper.KEY_RX));
        c.close();
        return address;
    }

    private void switchModel(AdapterView<?> parent, ModelBrief element) {
        this.mHandler.removeCallbacks(this.mInvalidateOtherPages);
        this.mCurrModelId = element._id;
        this.mPrefs.edit().putLong("current_model_id", element._id).commit();
        this.mPrefs.edit().putLong("current_model_type", (long) element.type).commit();
        if (parent instanceof GridView) {
            ((GridView) parent).invalidateViews();
        }
        this.mHandler.post(this.mInvalidateOtherPages);
        this.mProgressDialog = MyProgressDialog.show(this, "ABCDEFG", getResources().getString(R.string.str_dialog_waiting), false, false);
        new SyncModelDataTask(this, this.mController, new SyncModelDataCompletedAction() {
            public void SyncModelDataCompleted() {
                ModelSelectMain.this.mProgressDialog.dismiss();
                ModelSelectMain.this.mHandler.removeCallbacks(ModelSelectMain.this.mInvalidateOtherPages);
                Utilities.setStatusBarLeftText(ModelSelectMain.this, ModelSelectMain.this.mStatus);
                ModelSelectMain.this.finish();
            }
        }).execute(new Long[]{Long.valueOf(this.mCurrModelId)});
    }

    private void addModel(ModelBrief element) {
        editModel(element);
    }

    private void AddImportAction(final ModelBrief element) {
        final Dialog dialog = new BaseDialog(this, R.style.dialog_style);
        dialog.setContentView(R.layout.model_select_add_import_dialog);
        Button ms_create = (Button) dialog.findViewById(R.id.crate_model);
        Button ms_import = (Button) dialog.findViewById(R.id.import_model);
        if (Utilities.PROJECT_TAG.equals(Utilities.PROJECT_TAG) || Utilities.PROJECT_TAG.equals("ST12")) {
            ms_import.setVisibility(4);
            addModel(element);
        } else if (Utilities.PROJECT_TAG.equals("ST15")) {
            ms_create.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                    ModelSelectMain.this.addModel(element);
                }
            });
            ms_import.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                    ModelSelectMain.this.startActivity(new Intent(ModelSelectMain.this, ModelImport.class));
                }
            });
            dialog.show();
            dialog.setCanceledOnTouchOutside(true);
        }
    }

    private void ModelAction(final ModelBrief element) {
        final Dialog dialog = new BaseDialog(this, R.style.dialog_style);
        dialog.setContentView(R.layout.model_select_dialog);
        Button ms_edit = (Button) dialog.findViewById(R.id.edit_model);
        Button ms_delete = (Button) dialog.findViewById(R.id.delete_model);
        Button ms_copy = (Button) dialog.findViewById(R.id.copy_model);
        Button ms_export = (Button) dialog.findViewById(R.id.export_model);
        if (Utilities.PROJECT_TAG.equals(Utilities.PROJECT_TAG) || Utilities.PROJECT_TAG.equals("ST12")) {
            ms_copy.setVisibility(8);
            ms_export.setVisibility(8);
        }
        ms_edit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                ModelSelectMain.this.editModel(element);
            }
        });
        ms_delete.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                final TwoButtonPopDialog deleteDialog = new TwoButtonPopDialog(ModelSelectMain.this);
                deleteDialog.setTitle(ModelSelectMain.this.getResources().getString(R.string.str_delete));
                deleteDialog.adjustHeight(380);
                deleteDialog.setMessage(ModelSelectMain.this.getResources().getString(R.string.str_delete_text));
                final ModelBrief modelBrief = element;
                deleteDialog.setPositiveButton(R.string.str_ok, new OnClickListener() {
                    public void onClick(View v) {
                        deleteDialog.dismiss();
                        ModelSelectMain.this.deleteModel(modelBrief);
                    }
                });
                deleteDialog.setNegativeButton(R.string.str_cancel, new OnClickListener() {
                    public void onClick(View v) {
                        deleteDialog.dismiss();
                    }
                });
                deleteDialog.show();
                deleteDialog.setCanceledOnTouchOutside(true);
            }
        });
        if (Utilities.PROJECT_TAG.equals("ST15")) {
            ms_copy.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                    ModelSelectMain.this.mModelToAsyncOperate = element;
                    ModelSelectMain.this.showProgressDialog("copy", false);
                }
            });
            ms_export.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                    ModelSelectMain.this.mModelToAsyncOperate = element;
                    ModelSelectMain.this.showProgressDialog("export", false);
                }
            });
        }
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
    }

    private void editModel(ModelBrief element) {
        Intent intent = new Intent(this, EditModel.class);
        intent.putExtra("type", element.type);
        intent.putExtra(DBOpenHelper.KEY_ID, element._id);
        startActivity(intent);
    }

    private String copyModel() {
        ContentValues cv = new ContentValues();
        ContentResolver cr = getContentResolver();
        cv.put(DBOpenHelper.KEY_NAME, this.mModelToAsyncOperate.name);
        cv.put(DBOpenHelper.KEY_ICON, Integer.valueOf(this.mModelToAsyncOperate.iconResourceId));
        cv.put("type", Integer.valueOf(this.mModelToAsyncOperate.type));
        cv.put(DBOpenHelper.KEY_FPV, Integer.valueOf(this.mModelToAsyncOperate.fpv));
        cv.put(DBOpenHelper.KEY_F_MODE_KEY, Integer.valueOf(this.mModelToAsyncOperate.f_mode_key));
        ReceiverInfomation ri = Utilities.getReceiverInfo(this, this.mModelToAsyncOperate._id);
        if (ri == null) {
            Log.i(TAG, "can't get receiver info,skip copy");
        } else if (ri.analogChNumber == 0 && ri.switchChNumber == 0) {
            Log.i(TAG, "can't get receiver info,skip copy");
        } else {
            cv.put(DBOpenHelper.KEY_RX_ANALOG_NUM, Integer.valueOf(ri.analogChNumber));
            cv.put(DBOpenHelper.KEY_RX_SWITCH_NUM, Integer.valueOf(ri.switchChNumber));
        }
        cv.put(DBOpenHelper.KEY_ANALOG_MIN, Integer.valueOf(this.mModelToAsyncOperate.analog_min));
        cv.put(DBOpenHelper.KEY_SWITCH_MIN, Integer.valueOf(this.mModelToAsyncOperate.switch_min));
        Uri newUri = cr.insert(DataProvider.MODEL_URI, cv);
        if (newUri == null) {
            return null;
        }
        try {
            long newId = ContentUris.parseId(newUri);
            if (newId == -1) {
                Log.e(TAG, "Bad Uri :" + newUri);
                return null;
            }
            long oldId = this.mModelToAsyncOperate._id;
            if (copyChannelMap(oldId, newId) && copyThrData(oldId, newId) && copyDRData(oldId, newId) && copyServoData(oldId, newId)) {
                return "OK";
            }
            return null;
        } catch (NumberFormatException e) {
            Log.e(TAG, "Bad Uri :" + newUri);
            return null;
        }
    }

    private boolean copyChannelMap(long oldModelId, long newModelId) {
        ChannelMap[] old_cm = DataProviderHelper.readChannelMapFromDatabase(this, oldModelId);
        ChannelMap[] new_cm = DataProviderHelper.readChannelMapFromDatabase(this, newModelId);
        if (old_cm.length == new_cm.length) {
            for (int i = 0; i < new_cm.length; i++) {
                old_cm[i].id = new_cm[i].id;
            }
            DataProviderHelper.writeChannelMapFromDatabase(this, old_cm);
            return true;
        }
        Log.e(TAG, "copyChannelMap----Data is unified");
        return false;
    }

    private boolean copyThrData(long oldModelId, long newModelId) {
        for (int fmode = 0; fmode < 3; fmode++) {
            ThrottleData old_td = DataProviderHelper.readThrDataFromDatabase(this, oldModelId, fmode);
            ThrottleData new_td = DataProviderHelper.readThrDataFromDatabase(this, newModelId, fmode);
            old_td.id = new_td.id;
            old_td.thrCurve[0].id = new_td.thrCurve[0].id;
            old_td.thrCurve[1].id = new_td.thrCurve[1].id;
            old_td.thrCurve[2].id = new_td.thrCurve[2].id;
            DataProviderHelper.writeThrDataToDatabase(this, old_td);
        }
        return true;
    }

    private boolean copyDRData(long oldModelId, long newModelId) {
        boolean result = false;
        for (int fmode = 0; fmode < 3; fmode++) {
            DRData[] old_dd = DataProviderHelper.readDRDataFromDatabase(this, oldModelId, fmode);
            DRData[] new_dd = DataProviderHelper.readDRDataFromDatabase(this, newModelId, fmode);
            if (old_dd.length == new_dd.length) {
                for (int func = 0; func < 3; func++) {
                    old_dd[func].id = new_dd[func].id;
                    old_dd[func].curveparams[0].id = new_dd[func].curveparams[0].id;
                    old_dd[func].curveparams[1].id = new_dd[func].curveparams[1].id;
                    old_dd[func].curveparams[2].id = new_dd[func].curveparams[2].id;
                }
                DataProviderHelper.writeDRDataToDatabase(this, old_dd);
                result = true;
            } else {
                Log.e(TAG, "copyDRData----Data is unified");
                result = false;
            }
        }
        return result;
    }

    private boolean copyServoData(long oldModelId, long newModelId) {
        boolean result = false;
        for (int fmode = 0; fmode < 3; fmode++) {
            ServoData[] old_sd = DataProviderHelper.readServoDataFromDatabase(this, oldModelId, fmode);
            ServoData[] new_sd = DataProviderHelper.readServoDataFromDatabase(this, newModelId, fmode);
            if (old_sd.length == new_sd.length) {
                for (int i = 0; i < new_sd.length; i++) {
                    old_sd[i].id = new_sd[i].id;
                }
                DataProviderHelper.writeServoDataToDatabase(this, old_sd);
                result = true;
            } else {
                Log.e(TAG, "copyServoData----Data is unified");
                result = false;
            }
        }
        return result;
    }

    private void deleteModel(ModelBrief element) {
        getContentResolver().delete(ContentUris.withAppendedId(DataProvider.MODEL_URI, element._id), null, null);
        if (element._id == this.mCurrModelId) {
            this.mCurrModelId = -2;
            this.mPrefs.edit().putLong("current_model_id", this.mCurrModelId).commit();
            Utilities.syncMixingDataDeleteAll(this.mController);
            Utilities.setStatusBarLeftText(this, this.mStatus);
        }
        refresh();
    }

    private String exportModel() {
        FileWriter fw_channelmap;
        FileWriter fileWriter;
        String str;
        Throwable th;
        FileWriter fw_dr_datas;
        FileWriter fileWriter2;
        File folder = prepareExportedFolder();
        if (folder == null) {
            return "Prepare Error";
        }
        File file = new File(folder, "manifest.csv");
        File channelmap_manifest = new File(folder, "channelmap.csv");
        file = new File(folder, "thr_datas.csv");
        File dr_datas_manifest = new File(folder, "dr_datas.csv");
        file = new File(folder, "servos.csv");
        FileWriter fw_manifest = null;
        FileWriter fw_channelmap2 = null;
        FileWriter fw_dr_datas2 = null;
        FileWriter fw_sevros = null;
        try {
            FileWriter fw_manifest2 = new FileWriter(file);
            try {
                fw_channelmap = new FileWriter(channelmap_manifest);
                try {
                    fileWriter = new FileWriter(file);
                } catch (IOException e) {
                    fw_channelmap2 = fw_channelmap;
                    fw_manifest = fw_manifest2;
                    try {
                        str = "IO Error@" + folder.getAbsolutePath();
                        if (fw_manifest != null) {
                            try {
                                fw_manifest.close();
                            } catch (IOException e2) {
                                return str;
                            }
                        }
                        if (fw_channelmap2 != null) {
                            fw_channelmap2.close();
                        }
                        if (fw_dr_datas2 != null) {
                            fw_dr_datas2.close();
                        }
                        if (fw_dr_datas2 != null) {
                            fw_dr_datas2.close();
                        }
                        if (fw_sevros != null) {
                            return str;
                        }
                        fw_sevros.close();
                        return str;
                    } catch (Throwable th2) {
                        th = th2;
                        if (fw_manifest != null) {
                            try {
                                fw_manifest.close();
                            } catch (IOException e3) {
                                throw th;
                            }
                        }
                        if (fw_channelmap2 != null) {
                            fw_channelmap2.close();
                        }
                        if (fw_dr_datas2 != null) {
                            fw_dr_datas2.close();
                        }
                        if (fw_dr_datas2 != null) {
                            fw_dr_datas2.close();
                        }
                        if (fw_sevros != null) {
                            fw_sevros.close();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fw_channelmap2 = fw_channelmap;
                    fw_manifest = fw_manifest2;
                    if (fw_manifest != null) {
                        fw_manifest.close();
                    }
                    if (fw_channelmap2 != null) {
                        fw_channelmap2.close();
                    }
                    if (fw_dr_datas2 != null) {
                        fw_dr_datas2.close();
                    }
                    if (fw_dr_datas2 != null) {
                        fw_dr_datas2.close();
                    }
                    if (fw_sevros != null) {
                        fw_sevros.close();
                    }
                    throw th;
                }
            } catch (IOException e4) {
                fw_manifest = fw_manifest2;
                str = "IO Error@" + folder.getAbsolutePath();
                if (fw_manifest != null) {
                    fw_manifest.close();
                }
                if (fw_channelmap2 != null) {
                    fw_channelmap2.close();
                }
                if (fw_dr_datas2 != null) {
                    fw_dr_datas2.close();
                }
                if (fw_dr_datas2 != null) {
                    fw_dr_datas2.close();
                }
                if (fw_sevros != null) {
                    return str;
                }
                fw_sevros.close();
                return str;
            } catch (Throwable th4) {
                th = th4;
                fw_manifest = fw_manifest2;
                if (fw_manifest != null) {
                    fw_manifest.close();
                }
                if (fw_channelmap2 != null) {
                    fw_channelmap2.close();
                }
                if (fw_dr_datas2 != null) {
                    fw_dr_datas2.close();
                }
                if (fw_dr_datas2 != null) {
                    fw_dr_datas2.close();
                }
                if (fw_sevros != null) {
                    fw_sevros.close();
                }
                throw th;
            }
            try {
                fw_dr_datas = new FileWriter(dr_datas_manifest);
                try {
                    fileWriter = new FileWriter(file);
                } catch (IOException e5) {
                    fw_dr_datas2 = fw_dr_datas;
                    fileWriter2 = fileWriter;
                    fw_channelmap2 = fw_channelmap;
                    fw_manifest = fw_manifest2;
                    str = "IO Error@" + folder.getAbsolutePath();
                    if (fw_manifest != null) {
                        fw_manifest.close();
                    }
                    if (fw_channelmap2 != null) {
                        fw_channelmap2.close();
                    }
                    if (fw_dr_datas2 != null) {
                        fw_dr_datas2.close();
                    }
                    if (fw_dr_datas2 != null) {
                        fw_dr_datas2.close();
                    }
                    if (fw_sevros != null) {
                        return str;
                    }
                    fw_sevros.close();
                    return str;
                } catch (Throwable th5) {
                    th = th5;
                    fw_dr_datas2 = fw_dr_datas;
                    fileWriter2 = fileWriter;
                    fw_channelmap2 = fw_channelmap;
                    fw_manifest = fw_manifest2;
                    if (fw_manifest != null) {
                        fw_manifest.close();
                    }
                    if (fw_channelmap2 != null) {
                        fw_channelmap2.close();
                    }
                    if (fw_dr_datas2 != null) {
                        fw_dr_datas2.close();
                    }
                    if (fw_dr_datas2 != null) {
                        fw_dr_datas2.close();
                    }
                    if (fw_sevros != null) {
                        fw_sevros.close();
                    }
                    throw th;
                }
            } catch (IOException e6) {
                fileWriter2 = fileWriter;
                fw_channelmap2 = fw_channelmap;
                fw_manifest = fw_manifest2;
                str = "IO Error@" + folder.getAbsolutePath();
                if (fw_manifest != null) {
                    fw_manifest.close();
                }
                if (fw_channelmap2 != null) {
                    fw_channelmap2.close();
                }
                if (fw_dr_datas2 != null) {
                    fw_dr_datas2.close();
                }
                if (fw_dr_datas2 != null) {
                    fw_dr_datas2.close();
                }
                if (fw_sevros != null) {
                    return str;
                }
                fw_sevros.close();
                return str;
            } catch (Throwable th6) {
                th = th6;
                fileWriter2 = fileWriter;
                fw_channelmap2 = fw_channelmap;
                fw_manifest = fw_manifest2;
                if (fw_manifest != null) {
                    fw_manifest.close();
                }
                if (fw_channelmap2 != null) {
                    fw_channelmap2.close();
                }
                if (fw_dr_datas2 != null) {
                    fw_dr_datas2.close();
                }
                if (fw_dr_datas2 != null) {
                    fw_dr_datas2.close();
                }
                if (fw_sevros != null) {
                    fw_sevros.close();
                }
                throw th;
            }
            try {
                buildModelManifest(fw_manifest2);
                exportChannelMap(this, fw_channelmap, this.mModelToAsyncOperate._id);
                exportThrData(this, fileWriter, this.mModelToAsyncOperate._id);
                exportDRData(this, fw_dr_datas, this.mModelToAsyncOperate._id);
                exporServoData(this, fileWriter, this.mModelToAsyncOperate._id);
                fw_channelmap.flush();
                fileWriter.flush();
                fw_dr_datas.flush();
                fileWriter.flush();
                CopyIconResource(folder);
                if (fw_manifest2 != null) {
                    try {
                        fw_manifest2.close();
                    } catch (IOException e7) {
                    }
                }
                if (fw_channelmap != null) {
                    fw_channelmap.close();
                }
                if (fw_dr_datas != null) {
                    fw_dr_datas.close();
                }
                if (fw_dr_datas != null) {
                    fw_dr_datas.close();
                }
                if (fileWriter != null) {
                    fileWriter.close();
                }
                if (Utilities.hasSDCard(this)) {
                    return folder.getAbsolutePath().substring(Utilities.getExterPath().length());
                }
                return folder.getAbsolutePath().substring(Environment.getExternalStorageDirectory().getAbsolutePath().length());
            } catch (IOException e8) {
                fw_sevros = fileWriter;
                fw_dr_datas2 = fw_dr_datas;
                fileWriter2 = fileWriter;
                fw_channelmap2 = fw_channelmap;
                fw_manifest = fw_manifest2;
                str = "IO Error@" + folder.getAbsolutePath();
                if (fw_manifest != null) {
                    fw_manifest.close();
                }
                if (fw_channelmap2 != null) {
                    fw_channelmap2.close();
                }
                if (fw_dr_datas2 != null) {
                    fw_dr_datas2.close();
                }
                if (fw_dr_datas2 != null) {
                    fw_dr_datas2.close();
                }
                if (fw_sevros != null) {
                    return str;
                }
                fw_sevros.close();
                return str;
            } catch (Throwable th7) {
                th = th7;
                fw_sevros = fileWriter;
                fw_dr_datas2 = fw_dr_datas;
                fileWriter2 = fileWriter;
                fw_channelmap2 = fw_channelmap;
                fw_manifest = fw_manifest2;
                if (fw_manifest != null) {
                    fw_manifest.close();
                }
                if (fw_channelmap2 != null) {
                    fw_channelmap2.close();
                }
                if (fw_dr_datas2 != null) {
                    fw_dr_datas2.close();
                }
                if (fw_dr_datas2 != null) {
                    fw_dr_datas2.close();
                }
                if (fw_sevros != null) {
                    fw_sevros.close();
                }
                throw th;
            }
        } catch (IOException e9) {
            str = "IO Error@" + folder.getAbsolutePath();
            if (fw_manifest != null) {
                fw_manifest.close();
            }
            if (fw_channelmap2 != null) {
                fw_channelmap2.close();
            }
            if (fw_dr_datas2 != null) {
                fw_dr_datas2.close();
            }
            if (fw_dr_datas2 != null) {
                fw_dr_datas2.close();
            }
            if (fw_sevros != null) {
                return str;
            }
            fw_sevros.close();
            return str;
        }
    }

    private void exportChannelMap(Context context, FileWriter fw, long model_id) throws IOException {
        ChannelMap[] cm = DataProviderHelper.readChannelMapFromDatabase(context, model_id);
        for (int i = 0; i < cm.length; i++) {
            fw.write(new StringBuilder(String.valueOf(cm[i].channel)).append(",").toString());
            fw.write(new StringBuilder(String.valueOf(cm[i].function)).append(",").toString());
            fw.write(new StringBuilder(String.valueOf(cm[i].hardware)).append(",").toString());
            fw.write(new StringBuilder(String.valueOf(cm[i].alias)).append("\n").toString());
        }
    }

    private void exportThrData(Context context, FileWriter fw, long model_id) throws IOException {
        for (int fmode = 0; fmode < 3; fmode++) {
            ThrottleData td = DataProviderHelper.readThrDataFromDatabase(context, model_id, fmode);
            for (int state = 0; state < 3; state++) {
                fw.write(new StringBuilder(String.valueOf(fmode)).append(",").toString());
                fw.write(td.expo + ",");
                fw.write(td.sw + ",");
                fw.write(td.cut_sw + ",");
                fw.write(td.cut_value1 + ",");
                fw.write(td.cut_value2 + ",");
                fw.write(new StringBuilder(String.valueOf(td.thrCurve[state].sw_state)).append(",").toString());
                fw.write(new StringBuilder(String.valueOf(td.thrCurve[state].curvePoints[0])).append(",").toString());
                fw.write(new StringBuilder(String.valueOf(td.thrCurve[state].curvePoints[1])).append(",").toString());
                fw.write(new StringBuilder(String.valueOf(td.thrCurve[state].curvePoints[2])).append(",").toString());
                fw.write(new StringBuilder(String.valueOf(td.thrCurve[state].curvePoints[3])).append(",").toString());
                fw.write(new StringBuilder(String.valueOf(td.thrCurve[state].curvePoints[4])).append("\n").toString());
            }
        }
    }

    private void exportDRData(Context context, FileWriter fw, long model_id) throws IOException {
        for (int fmode = 0; fmode < 3; fmode++) {
            DRData[] dd = DataProviderHelper.readDRDataFromDatabase(context, model_id, fmode);
            for (int func = 0; func < 3; func++) {
                for (int state = 0; state < 3; state++) {
                    fw.write(new StringBuilder(String.valueOf(fmode)).append(",").toString());
                    fw.write(new StringBuilder(String.valueOf(dd[func].func)).append(",").toString());
                    fw.write(new StringBuilder(String.valueOf(dd[func].sw)).append(",").toString());
                    fw.write(new StringBuilder(String.valueOf(dd[func].curveparams[state].sw_state)).append(",").toString());
                    fw.write(new StringBuilder(String.valueOf(dd[func].curveparams[state].rate1)).append(",").toString());
                    fw.write(new StringBuilder(String.valueOf(dd[func].curveparams[state].rate2)).append(",").toString());
                    fw.write(new StringBuilder(String.valueOf(dd[func].curveparams[state].expo1)).append(",").toString());
                    fw.write(new StringBuilder(String.valueOf(dd[func].curveparams[state].expo2)).append(",").toString());
                    fw.write(new StringBuilder(String.valueOf(dd[func].curveparams[state].offset)).append("\n").toString());
                }
            }
        }
    }

    private void exporServoData(Context context, FileWriter fw, long model_id) throws IOException {
        for (int fmode = 0; fmode < 3; fmode++) {
            ServoData[] sd = DataProviderHelper.readServoDataFromDatabase(context, model_id, fmode);
            for (int i = 0; i < sd.length; i++) {
                fw.write(new StringBuilder(String.valueOf(fmode)).append(",").toString());
                fw.write(new StringBuilder(String.valueOf(sd[i].func)).append(",").toString());
                fw.write(new StringBuilder(String.valueOf(sd[i].subTrim)).append(",").toString());
                fw.write(new StringBuilder(String.valueOf(sd[i].reverse)).append(",").toString());
                fw.write(new StringBuilder(String.valueOf(sd[i].speed)).append(",").toString());
                fw.write(new StringBuilder(String.valueOf(sd[i].travelL)).append(",").toString());
                fw.write(new StringBuilder(String.valueOf(sd[i].travelR)).append("\n").toString());
            }
        }
    }

    private void showProgressDialog(String what, boolean cancelable) {
        String[] param = new String[]{what};
        this.mProgressDialog = MyProgressDialog.show(this, null, getResources().getText(R.string.str_dialog_waiting), false, cancelable);
        new MyAsyncTask().execute(param);
    }

    private File prepareExportedFolder() {
        File dir;
        if (Utilities.hasSDCard(this)) {
            dir = new File(new File(Utilities.getExterPath()), "Exported-Models");
        } else {
            dir = new File(Environment.getExternalStorageDirectory(), "Exported-Models");
        }
        if (dir.exists() || dir.mkdir()) {
            Time now = new Time();
            now.setToNow();
            File folder = new File(dir + "/" + "FPV" + "-" + this.mModelToAsyncOperate.name + "-" + now.format("%Y-%m-%d %H.%M.%S"));
            if (folder.mkdir()) {
                return folder;
            }
            Log.e(TAG, "create folder failed" + dir.getAbsolutePath());
            return null;
        }
        Log.e(TAG, "mkdir error dir:" + dir.getAbsolutePath());
        return null;
    }

    private void buildModelManifest(FileWriter fw) throws IOException {
        fw.write(DBOpenHelper.KEY_FPV);
        fw.write(44);
        fw.write(String.valueOf(this.mModelToAsyncOperate.fpv));
        fw.write(10);
        fw.write(DBOpenHelper.VERION);
        fw.write(44);
        fw.write("1");
        fw.write(10);
        fw.write(DBOpenHelper.KEY_NAME);
        fw.write(44);
        fw.write(this.mModelToAsyncOperate.name);
        fw.write(10);
        fw.write("type");
        fw.write(44);
        fw.write(String.valueOf(this.mModelToAsyncOperate.type));
        fw.write(10);
        fw.write(DBOpenHelper.KEY_F_MODE_KEY);
        fw.write(44);
        fw.write(String.valueOf(this.mModelToAsyncOperate.f_mode_key));
        fw.write(10);
        fw.write(DBOpenHelper.KEY_ANALOG_MIN);
        fw.write(44);
        fw.write(String.valueOf(this.mModelToAsyncOperate.analog_min));
        fw.write(10);
        fw.write(DBOpenHelper.KEY_SWITCH_MIN);
        fw.write(44);
        fw.write(String.valueOf(this.mModelToAsyncOperate.switch_min));
        fw.write(10);
        ReceiverInfomation ri = Utilities.getReceiverInfo(this, this.mModelToAsyncOperate._id);
        if (ri != null) {
            fw.write(DBOpenHelper.KEY_RX_ANALOG_NUM);
            fw.write(44);
            fw.write(String.valueOf(ri.analogChNumber));
            fw.write(10);
            fw.write(DBOpenHelper.KEY_RX_SWITCH_NUM);
            fw.write(44);
            fw.write(String.valueOf(ri.switchChNumber));
            fw.write(10);
        } else {
            Log.i(TAG, "cann't get rx info,skip export");
        }
        fw.flush();
    }

    private void CopyIconResource(File folder) throws IOException {
    }

    private String getIconLocation(Uri uri) {
        if (uri.getScheme().startsWith("file")) {
            return Uri.decode(uri.getPath());
        }
        if (uri.getScheme().startsWith("content")) {
            Cursor c = Media.query(getContentResolver(), uri, new String[]{"_data"});
            if (c != null && c.moveToFirst()) {
                String location = c.getString(c.getColumnIndex("_data"));
                c.close();
                return location;
            } else if (c == null) {
                return null;
            } else {
                c.close();
                return null;
            }
        }
        Log.w(TAG, "getIconLocation Unkown Uri: " + uri);
        return null;
    }
}
