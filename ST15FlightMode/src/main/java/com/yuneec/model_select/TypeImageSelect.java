package com.yuneec.model_select;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import com.yuneec.database.DataProvider;
import com.yuneec.database.DataProviderHelper;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;
import com.yuneec.uartcontroller.UARTController;
import com.yuneec.widget.StatusbarView;
import java.util.ArrayList;

public class TypeImageSelect extends Activity {
    private static final int APPS_PER_PAGE = 8;
    private static final String TAG = "TypeImageSelect";
    private int PrimaryType = 4;
    private int SubType = -1;
    private UARTController mController;
    private OnItemClickListener mElementClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent result = new Intent();
            SubTypeInfo e = (SubTypeInfo) parent.getItemAtPosition(position);
            if (TypeImageSelect.this.mModelId == -1 || e.type == TypeImageSelect.this.SubType) {
                result.putExtra("sub_type", e.type);
                result.putExtra("type_name", e.name);
                TypeImageSelect.this.setResult(-1, result);
                TypeImageSelect.this.finish();
                TypeImageSelect.this.overridePendingTransition(R.anim.type_close_enter, R.anim.type_close_exit);
                return;
            }
            TypeImageSelect.this.mController = UARTController.getInstance();
            if (TypeImageSelect.this.mController != null) {
                Utilities.showProgressDialog(TypeImageSelect.this, null, TypeImageSelect.this.getResources().getString(R.string.str_dialog_waiting), false, false);
                TypeImageSelect.this.mController.startReading();
                new ResetModelTypeTask(e.name, e.type).execute(new Long[]{Long.valueOf(TypeImageSelect.this.mModelId)});
                return;
            }
            Log.e(TypeImageSelect.TAG, "Should never be here,controller == null");
        }
    };
    private ArrayList<SubTypeInfo> mGridViewList;
    private PageIndicator mIndicator;
    private long mModelId = -2;
    private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {
        public void onPageSelected(int position) {
            TypeImageSelect.this.mIndicator.setCurrentPage(position);
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        public void onPageScrollStateChanged(int arg0) {
        }
    };
    private ViewPager mViewPager;
    private TextView model_select_model_type;

    private class ResetModelTypeTask extends AsyncTask<Long, Void, Void> {
        private long mModel;
        private int mType;
        private String mTypeName;

        public ResetModelTypeTask(String mTypeName, int mType) {
            this.mTypeName = mTypeName;
            this.mType = mType;
        }

        protected Void doInBackground(Long... params) {
            Long model_id = params[0];
            this.mModel = model_id.longValue();
            if (Utilities.checkDefaultMixingDataExisted(TypeImageSelect.this, this.mType)) {
                DataProviderHelper.resetMixingChannel(TypeImageSelect.this, model_id.longValue());
            } else {
                Log.w(TypeImageSelect.TAG, "Type:" + TypeImageSelect.this.SubType + " Default Data not Found");
            }
            Utilities.sendAllDataToFlightControl(TypeImageSelect.this.getApplication(), model_id.longValue(), TypeImageSelect.this.mController);
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Utilities.dismissProgressDialog();
            TypeImageSelect.this.mController.stopReading();
            TypeImageSelect.this.mController = null;
            Uri uri = ContentUris.withAppendedId(DataProvider.MODEL_URI, this.mModel);
            ContentValues cv = new ContentValues();
            cv.put("type", Integer.valueOf(this.mType));
            TypeImageSelect.this.getContentResolver().update(uri, cv, null, null);
            Intent res = new Intent();
            res.putExtra("sub_type", this.mType);
            res.putExtra("type_name", this.mTypeName);
            TypeImageSelect.this.setResult(-1, res);
            TypeImageSelect.this.finish();
        }
    }

    private class AircraftPagerAdapter extends PagerAdapter {
        private int highlight_position;

        public AircraftPagerAdapter(int subtype) {
            int pos = subtype % 100;
            if (pos == 0) {
                this.highlight_position = -1;
            }
            this.highlight_position = pos;
        }

        public int getCount() {
            int pages = (int) Math.ceil(((double) TypeImageSelect.this.mGridViewList.size()) / 8.0d);
            TypeImageSelect.this.mIndicator.setPageCount(pages);
            return pages;
        }

        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0.equals(arg1);
        }

        public Object instantiateItem(ViewGroup container, int position) {
            int start = position * 8;
            int end = (position + 1) * 8;
            int posInPage = -1;
            if (this.highlight_position > start && this.highlight_position <= end) {
                posInPage = this.highlight_position % 8;
                if (posInPage == 0) {
                    posInPage = 8;
                }
            }
            ArrayList<SubTypeInfo> page_list = new ArrayList(8);
            int i = start;
            while (i < end && i < TypeImageSelect.this.mGridViewList.size()) {
                page_list.add((SubTypeInfo) TypeImageSelect.this.mGridViewList.get(i));
                i++;
            }
            View v = TypeImageSelect.this.getLayoutInflater().inflate(R.layout.model_select_type_image, null);
            GridView gv = (GridView) v;
            gv.setAdapter(new TypeImageAdapter(TypeImageSelect.this.getApplicationContext(), page_list, posInPage));
            gv.setOnItemClickListener(TypeImageSelect.this.mElementClickListener);
            container.addView(v);
            return v;
        }

        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(128);
        setContentView(R.layout.model_select_type_select_main);
        Utilities.setStatusBarLeftText(this, (StatusbarView) findViewById(R.id.statusbar_of_type_select));
        Intent intent = getIntent();
        this.mModelId = intent.getLongExtra("model_id", -2);
        if (this.mModelId == -2) {
            Log.e(TAG, "Can't get the model id!");
            finish();
            overridePendingTransition(R.anim.type_close_enter, R.anim.type_close_exit);
            return;
        }
        this.PrimaryType = intent.getIntExtra("primary_type", 0);
        this.SubType = intent.getIntExtra("sub_type", -1);
        if (this.SubType == -1 || this.PrimaryType == this.SubType / 100) {
            String type_string;
            this.model_select_model_type = (TextView) findViewById(R.id.model_select_model_type);
            switch (this.PrimaryType) {
                case 4:
                    type_string = new StringBuilder(String.valueOf(getResources().getString(R.string.str_model_type))).append(" --> ").append(getResources().getString(R.string.str_multi_copter)).toString();
                    break;
                default:
                    type_string = getResources().getString(R.string.str_model_type);
                    break;
            }
            this.model_select_model_type.setText(type_string);
            loadPage(this.PrimaryType, this.SubType);
            Log.v(TAG, "++++++++++++ PrimaryType is" + this.PrimaryType);
            this.mViewPager = (ViewPager) findViewById(R.id.view_pager);
            this.mIndicator = (PageIndicator) findViewById(R.id.pageIndicator_type);
            this.mViewPager.setAdapter(new AircraftPagerAdapter(this.SubType));
            this.mViewPager.setOnPageChangeListener(this.mPageChangeListener);
            return;
        }
        Log.e(TAG, "primary type is not matched with sub type");
        finish();
        overridePendingTransition(R.anim.type_close_enter, R.anim.type_close_exit);
    }

    private void loadPage(int primary_type, int subtype) {
        int N = TypeImageResource.getTypeCount(primary_type);
        this.mGridViewList = new ArrayList(N);
        int base = primary_type * 100;
        for (int i = 0; i < N; i++) {
            this.mGridViewList.add(new SubTypeInfo((base + i) + 1, TypeImageResource.getTypeName(this, (base + i) + 1)));
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.type_close_enter, R.anim.type_close_exit);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode != 3 || event.isCanceled()) {
            return super.onKeyUp(keyCode, event);
        }
        Utilities.backToFlightScreen(this);
        return true;
    }
}
