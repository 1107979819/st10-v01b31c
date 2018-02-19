package com.yuneec.galleryloader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.yuneec.IPCameraManager.IPCameraManager;
import com.yuneec.IPCameraManager.IPCameraManager.GetMediaFileCallback;
import com.yuneec.flight_settings.FlightSettings;
import com.yuneec.flightmode15.R;
import com.yuneec.model_select.ModelSelectMain;
import java.io.File;
import java.util.ArrayList;

public class Gallery extends Activity implements OnItemClickListener, OnItemLongClickListener {
    public static final String FILE_DELURL_PREFIX_1 = "http://192.168.73.254/sddel.htm?FILE=";
    public static final String FILE_DELURL_PREFIX_2 = "http://192.168.42.1/DCIM/100MEDIA/";
    public static final String FILE_GETURL_PREFIX_1 = "http://192.168.73.254/sdget.htm?FILE=";
    public static final String FILE_GETURL_PREFIX_2 = "http://192.168.42.1/DCIM/100MEDIA/";
    private static final String TAG = "Gallery";
    public static String mCurrentFlieDelUrl;
    public static String mCurrentFlieGetUrl;
    private GridView mGridView;
    private Handler mHandler = new Handler();
    private IPCameraManager mIPCameraManager;
    private int mLongpressPosition = -1;
    private Callback mSelectActionMode = new Callback() {
        TextView mActionText;

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
            Gallery.this.toggleSelectAll(false);
            GalleryLoaderAdapter gla = (GalleryLoaderAdapter) Gallery.this.mGridView.getAdapter();
            if (gla != null) {
                gla.setSelectionMode(false);
            }
            Gallery.this.mGridView.setChoiceMode(0);
            Gallery.this.mGridView.setOnItemClickListener(Gallery.this);
            Gallery.this.mGridView.setOnItemLongClickListener(Gallery.this);
            Gallery.this.mLongpressPosition = -1;
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            View v = LayoutInflater.from(Gallery.this).inflate(R.layout.gallery_select_actionbar_layout, null);
            this.mActionText = (TextView) v.findViewById(R.id.action_text);
            this.mActionText.setVisibility(8);
            mode.setCustomView(v);
            Gallery.this.getMenuInflater().inflate(R.menu.photo_gallery_action, menu);
            GalleryLoaderAdapter gla = (GalleryLoaderAdapter) Gallery.this.mGridView.getAdapter();
            if (gla != null) {
                gla.setSelectionMode(true);
            }
            Gallery.this.mGridView.setChoiceMode(2);
            Gallery.this.mGridView.setOnItemClickListener(null);
            Gallery.this.mGridView.setOnItemLongClickListener(null);
            if (Gallery.this.mLongpressPosition != -1) {
                Gallery.this.mGridView.setItemChecked(Gallery.this.mLongpressPosition, true);
            }
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            boolean z = false;
            if (Gallery.this.mGridView.getVisibility() != 0) {
                return false;
            }
            switch (item.getItemId()) {
                case R.id.action_delete:
                    ArrayList<String> collection = collectSelection();
                    if (collection.size() == 0) {
                        Toast.makeText(Gallery.this.getApplicationContext(), R.string.str_gallery_menu_noselection, 0).show();
                        return true;
                    }
                    new SelectionActionTask(collection).execute(new String[]{"delete"});
                    return true;
                case R.id.action_select_all:
                    if (Gallery.this.mToggleSelectAll) {
                        Gallery.this.toggleSelectAll(false);
                        item.setTitle(R.string.str_gallery_menu_select_all);
                    } else {
                        Gallery.this.toggleSelectAll(true);
                        item.setTitle(R.string.str_gallery_menu_deselect_all);
                    }
                    Gallery gallery = Gallery.this;
                    if (!Gallery.this.mToggleSelectAll) {
                        z = true;
                    }
                    gallery.mToggleSelectAll = z;
                    return true;
                default:
                    return true;
            }
        }

        private ArrayList<String> collectSelection() {
            SparseBooleanArray checkedItemPositions = Gallery.this.mGridView.getCheckedItemPositions();
            ArrayList<String> list = new ArrayList();
            for (int i = 0; i < checkedItemPositions.size(); i++) {
                int position = checkedItemPositions.keyAt(i);
                if (checkedItemPositions.valueAt(i)) {
                    list.add((String) Gallery.this.mGridView.getItemAtPosition(position));
                }
            }
            return list;
        }
    };
    private boolean mToggleSelectAll = false;

    private class SelectionActionTask extends AsyncTask<String, Object, String> {
        private ProgressDialog dialog;
        private String mAction;
        private ArrayList<String> mFilelist;

        public SelectionActionTask(ArrayList<String> mFilelist) {
            this.mFilelist = mFilelist;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(Gallery.this);
            this.dialog.setCanceledOnTouchOutside(false);
            this.dialog.setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    SelectionActionTask.this.cancel(true);
                }
            });
            this.dialog.setProgressStyle(1);
            this.dialog.setMessage(Gallery.this.getResources().getString(R.string.str_gallery_menu_delete));
            this.dialog.setMax(0);
            this.dialog.show();
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            this.dialog.setProgress(this.dialog.getMax());
            if ("delete".equals(this.mAction)) {
                Gallery.this.reloadMedia();
            }
            Gallery.this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    SelectionActionTask.this.dialog.dismiss();
                }
            }, 200);
        }

        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            String msg = values[0];
            if (msg != null) {
                this.dialog.setMessage(msg);
            }
            int max = ((Integer) values[1]).intValue();
            if (max != 0) {
                this.dialog.setMax(max);
            }
            this.dialog.setProgress(((Integer) values[2]).intValue());
        }

        protected void onCancelled(String result) {
            super.onCancelled(result);
            if (IPCameraManager.HTTP_RESPONSE_CODE_USER_CANCELLED.equals(result)) {
                Toast.makeText(Gallery.this.getApplicationContext(), R.string.str_gallery_operation_cancelled, 0).show();
            } else {
                Log.i(Gallery.TAG, "onCancelled result:" + result);
            }
        }

        protected String doInBackground(String... params) {
            if ("delete".equals(params[0])) {
                this.mAction = "delete";
                this.dialog.setProgressNumberFormat("%1d/%2d");
                this.dialog.setMessage(Gallery.this.getResources().getString(R.string.str_gallery_menu_delete));
                Object[] update = new Object[3];
                update[1] = Integer.valueOf(this.mFilelist.size());
                update[2] = Integer.valueOf(0);
                publishProgress(update);
                for (int i = 0; i < this.mFilelist.size(); i++) {
                    if (isCancelled()) {
                        return IPCameraManager.HTTP_RESPONSE_CODE_USER_CANCELLED;
                    }
                    String filename = (String) this.mFilelist.get(i);
                    publishProgress(new Object[]{new StringBuilder(String.valueOf(filename)).append(" ").append(i + 1).append("/").append(N).toString(), Integer.valueOf(0), Integer.valueOf(i + 1)});
                    String result = Gallery.this.mIPCameraManager.rawRequestBlock(Gallery.mCurrentFlieDelUrl + filename, false, null);
                    Log.d(Gallery.TAG, "delete " + filename + " result:" + result);
                    File cache_file = ((GalleryLoaderAdapter) Gallery.this.mGridView.getAdapter()).getImageLoader().getFileCache(Gallery.mCurrentFlieGetUrl + filename);
                    if (cache_file != null) {
                        cache_file.delete();
                    }
                    if (IPCameraManager.HTTP_RESPONSE_CODE_USER_CANCELLED.equals(result)) {
                        return result;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
            }
            return null;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_gallery_main);
        this.mGridView = (GridView) findViewById(R.id.gridView);
        this.mGridView.setOnItemClickListener(this);
        this.mGridView.setOnItemLongClickListener(this);
        this.mGridView.setChoiceMode(0);
        int camera_type = getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0).getInt(FlightSettings.CAMERA_TYPE_VALUE, 0);
        if ((camera_type & 1) == 1) {
            this.mIPCameraManager = IPCameraManager.getIPCameraManager(this, 101);
            mCurrentFlieGetUrl = FILE_GETURL_PREFIX_1;
            mCurrentFlieDelUrl = FILE_DELURL_PREFIX_1;
        } else if ((camera_type & 4) == 4) {
            this.mIPCameraManager = IPCameraManager.getIPCameraManager(this, 102);
            mCurrentFlieGetUrl = "http://192.168.42.1/DCIM/100MEDIA/";
            mCurrentFlieDelUrl = "http://192.168.42.1/DCIM/100MEDIA/";
        } else if ((camera_type & 8) == 8) {
            this.mIPCameraManager = IPCameraManager.getIPCameraManager(this, 104);
            mCurrentFlieGetUrl = "http://192.168.42.1/DCIM/100MEDIA/";
            mCurrentFlieDelUrl = "http://192.168.42.1/DCIM/100MEDIA/";
        } else {
            this.mIPCameraManager = IPCameraManager.getIPCameraManager(this, 100);
            mCurrentFlieGetUrl = FILE_GETURL_PREFIX_1;
            mCurrentFlieDelUrl = FILE_DELURL_PREFIX_1;
        }
        loadMedia();
    }

    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        this.mIPCameraManager.finish();
        this.mIPCameraManager = null;
        clearLoaderMemoryCache();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photo_gallery, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_select) {
            startActionMode(this.mSelectActionMode);
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearLoaderMemoryCache() {
        GalleryLoaderAdapter gla = (GalleryLoaderAdapter) this.mGridView.getAdapter();
        if (gla != null) {
            gla.getImageLoader().clearMemoryCache();
        }
    }

    private void reloadMedia() {
        clearLoaderMemoryCache();
        this.mGridView.setAdapter(null);
        loadMedia();
    }

    private void loadMedia() {
        findViewById(R.id.network_err).setVisibility(4);
        findViewById(R.id.gridView).setVisibility(4);
        findViewById(R.id.progressBar).setVisibility(0);
        this.mIPCameraManager.getMediaFile(new GetMediaFileCallback() {
            public void MediaFileGot(String[] result) {
                if (result == null) {
                    Gallery.this.findViewById(R.id.network_err).setVisibility(0);
                    Gallery.this.findViewById(R.id.progressBar).setVisibility(4);
                    return;
                }
                ArrayList<String> onlyImg = new ArrayList();
                for (int i = 0; i < result.length; i++) {
                    if (!result[i].endsWith(".avi")) {
                        onlyImg.add(result[i]);
                    }
                }
                String[] array = new String[onlyImg.size()];
                onlyImg.toArray(array);
                for (String str : array) {
                    Log.i(Gallery.TAG, "media:" + str);
                }
                Gallery.this.mGridView.setAdapter(new GalleryLoaderAdapter(Gallery.this, array));
                Gallery.this.findViewById(R.id.gridView).setVisibility(0);
                Gallery.this.findViewById(R.id.progressBar).setVisibility(4);
            }
        });
    }

    private void toggleSelectAll(boolean selectAll) {
        ListAdapter adapter = this.mGridView.getAdapter();
        if (adapter != null) {
            int count = adapter.getCount();
            for (int i = 0; i < count; i++) {
                this.mGridView.setItemChecked(i, selectAll);
            }
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String url = (String) parent.getItemAtPosition(position);
        if (url.endsWith(ModelSelectMain.IMAGE_SUFFIX)) {
            File file = ((GalleryLoaderAdapter) parent.getAdapter()).getImageLoader().getFileCache(mCurrentFlieGetUrl + url);
            if (file != null) {
                Intent intent = new Intent(this, ImageViewActivity.class);
                intent.putExtra("url", file.getAbsolutePath());
                startActivity(intent);
            }
        }
    }

    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        this.mLongpressPosition = position;
        startActionMode(this.mSelectActionMode);
        return true;
    }
}
