package com.yuneec.flight_settings;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.yuneec.flightmode15.MainActivity;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class QuickReview extends Activity implements OnClickListener, OnCheckedChangeListener, OnItemClickListener, OnItemLongClickListener {
    private static final int SHOW_DIALOG_FILE_SELECTED_NUM = 5;
    private static final String TAG = "QuickReview";
    private Button mBtnCancel;
    private Button mBtnDelete;
    private boolean mInSelection = false;
    private ListView mListView;
    private TextView mNoVideoLabel;
    private String[] mShortnameVideoList = null;
    private ToggleButton mTogSelectAll;
    private FilenameFilter mVideoFilter = new FilenameFilter() {
        public boolean accept(File dir, String filename) {
            if (filename == null || filename.lastIndexOf(".avc") == -1) {
                return false;
            }
            return true;
        }
    };

    private class DeleteTask extends AsyncTask<ArrayList<String>, Void, Void> {
        private DeleteTask() {
        }

        protected Void doInBackground(ArrayList<String>... params) {
            QuickReview.this.deleteVideoInternal(params[0]);
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            QuickReview.this.refreshList();
            Utilities.dismissProgressDialog();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(128);
        setContentView(R.layout.quick_review_main);
        this.mListView = (ListView) findViewById(R.id.listView);
        this.mBtnCancel = (Button) findViewById(R.id.cancel);
        this.mBtnDelete = (Button) findViewById(R.id.delete);
        this.mTogSelectAll = (ToggleButton) findViewById(R.id.toggle_select);
        this.mNoVideoLabel = (TextView) findViewById(R.id.no_video_label);
        this.mBtnCancel.setOnClickListener(this);
        this.mBtnDelete.setOnClickListener(this);
        this.mTogSelectAll.setOnCheckedChangeListener(this);
        this.mShortnameVideoList = loadList();
        if (this.mShortnameVideoList != null && this.mShortnameVideoList.length > 0) {
            this.mNoVideoLabel.setVisibility(4);
            this.mListView.setVisibility(0);
            this.mListView.setAdapter(new ArrayAdapter(this, R.layout.video_list_item, 16908308, this.mShortnameVideoList));
            this.mListView.setOnItemClickListener(this);
            this.mListView.setOnItemLongClickListener(this);
        }
    }

    public void onClick(View v) {
        if (v.equals(this.mBtnCancel)) {
            exitSelectionMode();
        } else if (v.equals(this.mBtnDelete)) {
            deleteVideo();
        }
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.equals(this.mTogSelectAll)) {
            toggleSelectAll(isChecked);
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String shortFileName = (String) parent.getItemAtPosition(position);
        Intent intent = new Intent(this, QuickReviewPlayActivity.class);
        intent.putExtra("short_file_name", shortFileName);
        startActivity(intent);
    }

    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        enterSelectionMode();
        return true;
    }

    private String[] loadList() {
        File dir = new File(MainActivity.SAVED_LOCAL_VIDEO);
        if (dir.exists()) {
            return dir.list(this.mVideoFilter);
        }
        Log.i(TAG, "dir is not existed :/sdcard/FPV-Video/Local");
        return null;
    }

    private void enterSelectionMode() {
        this.mListView.setOnItemClickListener(null);
        this.mListView.setOnItemLongClickListener(null);
        this.mListView.setChoiceMode(2);
        this.mListView.setAdapter(null);
        this.mListView.setAdapter(new ArrayAdapter(this, R.layout.video_list_item_multiple_choice, 16908308, this.mShortnameVideoList));
        this.mBtnCancel.setVisibility(0);
        this.mBtnDelete.setVisibility(0);
        this.mTogSelectAll.setVisibility(0);
        this.mInSelection = true;
    }

    private void exitSelectionMode() {
        this.mListView.setChoiceMode(0);
        this.mListView.setAdapter(null);
        this.mListView.setAdapter(new ArrayAdapter(this, R.layout.video_list_item, 16908308, this.mShortnameVideoList));
        this.mListView.setOnItemClickListener(this);
        this.mListView.setOnItemLongClickListener(this);
        this.mBtnCancel.setVisibility(4);
        this.mBtnDelete.setVisibility(4);
        this.mTogSelectAll.setVisibility(4);
        this.mInSelection = false;
    }

    private void deleteVideo() {
        SparseBooleanArray checkedItemPositions = this.mListView.getCheckedItemPositions();
        ArrayList<String> ToDeleteList = new ArrayList();
        for (int i = 0; i < checkedItemPositions.size(); i++) {
            int position = checkedItemPositions.keyAt(i);
            if (checkedItemPositions.valueAt(i)) {
                ToDeleteList.add((String) this.mListView.getItemAtPosition(position));
            }
        }
        if (ToDeleteList.size() < 5) {
            deleteVideoInternal(ToDeleteList);
            refreshList();
            return;
        }
        Utilities.showProgressDialog(this, null, getResources().getString(R.string.str_dialog_waiting), false, false);
        new DeleteTask().execute(new ArrayList[]{ToDeleteList});
    }

    private void deleteVideoInternal(ArrayList<String> list) {
        for (int i = 0; i < list.size(); i++) {
            File file = new File(MainActivity.SAVED_LOCAL_VIDEO, (String) list.get(i));
            Log.d(TAG, "To delete :" + file.getAbsolutePath());
            if (!file.exists()) {
                Log.w(TAG, "file not existed:" + file.getAbsolutePath());
            } else if (!file.delete()) {
                Log.w(TAG, "delete file fail:" + file.getAbsolutePath());
            }
        }
    }

    private void refreshList() {
        this.mShortnameVideoList = loadList();
        if (this.mShortnameVideoList == null || this.mShortnameVideoList.length <= 0) {
            this.mNoVideoLabel.setVisibility(0);
            this.mListView.setVisibility(4);
            this.mBtnCancel.setVisibility(4);
            this.mBtnDelete.setVisibility(4);
            this.mTogSelectAll.setVisibility(4);
            return;
        }
        exitSelectionMode();
    }

    private void toggleSelectAll(boolean selectAll) {
        int count = this.mListView.getAdapter().getCount();
        for (int i = 0; i < count; i++) {
            this.mListView.setItemChecked(i, selectAll);
        }
    }
}
