package com.spreadtrum.android.eng;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings.System;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class uaagent extends Activity {
    private Button mBtnCancel;
    private RadioButton mBtnCustom;
    private RadioButton mBtnDefault;
    private RadioButton mBtnIphone;
    private RadioButton mBtnMtk;
    private Button mBtnOk;
    private RadioButton mBtnSamsung;
    private String mCustomUaStr = null;
    private String mDefaultUaStr = null;
    private EditText mEditor;
    private OnCheckedChangeListener mListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.default_ua:
                    uaagent.this.mEditor.setText(uaagent.this.mDefaultUaStr);
                    uaagent.this.mUaChoice = 0;
                    break;
                case R.id.mtk_ua:
                    uaagent.this.mEditor.setText("Mozilla/5.0 (Linux; U; Android 4.0; en-us; GIONEE GN105 Build/FRF91) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
                    uaagent.this.mUaChoice = 1;
                    break;
                case R.id.samsung_ua:
                    uaagent.this.mEditor.setText("Mozilla/5.0 (Linux; U; Android 4.0.3 zh-cn; GT-I9100 Build/IML74K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
                    uaagent.this.mUaChoice = 2;
                    break;
                case R.id.iphone_ua:
                    uaagent.this.mEditor.setText("Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16");
                    uaagent.this.mUaChoice = 3;
                    break;
                case R.id.custom_ua:
                    uaagent.this.mEditor.setText(uaagent.this.mCustomUaStr);
                    uaagent.this.mEditor.setSelection(uaagent.this.mEditor.getText().length());
                    uaagent.this.mUaChoice = 4;
                    break;
                default:
                    Log.e("eng/user-agent", "checkedId is <" + checkedId + ">");
                    break;
            }
            if (4 == uaagent.this.mUaChoice) {
                uaagent.this.mEditor.setBackgroundColor(-1);
                uaagent.this.mEditor.setTextColor(-16777216);
                uaagent.this.mEditor.setFocusableInTouchMode(true);
            } else {
                uaagent.this.mEditor.setBackgroundColor(-7829368);
                uaagent.this.mEditor.setTextColor(-16777216);
                uaagent.this.mEditor.setFocusableInTouchMode(false);
                uaagent.this.mEditor.clearFocus();
                ((InputMethodManager) uaagent.this.getSystemService("input_method")).hideSoftInputFromWindow(uaagent.this.mEditor.getWindowToken(), 0);
            }
            Log.d("eng/user-agent", "mUaChoice: " + uaagent.this.mUaChoice);
        }
    };
    private RadioGroup mRadioGroup;
    private int mUaChoice;
    private WebView mWebView = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.useragent);
        this.mRadioGroup = (RadioGroup) findViewById(R.id.ua_group);
        this.mRadioGroup.setOnCheckedChangeListener(this.mListener);
        this.mBtnDefault = (RadioButton) findViewById(R.id.default_ua);
        this.mBtnMtk = (RadioButton) findViewById(R.id.mtk_ua);
        this.mBtnSamsung = (RadioButton) findViewById(R.id.samsung_ua);
        this.mBtnIphone = (RadioButton) findViewById(R.id.iphone_ua);
        this.mBtnCustom = (RadioButton) findViewById(R.id.custom_ua);
        this.mEditor = (EditText) findViewById(R.id.ua_editor);
        this.mBtnOk = (Button) findViewById(R.id.btn_ok);
        this.mBtnCancel = (Button) findViewById(R.id.btn_cancel);
        this.mWebView = new WebView(this);
        initView();
    }

    private void initView() {
        this.mUaChoice = System.getInt(getContentResolver(), "user_agent_choice", 0);
        this.mCustomUaStr = System.getString(getContentResolver(), "custom_user_agent_string");
        this.mDefaultUaStr = this.mWebView.getSettings().getDefaultUserAgentString();
        Log.d("eng/user-agent", "-->initView-->mCustomUaStr is <" + this.mCustomUaStr + ">");
        Log.d("eng/user-agent", "-->initView-->mDefaultUaStr is <" + this.mDefaultUaStr + ">");
        Log.d("eng/user-agent", "-->initView-->mUaChoice is <" + this.mUaChoice + ">");
        if (4 == this.mUaChoice) {
            this.mEditor.setBackgroundColor(-1);
            this.mEditor.setTextColor(-16777216);
        } else {
            this.mEditor.setBackgroundColor(-7829368);
            this.mEditor.setTextColor(-16777216);
        }
        this.mEditor.setFocusableInTouchMode(false);
        this.mEditor.clearFocus();
        ((InputMethodManager) getSystemService("input_method")).hideSoftInputFromWindow(this.mEditor.getWindowToken(), 0);
        switch (this.mUaChoice) {
            case 1:
                this.mBtnMtk.setChecked(true);
                this.mEditor.setText("Mozilla/5.0 (Linux; U; Android 4.0; en-us; GIONEE GN105 Build/FRF91) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
                break;
            case 2:
                this.mBtnSamsung.setChecked(true);
                this.mEditor.setText("Mozilla/5.0 (Linux; U; Android 4.0.3 zh-cn; GT-I9100 Build/IML74K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
                break;
            case 3:
                this.mBtnIphone.setChecked(true);
                this.mEditor.setText("Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16");
                break;
            case 4:
                this.mBtnCustom.setChecked(true);
                this.mEditor.setText(this.mCustomUaStr);
                this.mEditor.setSelection(this.mEditor.getText().length());
                break;
            default:
                this.mBtnDefault.setChecked(true);
                this.mEditor.setText(this.mDefaultUaStr);
                this.mUaChoice = 0;
                break;
        }
        this.mEditor.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Log.d("eng/user-agent", "-->mEditor-->onClick()");
                if (4 == uaagent.this.mUaChoice) {
                    Log.d("eng/user-agent", "-->mEditor-->setFocusable = true");
                    uaagent.this.mEditor.requestFocus();
                    return;
                }
                uaagent.this.mEditor.clearFocus();
            }
        });
        this.mEditor.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d("eng/user-agent", "-->mEditor-->OnFocusChange()");
                if (4 != uaagent.this.mUaChoice) {
                }
            }
        });
        this.mBtnOk.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    Log.d("eng/user-agent", "-->mBtnOk.onClick-->mUaChoice is <" + uaagent.this.mUaChoice + ">");
                    System.putInt(uaagent.this.getContentResolver(), "user_agent_choice", uaagent.this.mUaChoice);
                    if (4 == uaagent.this.mUaChoice) {
                        uaagent.this.mCustomUaStr = uaagent.this.mEditor.getText().toString();
                        Log.d("eng/user-agent", "-->mBtnOk.onClick-->mCustomUaStr is <" + uaagent.this.mCustomUaStr + ">");
                        System.putString(uaagent.this.getContentResolver(), "custom_user_agent_string", uaagent.this.mCustomUaStr);
                    }
                } catch (NumberFormatException e) {
                    Log.e("eng/user-agent", "Save User-Agent choice failed!", e);
                }
                uaagent.this.finish();
            }
        });
        this.mBtnCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                uaagent.this.finish();
            }
        });
    }
}
