package com.yuneec.model_select;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.yuneec.database.DBOpenHelper;
import com.yuneec.database.DataProvider;
import com.yuneec.database.DataProviderHelper;
import com.yuneec.database.ModelBrief;
import com.yuneec.flight_settings.FlightSettings;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;
import com.yuneec.uartcontroller.UARTController;
import com.yuneec.widget.MyProgressDialog;
import com.yuneec.widget.MyToast;
import com.yuneec.widget.StatusbarView;
import java.io.File;

public class EditModel extends Activity implements OnClickListener, OnScanCompletedListener {
    private static final int CROP_ERR = -1;
    private static final int CROP_NEEDED = 1;
    private static final int CROP_NONEED = 0;
    private static final int CROP_REQ = 1002;
    static final String IMAGE_SUFFIX = ".jpg";
    private static final int MODEL_TYPE = 1;
    private static final int PHOTO_HEIGHT = 289;
    private static final int PHOTO_WIDTH = 515;
    private static final int PICKICON_REQ = 1000;
    private static final String TAG = "EditModel";
    private static final int TAKEPICTURE_REQ = 1001;
    private boolean isMediaScanning = false;
    private ModelBrief mBrief;
    private UARTController mController;
    private int mIconResourceId = R.drawable.model_select_missed;
    private long mModelId;
    private int mModelSubType = -1;
    private int mModelType;
    private Uri mOutput;
    private SharedPreferences mPrefs;
    private MyProgressDialog mProgressDialog;
    private RelativeLayout model_image_frame;
    private EditText model_name_title;
    private ImageView model_photo;
    private Button model_reset;
    private Button model_save;
    private ImageView model_type;
    private int originType;
    private TextView type_name;

    private class OnCreateNewModelFinishedTask extends AsyncTask<Long, Void, Void> {
        private OnCreateNewModelFinishedTask() {
        }

        protected Void doInBackground(Long... params) {
            Long model_id = params[0];
            if (Utilities.checkDefaultMixingDataExisted(EditModel.this, EditModel.this.mModelSubType)) {
                DataProviderHelper.resetMixingChannel(EditModel.this, model_id.longValue());
            } else {
                Log.w(EditModel.TAG, "Type:" + EditModel.this.mModelSubType + " Default Data not Found");
            }
            Utilities.sendAllDataToFlightControl(EditModel.this.getApplication(), model_id.longValue(), EditModel.this.mController);
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            EditModel.this.mProgressDialog.dismiss();
            EditModel.this.mController.stopReading();
            EditModel.this.mController = null;
            EditModel.this.finish();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(128);
        setContentView(R.layout.model_select_edit_model);
        Utilities.setStatusBarLeftText(this, (StatusbarView) findViewById(R.id.statusbar_of_edit_model));
        this.mPrefs = getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0);
        this.model_photo = (ImageView) findViewById(R.id.model_photo);
        this.model_type = (ImageView) findViewById(R.id.model_type);
        this.model_reset = (Button) findViewById(R.id.model_reset);
        this.model_save = (Button) findViewById(R.id.model_save);
        this.model_name_title = (EditText) findViewById(R.id.model_name_title);
        this.type_name = (TextView) findViewById(R.id.model_type_name);
        this.model_image_frame = (RelativeLayout) findViewById(R.id.model_image_frame);
        getIntentValue();
        setInitValue();
        this.model_type.setOnClickListener(this);
        this.type_name.setOnClickListener(this);
        this.model_reset.setOnClickListener(this);
        this.model_save.setOnClickListener(this);
        this.model_name_title.setOnClickListener(this);
        this.model_image_frame.setOnClickListener(this);
        this.model_name_title.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId != 6) {
                    return false;
                }
                ((InputMethodManager) v.getContext().getSystemService("input_method")).hideSoftInputFromWindow(v.getWindowToken(), 0);
                EditModel.this.model_name_title.setCompoundDrawablesWithIntrinsicBounds(EditModel.this.getResources().getDrawable(R.drawable.model_select_y), null, null, null);
                return true;
            }
        });
    }

    private void getIntentValue() {
        Intent intent = getIntent();
        this.mModelId = intent.getLongExtra(DBOpenHelper.KEY_ID, -2);
        this.originType = intent.getIntExtra("type", 0);
    }

    private void setInitValue() {
        if (this.mModelId == -2) {
            Log.e(TAG, "Cannnot get Model Id!!");
            finish();
            return;
        }
        if (this.mModelId != -1) {
            this.type_name.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.model_select_y), null, null, null);
            this.mModelSubType = this.originType;
            this.mModelType = this.mModelSubType / 100;
        } else {
            this.type_name.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.model_select_n), null, null, null);
            this.mModelType = this.originType;
            this.mModelSubType = -1;
        }
        this.model_name_title.setSelection(this.model_name_title.getText().length());
        if (this.mModelId != -1) {
            this.mBrief = loadModel(this.mModelId);
            this.mIconResourceId = this.mBrief.iconResourceId;
            setupView();
        }
        Log.v(TAG, "mModelType is : " + this.mModelType);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.model_photo:
                return;
            case R.id.model_name_title:
                if (!TextUtils.isEmpty(this.model_name_title.getText().toString())) {
                    this.model_name_title.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.model_select_y), null, null, null);
                    return;
                }
                return;
            case R.id.model_image_frame:
            case R.id.model_type:
            case R.id.model_type_name:
                Intent intent = new Intent(this, TypeImageSelect.class);
                intent.putExtra("model_id", this.mModelId);
                intent.putExtra("primary_type", this.mModelType);
                intent.putExtra("sub_type", this.mModelSubType);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.type_open_enter, R.anim.type_open_exit);
                return;
            case R.id.model_reset:
                resetModel();
                return;
            case R.id.model_save:
                saveModel();
                return;
            default:
                Log.v(TAG, "wrong click");
                return;
        }
    }

    public void channelSetting() {
    }

    public void saveModel() {
        if (this.isMediaScanning) {
            Log.w(TAG, "Media is Scanning,should not happen");
            MyToast.makeText((Context) this, (int) R.string.str_dialog_waiting, 0, 0);
        } else if (TextUtils.isEmpty(this.model_name_title.getText().toString()) || this.model_name_title.getText().toString().equals(" ")) {
            MyToast.makeText((Context) this, (int) R.string.str_model_name_null, 0, 1).show();
        } else {
            ContentValues cv = new ContentValues();
            cv.put(DBOpenHelper.KEY_NAME, this.model_name_title.getText().toString());
            cv.put(DBOpenHelper.KEY_ICON, Integer.valueOf(this.mIconResourceId));
            cv.put("type", Integer.valueOf(this.mModelSubType));
            if (Utilities.PROJECT_TAG.equals(Utilities.PROJECT_TAG) || "ST12".equals(Utilities.PROJECT_TAG)) {
                cv.put(DBOpenHelper.KEY_RX_ANALOG_NUM, Integer.valueOf(10));
                cv.put(DBOpenHelper.KEY_RX_SWITCH_NUM, Integer.valueOf(2));
            }
            Log.v(TAG, "init ..........." + this.mIconResourceId);
            if (this.mModelSubType < 100 || this.mModelSubType > DataProviderHelper.MODEL_TYPE_LAST || this.mModelSubType == 100 || this.mModelSubType == 200 || this.mModelSubType == DataProviderHelper.MODEL_TYPE_GLIDER_BASE || this.mModelSubType == DataProviderHelper.MODEL_TYPE_MULITCOPTER_BASE) {
                MyToast.makeText((Context) this, (int) R.string.str_type_empty, 0, 1).show();
                return;
            }
            ContentResolver cr = getContentResolver();
            if (this.mModelId == -1) {
                cv.put(DBOpenHelper.KEY_FPV, Integer.valueOf(Utilities.getCurrentMode()));
                Uri insert_uri = cr.insert(DataProvider.MODEL_URI, cv);
                if (insert_uri == null) {
                    MyToast.makeText((Context) this, (int) R.string.str_insert_fail, 0, 1).show();
                    return;
                }
                this.mPrefs.edit().putLong("current_model_id", ContentUris.parseId(insert_uri)).commit();
                this.mPrefs.edit().putLong("current_model_type", (long) this.mModelSubType).commit();
                this.mController = UARTController.getInstance();
                if (this.mController != null) {
                    this.mProgressDialog = MyProgressDialog.show(this, null, getResources().getString(R.string.str_dialog_waiting), false, false);
                    this.mController.startReading();
                    new OnCreateNewModelFinishedTask().execute(new Long[]{Long.valueOf(ContentUris.parseId(insert_uri))});
                    return;
                }
                Log.e(TAG, "Should never be here,controller == null");
                return;
            }
            this.mPrefs.edit().putLong("current_model_type", (long) this.mModelSubType).commit();
            if (cr.update(ContentUris.withAppendedId(DataProvider.MODEL_URI, this.mModelId), cv, null, null) == 0) {
                MyToast.makeText((Context) this, (int) R.string.str_update_fail, 0, 1).show();
            } else {
                finish();
            }
        }
    }

    private ModelBrief loadModel(long id) {
        Cursor c = getContentResolver().query(ContentUris.withAppendedId(DataProvider.MODEL_URI, id), new String[]{DBOpenHelper.KEY_ID, DBOpenHelper.KEY_NAME, DBOpenHelper.KEY_ICON, "type"}, null, null, null);
        if (c == null || !c.moveToFirst()) {
            Log.e(TAG, "Cursor open failed,Cannot get Model Brief");
            return null;
        }
        ModelBrief m = new ModelBrief();
        m.name = c.getString(c.getColumnIndex(DBOpenHelper.KEY_NAME));
        m.iconResourceId = c.getInt(c.getColumnIndex(DBOpenHelper.KEY_ICON));
        m.type = c.getInt(c.getColumnIndex("type"));
        c.close();
        return m;
    }

    private void setupView() {
        this.model_name_title.setText(this.mBrief.name);
        if (this.mBrief.iconResourceId == 0) {
            this.model_photo.setImageResource(R.drawable.model_select_missed);
        } else {
            this.model_photo.setImageResource(this.mBrief.iconResourceId);
        }
        setTypeImage();
    }

    private void setTypeImage() {
        this.model_type.setImageResource(TypeImageResource.typeTransformToImageId(this, this.mBrief.type));
        this.type_name.setText(TypeImageResource.getTypeName(this, this.mBrief.type));
    }

    public Uri getImagePath(int id) {
        return Uri.parse("android.resource://" + getResources().getResourcePackageName(id) + "/" + getResources().getResourceTypeName(id) + "/" + getResources().getResourceEntryName(id));
    }

    public void resetModel() {
        this.model_photo.setImageResource(R.drawable.model_select_missed);
        this.model_type.setImageResource(R.drawable.model_select_type);
        this.type_name.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.model_select_n), null, null, null);
        this.type_name.setText(getString(R.string.str_edit_type_title));
        this.model_name_title.setText("");
        this.mIconResourceId = R.drawable.model_select_missed;
        if (this.mModelId != -1) {
            this.mModelSubType = (this.originType / 100) * 100;
            this.mModelType = this.mModelSubType / 100;
            return;
        }
        this.mModelType = this.originType;
        this.mModelSubType = -1;
    }

    private void showSelectPictureAcitivity() {
        Intent intent = new Intent();
        intent.setFlags(1073741824);
        intent.setAction("android.intent.action.GET_CONTENT");
        intent.addCategory("android.intent.category.OPENABLE");
        intent.setType("image/*");
        intent.putExtra("output", this.mOutput);
        intent.setClassName("com.android.gallery3d", "com.android.gallery3d.app.Gallery");
        startActivityForResult(intent, PICKICON_REQ);
    }

    private Uri prepare() {
        File dir = new File(Environment.getExternalStorageDirectory(), "ModelsIcon");
        if (!(dir.exists() || dir.mkdir())) {
            Log.e(TAG, "mkdir error dir:" + dir.getAbsolutePath());
        }
        Time now = new Time();
        now.setToNow();
        String filename = dir + "/" + "Icon-" + now.format("%Y-%m-%d %H.%M.%S");
        return Uri.fromFile(checkVaild(filename, 1, new File(new StringBuilder(String.valueOf(filename)).append(".jpg").toString())));
    }

    private File checkVaild(String oldname, int suffix_count, File file) {
        if (!file.exists()) {
            return file;
        }
        return checkVaild(oldname, suffix_count + 1, new File(new StringBuilder(String.valueOf(oldname)).append("-").append(suffix_count).append(".jpg").toString()));
    }

    private void updateGallery(Uri uri) {
        MediaScannerConnection.scanFile(this, new String[]{Uri.decode(uri.getPath())}, null, this);
        this.isMediaScanning = true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            if (requestCode == PICKICON_REQ) {
                if (cropIfNeeded(data) == 0) {
                    Utilities.setImageUri(this, this.model_photo, data.getData());
                }
            } else if (requestCode == 1001) {
                startCrop(this.mOutput, false);
            } else if (requestCode == 1002) {
                Utilities.setImageUri(this, this.model_photo, this.mOutput);
                updateGallery(this.mOutput);
                this.mOutput = null;
            } else if (requestCode != 1) {
            } else {
                if (data == null) {
                    Log.e(TAG, "image name is empty");
                    return;
                }
                this.mModelSubType = data.getIntExtra("sub_type", 0);
                int type_icon_id = TypeImageResource.typeTransformToImageId(this, this.mModelSubType);
                Log.v(TAG, "---------->get ModelType is :" + this.mModelSubType);
                this.model_type.setImageResource(type_icon_id);
                this.model_photo.setImageResource(type_icon_id);
                this.mIconResourceId = type_icon_id;
                this.type_name.setText(data.getStringExtra("type_name"));
                this.type_name.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.model_select_y), null, null, null);
            }
        } else if (resultCode == 0 && requestCode == 1002 && this.mOutput != null) {
            new File(this.mOutput.getPath()).delete();
            this.mOutput = null;
        }
    }

    private int cropIfNeeded(Intent intent) {
        if (intent == null) {
            Log.w(TAG, "cropIfNeeded returned uri is null");
            return -1;
        }
        Uri uri = intent.getData();
        if (uri == null) {
            Log.w(TAG, "cropIfNeeded returned uri is null");
            return -1;
        }
        String location = getIconLocation(uri);
        if (location.startsWith(new File(Environment.getExternalStorageDirectory(), "ModelsIcon").getAbsolutePath())) {
            Log.i(TAG, "picked icon " + location + " was in ModelIcons Folder,no need to crop");
            return 0;
        }
        startCrop(uri, true);
        return 1;
    }

    private String getIconLocation(Uri uri) {
        if (uri.getScheme().startsWith("file")) {
            return Uri.decode(uri.getPath());
        }
        if (uri.getScheme().startsWith("content")) {
            Cursor c = Media.query(getContentResolver(), uri, new String[]{"_data"});
            c.moveToFirst();
            String location = c.getString(c.getColumnIndex("_data"));
            c.close();
            return location;
        }
        Log.w(TAG, "getIconLocation Unkown Uri: " + uri);
        return null;
    }

    public void onScanCompleted(String path, Uri uri) {
        this.isMediaScanning = false;
    }

    private void startCrop(Uri uri, boolean newOutput) {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(uri, "image/*");
        cropIntent.setFlags(1073741824);
        cropIntent.putExtra("aspectX", 9);
        cropIntent.putExtra("aspectY", 5);
        cropIntent.putExtra("outputX", PHOTO_WIDTH);
        cropIntent.putExtra("outputY", PHOTO_HEIGHT);
        cropIntent.putExtra("scale", true);
        cropIntent.putExtra("outputFormat", CompressFormat.JPEG.toString());
        if (newOutput) {
            this.mOutput = prepare();
        }
        cropIntent.putExtra("output", this.mOutput);
        startActivityForResult(cropIntent, 1002);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode != 3 || event.isCanceled()) {
            return super.onKeyUp(keyCode, event);
        }
        Utilities.backToFlightScreen(this);
        return true;
    }
}
