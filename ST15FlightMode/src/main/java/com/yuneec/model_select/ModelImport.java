package com.yuneec.model_select;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.yuneec.database.DBOpenHelper;
import com.yuneec.flightmode15.ModelImporter;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;
import com.yuneec.widget.MyProgressDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ModelImport extends Activity implements OnItemClickListener, OnClickListener {
    private static final String EXTEN_PATH = "/storage/sdcard1/Exported-Models";
    private static final String LOCATION = new StringBuilder(String.valueOf(Environment.getExternalStorageDirectory().getAbsolutePath())).append("/").append("Exported-Models").toString();
    private static final int SHOW_DIALOG_NUM = 5;
    private static final String TAG = "ModelImportFragment";
    private boolean isFirstShowDetail = true;
    private Button mBtnDelete;
    private Button mBtnImport;
    private String mCurrentImport;
    private Handler mHandler = new Handler();
    private ViewGroup mImportContent;
    private ViewGroup mImportDetail;
    private ArrayList<String> mImportNames = new ArrayList();
    private ListView mListView;
    private ProgressBar mLoadingProgressBar;
    private MyAsyncTask mLoadingTask;
    private ArrayAdapter<String> mModelAdapter;
    private TextView mNameLabel;
    private TextView mNoDetailLabel;
    private TextView mNoModelLabel;
    private ImageView mPicture;
    private ViewGroup mPictureFrame;
    private MyProgressDialog mProgressDialog;
    private ViewGroup mTypeFrame;
    private ImageView mTypeImage;
    private TextView mTypeName;

    private class DeleteAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private DeleteAsyncTask() {
        }

        protected Boolean doInBackground(Void... params) {
            ModelImport.this.deleteImport(ModelImport.this.mCurrentImport);
            return Boolean.valueOf(ModelImport.this.loadImportModels());
        }

        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            final boolean res = result.booleanValue();
            ModelImport.this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    ModelImport.this.mProgressDialog.dismiss();
                    if (res) {
                        ModelImport.this.mListView.setAdapter(null);
                        ModelImport.this.mModelAdapter = new ArrayAdapter(ModelImport.this, 17367062, ModelImport.this.mImportNames);
                        ModelImport.this.mListView.setAdapter(ModelImport.this.mModelAdapter);
                        return;
                    }
                    ModelImport.this.mImportContent.setVisibility(4);
                    ModelImport.this.mLoadingProgressBar.setVisibility(4);
                    ModelImport.this.mNoModelLabel.setVisibility(0);
                }
            }, 400);
        }
    }

    private class ImportAsyncTask extends AsyncTask<Void, Void, Integer> {
        private ImportAsyncTask() {
        }

        protected Integer doInBackground(Void... params) {
            ModelImporter mi = new ModelImporter(ModelImport.this);
            if (Utilities.hasSDCard(ModelImport.this)) {
                return Integer.valueOf(mi.importModelFromSD(ModelImport.EXTEN_PATH, ModelImport.this.mCurrentImport));
            }
            return Integer.valueOf(mi.importModelFromSD(ModelImport.LOCATION, ModelImport.this.mCurrentImport));
        }

        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            ModelImport.this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    ModelImport.this.mProgressDialog.dismiss();
                }
            }, 400);
        }
    }

    private class MyAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private MyAsyncTask() {
        }

        protected Boolean doInBackground(Void... params) {
            return Boolean.valueOf(ModelImport.this.loadImportModels());
        }

        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result.booleanValue()) {
                ModelImport.this.mModelAdapter = new ArrayAdapter(ModelImport.this, 17367062, ModelImport.this.mImportNames);
                ModelImport.this.mListView.setAdapter(ModelImport.this.mModelAdapter);
                dismissProgressBar(new Runnable() {
                    public void run() {
                        ModelImport.this.mLoadingProgressBar.setVisibility(4);
                        ModelImport.this.mNoModelLabel.setVisibility(4);
                        ModelImport.this.mImportContent.setVisibility(0);
                    }
                });
                return;
            }
            onCancelled();
        }

        protected void onCancelled() {
            super.onCancelled();
            ModelImport.this.mImportNames.clear();
            dismissProgressBar(new Runnable() {
                public void run() {
                    ModelImport.this.mImportContent.setVisibility(4);
                    ModelImport.this.mLoadingProgressBar.setVisibility(4);
                    ModelImport.this.mNoModelLabel.setVisibility(0);
                }
            });
        }

        private void dismissProgressBar(Runnable r) {
            ModelImport.this.mHandler.postDelayed(r, 300);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(128);
        setContentView(R.layout.model_import);
        if (Utilities.hasSDCard(this)) {
            ((TextView) findViewById(R.id.location)).setText(EXTEN_PATH);
        } else {
            ((TextView) findViewById(R.id.location)).setText(LOCATION);
        }
        this.mImportContent = (ViewGroup) findViewById(R.id.import_content);
        this.mImportDetail = (ViewGroup) findViewById(R.id.import_detail);
        this.mLoadingProgressBar = (ProgressBar) findViewById(R.id.loading_progressbar);
        this.mListView = (ListView) findViewById(R.id.model_list);
        this.mNameLabel = (TextView) findViewById(R.id.improt_photo_name);
        this.mNoModelLabel = (TextView) findViewById(R.id.label_no_model);
        this.mNoDetailLabel = (TextView) findViewById(R.id.no_import_detail);
        this.mPicture = (ImageView) findViewById(R.id.import_photo);
        this.mBtnDelete = (Button) findViewById(R.id.btn_delete);
        this.mBtnImport = (Button) findViewById(R.id.btn_import);
        this.mPictureFrame = (ViewGroup) findViewById(R.id.import_photo_frame);
        this.mTypeFrame = (ViewGroup) findViewById(R.id.import_type_frame);
        this.mTypeName = (TextView) findViewById(R.id.import_type_name);
        this.mTypeImage = (ImageView) findViewById(R.id.import_type);
        this.mImportContent.setVisibility(4);
        this.mLoadingProgressBar.setVisibility(0);
        this.mNoModelLabel.setVisibility(4);
        this.mListView.setChoiceMode(1);
        this.mListView.setOnItemClickListener(this);
        this.mBtnDelete.setOnClickListener(this);
        this.mBtnImport.setOnClickListener(this);
        showDetail(false);
    }

    public void onStart() {
        super.onStart();
        this.mLoadingTask = new MyAsyncTask();
        this.mLoadingTask.execute(new Void[0]);
    }

    public void onStop() {
        super.onStop();
        if (this.mLoadingTask != null) {
            this.mLoadingTask.cancel(true);
        }
    }

    private boolean loadImportModels() {
        File dir;
        BufferedReader br;
        Exception e;
        Throwable th;
        this.mImportNames.clear();
        if (Utilities.hasSDCard(this)) {
            dir = new File(EXTEN_PATH);
        } else {
            dir = new File(LOCATION);
        }
        if (!dir.exists()) {
            return false;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            Log.w(TAG, "dir is not a directory:" + dir.getAbsolutePath());
            return false;
        } else if (files.length == 0) {
            return false;
        } else {
            int N = files.length;
            for (int i = 0; i < N; i++) {
                if (this.mLoadingTask.isCancelled()) {
                    return false;
                }
                if (files[i].isDirectory()) {
                    File manifest = new File(files[i], "manifest.csv");
                    if (manifest.exists()) {
                        br = null;
                        try {
                            BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(manifest)));
                            try {
                                String[] kv = br2.readLine().split(",");
                                if (kv[0].equals(DBOpenHelper.KEY_FPV) && Integer.parseInt(kv[1]) == Utilities.getCurrentMode()) {
                                    this.mImportNames.add(files[i].getName());
                                }
                                if (br2 != null) {
                                    try {
                                        br2.close();
                                    } catch (IOException e2) {
                                    }
                                }
                            } catch (Exception e3) {
                                e = e3;
                                br = br2;
                                try {
                                    Log.e(TAG, "get import manifest error:" + e.getMessage());
                                    if (br != null) {
                                        try {
                                            br.close();
                                        } catch (IOException e4) {
                                        }
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                br = br2;
                            }
                        } catch (Exception e5) {
                            e = e5;
                            Log.e(TAG, "get import manifest error:" + e.getMessage());
                            if (br != null) {
                                br.close();
                            }
                        }
                    } else {
                        continue;
                    }
                }
            }
            if (this.mImportNames.size() != 0) {
                return true;
            }
            return false;
        }
        throw th;
        if (br != null) {
            try {
                br.close();
            } catch (IOException e6) {
            }
        }
        throw th;
    }

    private boolean getModelDetail(File folder) {
        IOException e;
        Throwable th;
        Exception e2;
        File manifest = new File(folder, "manifest.csv");
        if (manifest.exists()) {
            BufferedReader br = null;
            try {
                BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(manifest)));
                try {
                    br2.readLine();
                    br2.readLine();
                    String[] kv = br2.readLine().split(",");
                    if (kv[0].equals(DBOpenHelper.KEY_NAME)) {
                        this.mNameLabel.setText(kv[1]);
                    }
                    kv = br2.readLine().split(",");
                    if (kv[0].equals("type")) {
                        int image_id = TypeImageResource.typeTransformToImageId(this, Integer.parseInt(kv[1]));
                        String typeName = TypeImageResource.getTypeName(this, Integer.parseInt(kv[1]));
                        this.mTypeImage.setImageResource(image_id);
                        this.mTypeName.setText(typeName);
                    }
                    if (br2 != null) {
                        try {
                            br2.close();
                        } catch (IOException e3) {
                        }
                    }
                } catch (FileNotFoundException e4) {
                    br = br2;
                    if (br != null) {
                        return false;
                    }
                    try {
                        br.close();
                        return false;
                    } catch (IOException e5) {
                        return false;
                    }
                } catch (IOException e6) {
                    e = e6;
                    br = br2;
                    try {
                        Log.e(TAG, "Reading Manifest failed" + e.getMessage());
                        if (br != null) {
                            return false;
                        }
                        try {
                            br.close();
                            return false;
                        } catch (IOException e7) {
                            return false;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (br != null) {
                            try {
                                br.close();
                            } catch (IOException e8) {
                            }
                        }
                        throw th;
                    }
                } catch (NullPointerException e9) {
                    br = br2;
                    Log.e(TAG, "Parse Manifest Error");
                    if (br != null) {
                        return false;
                    }
                    try {
                        br.close();
                        return false;
                    } catch (IOException e10) {
                        return false;
                    }
                } catch (NumberFormatException e11) {
                    br = br2;
                    if (br != null) {
                        return false;
                    }
                    try {
                        br.close();
                        return false;
                    } catch (IOException e12) {
                        return false;
                    }
                } catch (Exception e13) {
                    e2 = e13;
                    br = br2;
                    Log.e(TAG, "Unexpected err:" + e2.getMessage());
                    if (br != null) {
                        return false;
                    }
                    try {
                        br.close();
                        return false;
                    } catch (IOException e14) {
                        return false;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    br = br2;
                    if (br != null) {
                        br.close();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e15) {
                if (br != null) {
                    return false;
                }
                br.close();
                return false;
            } catch (IOException e16) {
                e = e16;
                Log.e(TAG, "Reading Manifest failed" + e.getMessage());
                if (br != null) {
                    return false;
                }
                br.close();
                return false;
            } catch (NullPointerException e17) {
                Log.e(TAG, "Parse Manifest Error");
                if (br != null) {
                    return false;
                }
                br.close();
                return false;
            } catch (NumberFormatException e18) {
                if (br != null) {
                    return false;
                }
                br.close();
                return false;
            } catch (Exception e19) {
                e2 = e19;
                Log.e(TAG, "Unexpected err:" + e2.getMessage());
                if (br != null) {
                    return false;
                }
                br.close();
                return false;
            }
        }
        File icon = new File(folder, "icon.jpg");
        if (icon.exists()) {
            Utilities.setImageUri(this, this.mPicture, Uri.fromFile(icon));
            if (this.mPicture.getDrawable() == null) {
                this.mPicture.setImageResource(R.drawable.model_select_missed);
            }
        } else {
            this.mPicture.setImageResource(R.drawable.model_select_missed);
        }
        return true;
    }

    private void showDetail(boolean show) {
        if (show) {
            if (this.isFirstShowDetail) {
                this.isFirstShowDetail = false;
                this.mImportDetail.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this, R.anim.layout_anim_slide_right));
                this.mImportDetail.scheduleLayoutAnimation();
            }
            this.mNoDetailLabel.setVisibility(4);
            this.mPictureFrame.setVisibility(0);
            this.mTypeFrame.setVisibility(0);
            this.mBtnDelete.setVisibility(0);
            this.mBtnImport.setVisibility(0);
            return;
        }
        this.isFirstShowDetail = true;
        this.mNoDetailLabel.setVisibility(0);
        this.mPictureFrame.setVisibility(4);
        this.mTypeFrame.setVisibility(4);
        this.mBtnDelete.setVisibility(4);
        this.mBtnImport.setVisibility(4);
        if (this.mListView.getCount() == 1) {
            this.mListView.clearChoices();
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File folder;
        String import_folder = (String) parent.getItemAtPosition(position);
        if (Utilities.hasSDCard(this)) {
            folder = new File(EXTEN_PATH, import_folder);
        } else {
            folder = new File(LOCATION, import_folder);
        }
        if (getModelDetail(folder)) {
            showDetail(true);
            this.mCurrentImport = import_folder;
            return;
        }
        showDetail(false);
        this.mCurrentImport = null;
    }

    public void onClick(View v) {
        if (v.equals(this.mBtnImport)) {
            this.mProgressDialog = MyProgressDialog.show(this, null, getResources().getText(R.string.str_dialog_waiting), false, false);
            new ImportAsyncTask().execute(new Void[0]);
        } else if (v.equals(this.mBtnDelete)) {
            new Builder(this).setMessage(getResources().getString(R.string.str_delete_confirm, new Object[]{this.mCurrentImport})).setPositiveButton(17039370, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (ModelImport.this.showDialogIfNeeded()) {
                        ModelImport.this.mProgressDialog = MyProgressDialog.show(ModelImport.this, null, ModelImport.this.getResources().getText(R.string.str_dialog_waiting), false, false);
                        new DeleteAsyncTask().execute(new Void[0]);
                        return;
                    }
                    ModelImport.this.deleteImport(ModelImport.this.mCurrentImport);
                    if (ModelImport.this.loadImportModels()) {
                        ModelImport.this.mListView.setAdapter(null);
                        ModelImport.this.mModelAdapter = new ArrayAdapter(ModelImport.this, 17367062, ModelImport.this.mImportNames);
                        ModelImport.this.mListView.setAdapter(ModelImport.this.mModelAdapter);
                        ModelImport.this.showDetail(false);
                        return;
                    }
                    ModelImport.this.mImportContent.setVisibility(4);
                    ModelImport.this.mLoadingProgressBar.setVisibility(4);
                    ModelImport.this.mNoModelLabel.setVisibility(0);
                }
            }).setNegativeButton(17039360, null).create().show();
        }
    }

    private boolean showDialogIfNeeded() {
        File folder;
        if (Utilities.hasSDCard(this)) {
            folder = new File(EXTEN_PATH, this.mCurrentImport);
        } else {
            folder = new File(LOCATION, this.mCurrentImport);
        }
        if (folder.list().length >= 5) {
            return true;
        }
        File parent_folder;
        if (Utilities.hasSDCard(this)) {
            parent_folder = new File(EXTEN_PATH, this.mCurrentImport);
        } else {
            parent_folder = new File(LOCATION, this.mCurrentImport);
        }
        if (parent_folder.list().length < 5) {
            return false;
        }
        return true;
    }

    private void deleteImport(String folder) {
        File dir;
        if (Utilities.hasSDCard(this)) {
            dir = new File(EXTEN_PATH, folder);
        } else {
            dir = new File(LOCATION, folder);
        }
        File[] files = dir.listFiles();
        if (files != null) {
            for (File delete : files) {
                delete.delete();
            }
        }
        dir.delete();
    }
}
