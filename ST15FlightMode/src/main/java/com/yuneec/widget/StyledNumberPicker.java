package com.yuneec.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.NumberKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;
import com.appunite.ffmpeg.ViewCompat;
import com.yuneec.IPCameraManager.IPCameraManager;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;
import java.util.Locale;

public class StyledNumberPicker extends LinearLayout {
    private static final int DEFAULT_LAYOUT_RESOURCE_ID = 2130903090;
    private static final long DEFAULT_LONG_PRESS_UPDATE_INTERVAL = 300;
    private static final char[] DIGIT_CHARACTERS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-'};
    private static final int SELECTOR_ADJUSTMENT_DURATION_MILLIS = 800;
    private static final int SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT = 8;
    private static final int SELECTOR_MIDDLE_ITEM_INDEX = 1;
    private static final int SELECTOR_WHEEL_ITEM_COUNT = 3;
    private static final int SIZE_UNSPECIFIED = -1;
    private static final int SNAP_SCROLL_DURATION = 300;
    private static final String TAG = "StyledNumberPicker";
    private static final float TOP_AND_BOTTOM_FADING_EDGE_STRENGTH = 0.9f;
    public static final Formatter TWO_DIGIT_FORMATTER = new Formatter() {
        final Object[] mArgs = new Object[1];
        final StringBuilder mBuilder = new StringBuilder();
        final java.util.Formatter mFmt = new java.util.Formatter(this.mBuilder, Locale.US);

        public String format(int value) {
            this.mArgs[0] = Integer.valueOf(value);
            this.mBuilder.delete(0, this.mBuilder.length());
            this.mFmt.format("%02d", this.mArgs);
            return this.mFmt.toString();
        }
    };
    private static final int UNSCALED_DEFAULT_SELECTION_DIVIDERS_DISTANCE = 48;
    private static final int UNSCALED_DEFAULT_SELECTION_DIVIDER_HEIGHT = 2;
    private AccessibilityNodeProviderImpl mAccessibilityNodeProvider;
    private final Scroller mAdjustScroller;
    private BeginSoftInputOnLongPressCommand mBeginSoftInputOnLongPressCommand;
    private int mBottomSelectionDividerBottom;
    public OnButtonClickedListener mButtonClickedListener;
    private ChangeCurrentByOneFromLongPressCommand mChangeCurrentByOneFromLongPressCommand;
    private final boolean mComputeMaxWidth;
    private int mCurrentScrollOffset;
    private final Paint mCurrentWheelPaint;
    private final ImageButton mDecrementButton;
    private boolean mDecrementVirtualButtonPressed;
    private int mDidaSoundId;
    private boolean mDisableKeyboard;
    private String[] mDisplayedValues;
    private final Scroller mFlingScroller;
    private Formatter mFormatter;
    private final boolean mHasSelectorWheel;
    private final ImageButton mIncrementButton;
    private boolean mIncrementVirtualButtonPressed;
    private boolean mIngonreMoveEvents;
    private int mInitialScrollOffset;
    private final EditText mInputText;
    private long mLastDownEventTime;
    private float mLastDownEventY;
    private float mLastDownOrMoveEventY;
    private long mLongPressUpdateInterval;
    private final int mMaxHeight;
    private int mMaxValue;
    private int mMaxWidth;
    private int mMaximumFlingVelocity;
    private final int mMinHeight;
    private int mMinValue;
    private final int mMinWidth;
    private int mMinimumFlingVelocity;
    private OnScrollListener mOnScrollListener;
    private OnValueChangeListener mOnValueChangeListener;
    private final PressedStateHelper mPressedStateHelper;
    private int mPreviousScrollerY;
    private int mScrollState;
    private final Drawable mSelectionDivider;
    private final int mSelectionDividerHeight;
    private final int mSelectionDividersDistance;
    private int mSelectorElementHeight;
    private final SparseArray<String> mSelectorIndexToStringCache;
    private final int[] mSelectorIndices;
    private int mSelectorTextGapHeight;
    private final Paint mSelectorWheelPaint;
    private SetSelectionCommand mSetSelectionCommand;
    private boolean mShowSoftInputOnTap;
    private final int mSolidColor;
    private SoundPool mSoundPool;
    private int mTextGapPadding;
    private final int mTextSize;
    private int mTopSelectionDividerTop;
    private int mTouchSlop;
    private int mValue;
    private VelocityTracker mVelocityTracker;
    private final Drawable mVirtualButtonPressedDrawable;
    private boolean mWrapSelectorWheel;
    private boolean mWrapSelectorWheelValid;

    @SuppressLint({"NewApi"})
    class AccessibilityNodeProviderImpl extends AccessibilityNodeProvider {
        private static final int UNDEFINED = Integer.MIN_VALUE;
        private static final int VIRTUAL_VIEW_ID_DECREMENT = 3;
        private static final int VIRTUAL_VIEW_ID_INCREMENT = 1;
        private static final int VIRTUAL_VIEW_ID_INPUT = 2;
        private int mAccessibilityFocusedView = Integer.MIN_VALUE;

        AccessibilityNodeProviderImpl() {
        }

        public boolean performAction(int virtualViewId, int action, Bundle arguments) {
            boolean increment = false;
            switch (virtualViewId) {
                case -1:
                    switch (action) {
                        case 64:
                            if (this.mAccessibilityFocusedView == virtualViewId) {
                                return false;
                            }
                            this.mAccessibilityFocusedView = virtualViewId;
                            return true;
                        case 128:
                            if (this.mAccessibilityFocusedView != virtualViewId) {
                                return false;
                            }
                            this.mAccessibilityFocusedView = Integer.MIN_VALUE;
                            return true;
                        case 4096:
                            if (!StyledNumberPicker.this.isEnabled()) {
                                return false;
                            }
                            if (!StyledNumberPicker.this.getWrapSelectorWheel() && StyledNumberPicker.this.getValue() >= StyledNumberPicker.this.getMaxValue()) {
                                return false;
                            }
                            StyledNumberPicker.this.changeValueByOne(true);
                            return true;
                        case 8192:
                            if (!StyledNumberPicker.this.isEnabled()) {
                                return false;
                            }
                            if (!StyledNumberPicker.this.getWrapSelectorWheel() && StyledNumberPicker.this.getValue() <= StyledNumberPicker.this.getMinValue()) {
                                return false;
                            }
                            StyledNumberPicker.this.changeValueByOne(false);
                            return true;
                        default:
                            break;
                    }
                case 1:
                    switch (action) {
                        case 16:
                            if (!StyledNumberPicker.this.isEnabled()) {
                                return false;
                            }
                            StyledNumberPicker.this.changeValueByOne(true);
                            sendAccessibilityEventForVirtualView(virtualViewId, 1);
                            return true;
                        case 64:
                            if (this.mAccessibilityFocusedView == virtualViewId) {
                                return false;
                            }
                            this.mAccessibilityFocusedView = virtualViewId;
                            sendAccessibilityEventForVirtualView(virtualViewId, 32768);
                            StyledNumberPicker.this.invalidate(0, StyledNumberPicker.this.mBottomSelectionDividerBottom, StyledNumberPicker.this.getRight(), StyledNumberPicker.this.getBottom());
                            return true;
                        case 128:
                            if (this.mAccessibilityFocusedView != virtualViewId) {
                                return false;
                            }
                            this.mAccessibilityFocusedView = Integer.MIN_VALUE;
                            sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEventCompat.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                            StyledNumberPicker.this.invalidate(0, StyledNumberPicker.this.mBottomSelectionDividerBottom, StyledNumberPicker.this.getRight(), StyledNumberPicker.this.getBottom());
                            return true;
                        default:
                            return false;
                    }
                case 2:
                    switch (action) {
                        case 1:
                            if (!StyledNumberPicker.this.isEnabled() || StyledNumberPicker.this.mInputText.isFocused()) {
                                return false;
                            }
                            return StyledNumberPicker.this.mInputText.requestFocus();
                        case 2:
                            if (!StyledNumberPicker.this.isEnabled() || !StyledNumberPicker.this.mInputText.isFocused()) {
                                return false;
                            }
                            StyledNumberPicker.this.mInputText.clearFocus();
                            return true;
                        case 16:
                            if (!StyledNumberPicker.this.isEnabled()) {
                                return false;
                            }
                            StyledNumberPicker.this.showSoftInput();
                            return true;
                        case 64:
                            if (this.mAccessibilityFocusedView == virtualViewId) {
                                return false;
                            }
                            this.mAccessibilityFocusedView = virtualViewId;
                            sendAccessibilityEventForVirtualView(virtualViewId, 32768);
                            StyledNumberPicker.this.mInputText.invalidate();
                            return true;
                        case 128:
                            if (this.mAccessibilityFocusedView != virtualViewId) {
                                return false;
                            }
                            this.mAccessibilityFocusedView = Integer.MIN_VALUE;
                            sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEventCompat.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                            StyledNumberPicker.this.mInputText.invalidate();
                            return true;
                        default:
                            return StyledNumberPicker.this.mInputText.performAccessibilityAction(action, arguments);
                    }
                case 3:
                    switch (action) {
                        case 16:
                            if (!StyledNumberPicker.this.isEnabled()) {
                                return false;
                            }
                            if (virtualViewId == 1) {
                                increment = true;
                            }
                            StyledNumberPicker.this.changeValueByOne(increment);
                            sendAccessibilityEventForVirtualView(virtualViewId, 1);
                            return true;
                        case 64:
                            if (this.mAccessibilityFocusedView == virtualViewId) {
                                return false;
                            }
                            this.mAccessibilityFocusedView = virtualViewId;
                            sendAccessibilityEventForVirtualView(virtualViewId, 32768);
                            StyledNumberPicker.this.invalidate(0, 0, StyledNumberPicker.this.getRight(), StyledNumberPicker.this.mTopSelectionDividerTop);
                            return true;
                        case 128:
                            if (this.mAccessibilityFocusedView != virtualViewId) {
                                return false;
                            }
                            this.mAccessibilityFocusedView = Integer.MIN_VALUE;
                            sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEventCompat.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                            StyledNumberPicker.this.invalidate(0, 0, StyledNumberPicker.this.getRight(), StyledNumberPicker.this.mTopSelectionDividerTop);
                            return true;
                        default:
                            return false;
                    }
            }
            return super.performAction(virtualViewId, action, arguments);
        }

        public void sendAccessibilityEventForVirtualView(int virtualViewId, int eventType) {
            switch (virtualViewId) {
                case 1:
                    if (hasVirtualIncrementButton()) {
                        sendAccessibilityEventForVirtualButton(virtualViewId, eventType, getVirtualIncrementButtonText());
                        return;
                    }
                    return;
                case 2:
                    sendAccessibilityEventForVirtualText(eventType);
                    return;
                case 3:
                    if (hasVirtualDecrementButton()) {
                        sendAccessibilityEventForVirtualButton(virtualViewId, eventType, getVirtualDecrementButtonText());
                        return;
                    }
                    return;
                default:
                    return;
            }
        }

        private void sendAccessibilityEventForVirtualText(int eventType) {
            AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
            StyledNumberPicker.this.mInputText.onInitializeAccessibilityEvent(event);
            StyledNumberPicker.this.mInputText.onPopulateAccessibilityEvent(event);
            event.setSource(StyledNumberPicker.this, 2);
            StyledNumberPicker.this.requestSendAccessibilityEvent(StyledNumberPicker.this, event);
        }

        private void sendAccessibilityEventForVirtualButton(int virtualViewId, int eventType, String text) {
            AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
            event.setClassName(Button.class.getName());
            event.setPackageName(StyledNumberPicker.this.getContext().getPackageName());
            event.getText().add(text);
            event.setEnabled(StyledNumberPicker.this.isEnabled());
            event.setSource(StyledNumberPicker.this, virtualViewId);
            StyledNumberPicker.this.requestSendAccessibilityEvent(StyledNumberPicker.this, event);
        }

        private boolean hasVirtualDecrementButton() {
            return StyledNumberPicker.this.getWrapSelectorWheel() || StyledNumberPicker.this.getValue() > StyledNumberPicker.this.getMinValue();
        }

        private boolean hasVirtualIncrementButton() {
            return StyledNumberPicker.this.getWrapSelectorWheel() || StyledNumberPicker.this.getValue() < StyledNumberPicker.this.getMaxValue();
        }

        private String getVirtualDecrementButtonText() {
            int value = StyledNumberPicker.this.mValue - 1;
            if (StyledNumberPicker.this.mWrapSelectorWheel) {
                value = StyledNumberPicker.this.getWrappedSelectorIndex(value);
            }
            if (value < StyledNumberPicker.this.mMinValue || StyledNumberPicker.this.mDisplayedValues != null) {
                return null;
            }
            return StyledNumberPicker.this.formatNumber(value);
        }

        private String getVirtualIncrementButtonText() {
            int value = StyledNumberPicker.this.mValue + 1;
            if (StyledNumberPicker.this.mWrapSelectorWheel) {
                value = StyledNumberPicker.this.getWrappedSelectorIndex(value);
            }
            if (value > StyledNumberPicker.this.mMaxValue || StyledNumberPicker.this.mDisplayedValues != null) {
                return null;
            }
            return StyledNumberPicker.this.formatNumber(value);
        }
    }

    class BeginSoftInputOnLongPressCommand implements Runnable {
        BeginSoftInputOnLongPressCommand() {
        }

        public void run() {
            StyledNumberPicker.this.showSoftInput();
            StyledNumberPicker.this.mIngonreMoveEvents = true;
        }
    }

    class ChangeCurrentByOneFromLongPressCommand implements Runnable {
        private boolean mIncrement;

        ChangeCurrentByOneFromLongPressCommand() {
        }

        private void setStep(boolean increment) {
            this.mIncrement = increment;
        }

        public void run() {
            StyledNumberPicker.this.changeValueByOne(this.mIncrement);
            StyledNumberPicker.this.postDelayed(this, StyledNumberPicker.this.mLongPressUpdateInterval);
        }
    }

    public static class CustomEditText extends EditText {
        public CustomEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public void onEditorAction(int actionCode) {
            super.onEditorAction(actionCode);
            if (actionCode == 6) {
                clearFocus();
            }
        }
    }

    public interface Formatter {
        String format(int i);
    }

    class InputTextFilter extends NumberKeyListener {
        InputTextFilter() {
        }

        public int getInputType() {
            return 1;
        }

        protected char[] getAcceptedChars() {
            return StyledNumberPicker.DIGIT_CHARACTERS;
        }

        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            int i = 0;
            CharSequence filtered;
            String result;
            if (StyledNumberPicker.this.mDisplayedValues == null) {
                filtered = super.filter(source, start, end, dest, dstart, dend);
                if (filtered == null) {
                    filtered = source.subSequence(start, end);
                }
                result = new StringBuilder(String.valueOf(String.valueOf(dest.subSequence(0, dstart)))).append(filtered).append(dest.subSequence(dend, dest.length())).toString();
                if ("".equals(result)) {
                    return result;
                }
                return StyledNumberPicker.this.getSelectedPos(result) > StyledNumberPicker.this.mMaxValue ? "" : filtered;
            } else {
                filtered = String.valueOf(source.subSequence(start, end));
                if (TextUtils.isEmpty(filtered)) {
                    return "";
                }
                result = new StringBuilder(String.valueOf(String.valueOf(dest.subSequence(0, dstart)))).append(filtered).append(dest.subSequence(dend, dest.length())).toString();
                String str = String.valueOf(result).toLowerCase();
                String[] access$1 = StyledNumberPicker.this.mDisplayedValues;
                int length = access$1.length;
                while (i < length) {
                    String val = access$1[i];
                    if (val.toLowerCase().startsWith(str)) {
                        StyledNumberPicker.this.postSetSelectionCommand(result.length(), val.length());
                        return val.subSequence(dstart, val.length());
                    }
                    i++;
                }
                return "";
            }
        }
    }

    public interface OnButtonClickedListener {
        void OnButtonClicked(StyledNumberPicker styledNumberPicker, int i);
    }

    public interface OnScrollListener {
        public static final int SCROLL_STATE_FLING = 2;
        public static final int SCROLL_STATE_IDLE = 0;
        public static final int SCROLL_STATE_TOUCH_SCROLL = 1;

        void onScrollStateChange(StyledNumberPicker styledNumberPicker, int i);
    }

    public interface OnValueChangeListener {
        void onValueChange(StyledNumberPicker styledNumberPicker, int i, int i2);
    }

    class PressedStateHelper implements Runnable {
        public static final int BUTTON_DECREMENT = 2;
        public static final int BUTTON_INCREMENT = 1;
        private final int MODE_PRESS = 1;
        private final int MODE_TAPPED = 2;
        private int mManagedButton;
        private int mMode;

        PressedStateHelper() {
        }

        public void cancel() {
            this.mMode = 0;
            this.mManagedButton = 0;
            StyledNumberPicker.this.removeCallbacks(this);
            if (StyledNumberPicker.this.mIncrementVirtualButtonPressed) {
                StyledNumberPicker.this.mIncrementVirtualButtonPressed = false;
                StyledNumberPicker.this.invalidate(0, StyledNumberPicker.this.mBottomSelectionDividerBottom, StyledNumberPicker.this.getRight(), StyledNumberPicker.this.getBottom());
            }
            StyledNumberPicker.this.mDecrementVirtualButtonPressed = false;
            if (StyledNumberPicker.this.mDecrementVirtualButtonPressed) {
                StyledNumberPicker.this.invalidate(0, 0, StyledNumberPicker.this.getRight(), StyledNumberPicker.this.mTopSelectionDividerTop);
            }
        }

        public void buttonPressDelayed(int button) {
            cancel();
            this.mMode = 1;
            this.mManagedButton = button;
            StyledNumberPicker.this.postDelayed(this, (long) ViewConfiguration.getTapTimeout());
        }

        public void buttonTapped(int button) {
            cancel();
            this.mMode = 2;
            this.mManagedButton = button;
            StyledNumberPicker.this.post(this);
        }

        public void run() {
            switch (this.mMode) {
                case 1:
                    switch (this.mManagedButton) {
                        case 1:
                            StyledNumberPicker.this.mIncrementVirtualButtonPressed = true;
                            StyledNumberPicker.this.invalidate(0, StyledNumberPicker.this.mBottomSelectionDividerBottom, StyledNumberPicker.this.getRight(), StyledNumberPicker.this.getBottom());
                            return;
                        case 2:
                            StyledNumberPicker.this.mDecrementVirtualButtonPressed = true;
                            StyledNumberPicker.this.invalidate(0, 0, StyledNumberPicker.this.getRight(), StyledNumberPicker.this.mTopSelectionDividerTop);
                            return;
                        default:
                            return;
                    }
                case 2:
                    StyledNumberPicker styledNumberPicker;
                    switch (this.mManagedButton) {
                        case 1:
                            if (!StyledNumberPicker.this.mIncrementVirtualButtonPressed) {
                                StyledNumberPicker.this.postDelayed(this, (long) ViewConfiguration.getPressedStateDuration());
                            }
                            styledNumberPicker = StyledNumberPicker.this;
                            styledNumberPicker.mIncrementVirtualButtonPressed = styledNumberPicker.mIncrementVirtualButtonPressed ^ 1;
                            StyledNumberPicker.this.invalidate(0, StyledNumberPicker.this.mBottomSelectionDividerBottom, StyledNumberPicker.this.getRight(), StyledNumberPicker.this.getBottom());
                            return;
                        case 2:
                            if (!StyledNumberPicker.this.mDecrementVirtualButtonPressed) {
                                StyledNumberPicker.this.postDelayed(this, (long) ViewConfiguration.getPressedStateDuration());
                            }
                            styledNumberPicker = StyledNumberPicker.this;
                            styledNumberPicker.mDecrementVirtualButtonPressed = styledNumberPicker.mDecrementVirtualButtonPressed ^ 1;
                            StyledNumberPicker.this.invalidate(0, 0, StyledNumberPicker.this.getRight(), StyledNumberPicker.this.mTopSelectionDividerTop);
                            return;
                        default:
                            return;
                    }
                default:
                    return;
            }
        }
    }

    class SetSelectionCommand implements Runnable {
        private int mSelectionEnd;
        private int mSelectionStart;

        SetSelectionCommand() {
        }

        public void run() {
            StyledNumberPicker.this.mInputText.setSelection(this.mSelectionStart, this.mSelectionEnd);
        }
    }

    public StyledNumberPicker(Context context) {
        this(context, null);
    }

    public StyledNumberPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StyledNumberPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mLongPressUpdateInterval = DEFAULT_LONG_PRESS_UPDATE_INTERVAL;
        this.mSelectorIndexToStringCache = new SparseArray();
        this.mSelectorIndices = new int[3];
        this.mInitialScrollOffset = Utilities.FLAG_HOMEKEY_DISPATCHED;
        this.mScrollState = 0;
        TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.NumberPicker, defStyle, 0);
        int layoutResId = attributesArray.getResourceId(8, R.layout.number_picker);
        this.mHasSelectorWheel = layoutResId != R.layout.number_picker;
        this.mSolidColor = attributesArray.getColor(0, 0);
        this.mSelectionDivider = attributesArray.getDrawable(1);
        this.mSelectionDividerHeight = attributesArray.getDimensionPixelSize(2, (int) TypedValue.applyDimension(1, Utilities.K_MAX, getResources().getDisplayMetrics()));
        this.mSelectionDividersDistance = attributesArray.getDimensionPixelSize(3, (int) TypedValue.applyDimension(1, 48.0f, getResources().getDisplayMetrics()));
        this.mMinHeight = attributesArray.getDimensionPixelSize(4, -1);
        this.mMaxHeight = attributesArray.getDimensionPixelSize(5, -1);
        if (this.mMinHeight == -1 || this.mMaxHeight == -1 || this.mMinHeight <= this.mMaxHeight) {
            this.mMinWidth = attributesArray.getDimensionPixelSize(6, -1);
            this.mMaxWidth = attributesArray.getDimensionPixelSize(7, -1);
            if (this.mMinWidth == -1 || this.mMaxWidth == -1 || this.mMinWidth <= this.mMaxWidth) {
                this.mComputeMaxWidth = this.mMaxWidth == -1;
                this.mVirtualButtonPressedDrawable = attributesArray.getDrawable(9);
                this.mDisableKeyboard = attributesArray.getBoolean(10, false);
                this.mWrapSelectorWheelValid = attributesArray.getBoolean(11, true);
                int centerColor = attributesArray.getColor(12, ViewCompat.MEASURED_STATE_MASK);
                int wheelColor = attributesArray.getColor(13, -13619152);
                this.mTextGapPadding = (int) attributesArray.getDimension(14, 0.0f);
                attributesArray.recycle();
                this.mPressedStateHelper = new PressedStateHelper();
                setWillNotDraw(!this.mHasSelectorWheel);
                ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(layoutResId, this, true);
                OnClickListener onClickListener = new OnClickListener() {
                    public void onClick(View v) {
                        StyledNumberPicker.this.hideSoftInput();
                        StyledNumberPicker.this.mInputText.clearFocus();
                        if (v.getId() == R.id.increment) {
                            StyledNumberPicker.this.changeValueByOne(true);
                        } else {
                            StyledNumberPicker.this.changeValueByOne(false);
                        }
                    }
                };
                OnLongClickListener onLongClickListener = new OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        StyledNumberPicker.this.hideSoftInput();
                        StyledNumberPicker.this.mInputText.clearFocus();
                        if (v.getId() == R.id.increment) {
                            StyledNumberPicker.this.postChangeCurrentByOneFromLongPress(true, 0);
                        } else {
                            StyledNumberPicker.this.postChangeCurrentByOneFromLongPress(false, 0);
                        }
                        return true;
                    }
                };
                if (this.mHasSelectorWheel) {
                    this.mIncrementButton = null;
                } else {
                    this.mIncrementButton = (ImageButton) findViewById(R.id.increment);
                    this.mIncrementButton.setOnClickListener(onClickListener);
                }
                if (this.mHasSelectorWheel) {
                    this.mDecrementButton = null;
                } else {
                    this.mDecrementButton = (ImageButton) findViewById(R.id.decrement);
                    this.mDecrementButton.setOnClickListener(onClickListener);
                }
                if (isInEditMode()) {
                    this.mInputText = new EditText(context);
                } else {
                    this.mInputText = (EditText) findViewById(R.id.numberpicker_input);
                }
                if (this.mDisableKeyboard) {
                    this.mInputText.setFocusable(false);
                    this.mInputText.setFocusableInTouchMode(false);
                }
                this.mInputText.setOnFocusChangeListener(new OnFocusChangeListener() {
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            StyledNumberPicker.this.mInputText.selectAll();
                            return;
                        }
                        StyledNumberPicker.this.mInputText.setSelection(0, 0);
                        StyledNumberPicker.this.validateInputTextView(v);
                    }
                });
                this.mInputText.setRawInputType(2);
                this.mInputText.setImeOptions(6);
                this.mInputText.setTextColor(centerColor);
                ViewConfiguration configuration = ViewConfiguration.get(context);
                this.mTouchSlop = configuration.getScaledTouchSlop();
                this.mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
                this.mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity() / 8;
                this.mTextSize = (int) this.mInputText.getTextSize();
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setTextAlign(Align.CENTER);
                paint.setTextSize((float) this.mTextSize);
                paint.setTypeface(this.mInputText.getTypeface());
                paint.setColor(wheelColor);
                this.mSelectorWheelPaint = paint;
                this.mCurrentWheelPaint = new Paint(this.mSelectorWheelPaint);
                this.mCurrentWheelPaint.setColor(centerColor);
                this.mFlingScroller = new Scroller(getContext(), null, true);
                this.mAdjustScroller = new Scroller(getContext(), new DecelerateInterpolator(2.5f));
                updateInputTextView();
                return;
            }
            throw new IllegalArgumentException("minWidth > maxWidth");
        }
        throw new IllegalArgumentException("minHeight > maxHeight");
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (this.mHasSelectorWheel) {
            int msrdWdth = getMeasuredWidth();
            int msrdHght = getMeasuredHeight();
            int inptTxtMsrdWdth = this.mInputText.getMeasuredWidth();
            int inptTxtMsrdHght = this.mInputText.getMeasuredHeight();
            int inptTxtLeft = (msrdWdth - inptTxtMsrdWdth) / 2;
            int inptTxtTop = (msrdHght - inptTxtMsrdHght) / 2;
            this.mInputText.layout(inptTxtLeft, inptTxtTop, inptTxtLeft + inptTxtMsrdWdth, inptTxtTop + inptTxtMsrdHght);
            if (changed) {
                initializeSelectorWheel();
                initializeFadingEdges();
                this.mTopSelectionDividerTop = ((getHeight() - this.mSelectionDividersDistance) / 2) - this.mSelectionDividerHeight;
                this.mBottomSelectionDividerBottom = (this.mTopSelectionDividerTop + (this.mSelectionDividerHeight * 2)) + this.mSelectionDividersDistance;
                return;
            }
            return;
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mHasSelectorWheel) {
            super.onMeasure(makeMeasureSpec(widthMeasureSpec, this.mMaxWidth), makeMeasureSpec(heightMeasureSpec, this.mMaxHeight));
            setMeasuredDimension(resolveSizeAndStateRespectingMinSize(this.mMinWidth, getMeasuredWidth(), widthMeasureSpec), resolveSizeAndStateRespectingMinSize(this.mMinHeight, getMeasuredHeight(), heightMeasureSpec));
            return;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private boolean moveToFinalScrollerPosition(Scroller scroller) {
        scroller.forceFinished(true);
        int amountToScroll = scroller.getFinalY() - scroller.getCurrY();
        int overshootAdjustment = this.mInitialScrollOffset - ((this.mCurrentScrollOffset + amountToScroll) % this.mSelectorElementHeight);
        if (overshootAdjustment == 0) {
            return false;
        }
        if (Math.abs(overshootAdjustment) > this.mSelectorElementHeight / 2) {
            if (overshootAdjustment > 0) {
                overshootAdjustment -= this.mSelectorElementHeight;
            } else {
                overshootAdjustment += this.mSelectorElementHeight;
            }
        }
        scrollBy(0, amountToScroll + overshootAdjustment);
        return true;
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!this.mHasSelectorWheel || !isEnabled()) {
            return false;
        }
        switch (event.getActionMasked()) {
            case 0:
                removeAllCallbacks();
                this.mInputText.setVisibility(4);
                float y = event.getY();
                this.mLastDownEventY = y;
                this.mLastDownOrMoveEventY = y;
                this.mLastDownEventTime = event.getEventTime();
                this.mIngonreMoveEvents = false;
                this.mShowSoftInputOnTap = false;
                if (this.mLastDownEventY < ((float) this.mTopSelectionDividerTop)) {
                    if (this.mScrollState == 0) {
                        this.mPressedStateHelper.buttonPressDelayed(2);
                    }
                } else if (this.mLastDownEventY > ((float) this.mBottomSelectionDividerBottom) && this.mScrollState == 0) {
                    this.mPressedStateHelper.buttonPressDelayed(1);
                }
                getParent().requestDisallowInterceptTouchEvent(true);
                if (!this.mFlingScroller.isFinished()) {
                    this.mFlingScroller.forceFinished(true);
                    this.mAdjustScroller.forceFinished(true);
                    onScrollStateChange(0);
                    return true;
                } else if (!this.mAdjustScroller.isFinished()) {
                    this.mFlingScroller.forceFinished(true);
                    this.mAdjustScroller.forceFinished(true);
                    return true;
                } else if (this.mLastDownEventY < ((float) this.mTopSelectionDividerTop)) {
                    hideSoftInput();
                    postChangeCurrentByOneFromLongPress(false, (long) ViewConfiguration.getLongPressTimeout());
                    return true;
                } else if (this.mLastDownEventY > ((float) this.mBottomSelectionDividerBottom)) {
                    hideSoftInput();
                    postChangeCurrentByOneFromLongPress(true, (long) ViewConfiguration.getLongPressTimeout());
                    return true;
                } else {
                    this.mShowSoftInputOnTap = true;
                    postBeginSoftInputOnLongPressCommand();
                    return true;
                }
            default:
                return false;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || !this.mHasSelectorWheel) {
            return false;
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(event);
        switch (event.getActionMasked()) {
            case 1:
                removeBeginSoftInputCommand();
                removeChangeCurrentByOneFromLongPress();
                this.mPressedStateHelper.cancel();
                VelocityTracker velocityTracker = this.mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumFlingVelocity);
                int initialVelocity = (int) velocityTracker.getYVelocity();
                if (Math.abs(initialVelocity) > this.mMinimumFlingVelocity) {
                    fling(initialVelocity);
                    onScrollStateChange(2);
                } else {
                    int eventY = (int) event.getY();
                    long deltaTime = event.getEventTime() - this.mLastDownEventTime;
                    if (((int) Math.abs(((float) eventY) - this.mLastDownEventY)) > this.mTouchSlop || deltaTime >= ((long) ViewConfiguration.getTapTimeout())) {
                        ensureScrollWheelAdjusted();
                    } else if (this.mShowSoftInputOnTap) {
                        this.mShowSoftInputOnTap = false;
                        showSoftInput();
                    } else {
                        int selectorIndexOffset = (eventY / this.mSelectorElementHeight) - 1;
                        if (selectorIndexOffset > 0) {
                            changeValueByOne(true);
                            this.mPressedStateHelper.buttonTapped(1);
                        } else if (selectorIndexOffset < 0) {
                            changeValueByOne(false);
                            this.mPressedStateHelper.buttonTapped(2);
                        }
                        if (this.mButtonClickedListener != null) {
                            this.mButtonClickedListener.OnButtonClicked(this, this.mValue);
                        }
                    }
                    onScrollStateChange(0);
                }
                this.mVelocityTracker.recycle();
                this.mVelocityTracker = null;
                break;
            case 2:
                if (!this.mIngonreMoveEvents) {
                    float currentMoveY = event.getY();
                    if (this.mScrollState == 1) {
                        scrollBy(0, (int) (currentMoveY - this.mLastDownOrMoveEventY));
                        invalidate();
                    } else if (((int) Math.abs(currentMoveY - this.mLastDownEventY)) > this.mTouchSlop) {
                        removeAllCallbacks();
                        onScrollStateChange(1);
                    }
                    this.mLastDownOrMoveEventY = currentMoveY;
                    break;
                }
                break;
        }
        return true;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case 1:
            case 3:
                removeAllCallbacks();
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case 23:
            case IPCameraManager.REQUEST_SET_ISO /*66*/:
                removeAllCallbacks();
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    public boolean dispatchTrackballEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case 1:
            case 3:
                removeAllCallbacks();
                break;
        }
        return super.dispatchTrackballEvent(event);
    }

    protected boolean dispatchHoverEvent(MotionEvent event) {
        if (this.mHasSelectorWheel) {
            return false;
        }
        return super.dispatchHoverEvent(event);
    }

    public void computeScroll() {
        Scroller scroller = this.mFlingScroller;
        if (scroller.isFinished()) {
            scroller = this.mAdjustScroller;
            if (scroller.isFinished()) {
                return;
            }
        }
        scroller.computeScrollOffset();
        int currentScrollerY = scroller.getCurrY();
        if (this.mPreviousScrollerY == 0) {
            this.mPreviousScrollerY = scroller.getStartY();
        }
        scrollBy(0, currentScrollerY - this.mPreviousScrollerY);
        this.mPreviousScrollerY = currentScrollerY;
        if (scroller.isFinished()) {
            onScrollerFinished(scroller);
        } else {
            invalidate();
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!this.mHasSelectorWheel) {
            this.mIncrementButton.setEnabled(enabled);
        }
        if (!this.mHasSelectorWheel) {
            this.mDecrementButton.setEnabled(enabled);
        }
        this.mInputText.setEnabled(enabled);
    }

    public void scrollBy(int x, int y) {
        int[] selectorIndices = this.mSelectorIndices;
        if (!this.mWrapSelectorWheel && y > 0 && selectorIndices[1] <= this.mMinValue) {
            this.mCurrentScrollOffset = this.mInitialScrollOffset;
        } else if (this.mWrapSelectorWheel || y >= 0 || selectorIndices[1] < this.mMaxValue) {
            this.mCurrentScrollOffset += y;
            while (this.mCurrentScrollOffset - this.mInitialScrollOffset > this.mSelectorTextGapHeight) {
                this.mCurrentScrollOffset -= this.mSelectorElementHeight;
                decrementSelectorIndices(selectorIndices);
                setValueInternal(selectorIndices[1], true);
                if (!this.mWrapSelectorWheel && selectorIndices[1] <= this.mMinValue) {
                    this.mCurrentScrollOffset = this.mInitialScrollOffset;
                }
            }
            while (this.mCurrentScrollOffset - this.mInitialScrollOffset < (-this.mSelectorTextGapHeight)) {
                this.mCurrentScrollOffset += this.mSelectorElementHeight;
                incrementSelectorIndices(selectorIndices);
                setValueInternal(selectorIndices[1], true);
                if (!this.mWrapSelectorWheel && selectorIndices[1] >= this.mMaxValue) {
                    this.mCurrentScrollOffset = this.mInitialScrollOffset;
                }
            }
        } else {
            this.mCurrentScrollOffset = this.mInitialScrollOffset;
        }
    }

    public int getSolidColor() {
        return this.mSolidColor;
    }

    public void setOnValueChangedListener(OnValueChangeListener onValueChangedListener) {
        this.mOnValueChangeListener = onValueChangedListener;
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.mOnScrollListener = onScrollListener;
    }

    public void setFormatter(Formatter formatter) {
        if (formatter != this.mFormatter) {
            this.mFormatter = formatter;
            initializeSelectorWheelIndices();
            updateInputTextView();
        }
    }

    public void setValue(int value) {
        setValueInternal(value, false);
    }

    private void showSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService("input_method");
        if (inputMethodManager != null) {
            if (this.mHasSelectorWheel) {
                this.mInputText.setVisibility(0);
            }
            this.mInputText.requestFocus();
            inputMethodManager.showSoftInput(this.mInputText, 0);
        }
    }

    private void hideSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService("input_method");
        if (inputMethodManager != null && inputMethodManager.isActive(this.mInputText)) {
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            if (this.mHasSelectorWheel) {
                this.mInputText.setVisibility(4);
            }
        }
    }

    private void tryComputeMaxWidth() {
        if (this.mComputeMaxWidth) {
            int maxTextWidth = 0;
            int i;
            if (this.mDisplayedValues == null) {
                float maxDigitWidth = 0.0f;
                for (i = 0; i <= 9; i++) {
                    float digitWidth = this.mSelectorWheelPaint.measureText(String.valueOf(i));
                    if (digitWidth > maxDigitWidth) {
                        maxDigitWidth = digitWidth;
                    }
                }
                int numberOfDigits = 0;
                for (int current = this.mMaxValue; current > 0; current /= 10) {
                    numberOfDigits++;
                }
                maxTextWidth = (int) (((float) numberOfDigits) * maxDigitWidth);
            } else {
                for (String measureText : this.mDisplayedValues) {
                    float textWidth = this.mSelectorWheelPaint.measureText(measureText);
                    if (textWidth > ((float) maxTextWidth)) {
                        maxTextWidth = (int) textWidth;
                    }
                }
            }
            maxTextWidth += this.mInputText.getPaddingLeft() + this.mInputText.getPaddingRight();
            if (this.mMaxWidth != maxTextWidth) {
                if (maxTextWidth > this.mMinWidth) {
                    this.mMaxWidth = maxTextWidth;
                } else {
                    this.mMaxWidth = this.mMinWidth;
                }
                invalidate();
            }
        }
    }

    public boolean getWrapSelectorWheel() {
        return this.mWrapSelectorWheel;
    }

    public void setWrapSelectorWheel(boolean wrapSelectorWheel) {
        boolean wrappingAllowed = this.mMaxValue - this.mMinValue >= this.mSelectorIndices.length;
        if ((!wrapSelectorWheel || wrappingAllowed) && wrapSelectorWheel != this.mWrapSelectorWheel) {
            this.mWrapSelectorWheel = wrapSelectorWheel;
        }
    }

    public void setOnLongPressUpdateInterval(long intervalMillis) {
        this.mLongPressUpdateInterval = intervalMillis;
    }

    public int getValue() {
        return this.mValue;
    }

    public int getMinValue() {
        return this.mMinValue;
    }

    public void setMinValue(int minValue) {
        if (this.mMinValue != minValue) {
            this.mMinValue = minValue;
            if (this.mMinValue > this.mValue) {
                this.mValue = this.mMinValue;
            }
            boolean wrapSelectorWheel = this.mMaxValue - this.mMinValue > this.mSelectorIndices.length && this.mWrapSelectorWheelValid;
            setWrapSelectorWheel(wrapSelectorWheel);
            initializeSelectorWheelIndices();
            updateInputTextView();
            tryComputeMaxWidth();
            invalidate();
        }
    }

    public int getMaxValue() {
        return this.mMaxValue;
    }

    public void setMaxValue(int maxValue) {
        if (this.mMaxValue != maxValue) {
            this.mMaxValue = maxValue;
            if (this.mMaxValue < this.mValue) {
                this.mValue = this.mMaxValue;
            }
            boolean wrapSelectorWheel = this.mMaxValue - this.mMinValue > this.mSelectorIndices.length && this.mWrapSelectorWheelValid;
            setWrapSelectorWheel(wrapSelectorWheel);
            initializeSelectorWheelIndices();
            updateInputTextView();
            tryComputeMaxWidth();
            invalidate();
        }
    }

    public String[] getDisplayedValues() {
        return this.mDisplayedValues;
    }

    public void setDisplayedValues(String[] displayedValues) {
        if (this.mDisplayedValues != displayedValues) {
            if (this.mMinValue < 0) {
                Log.e(TAG, "mMinValue can not have displayedValues");
                return;
            }
            this.mDisplayedValues = displayedValues;
            if (this.mDisplayedValues != null) {
                this.mInputText.setRawInputType(524289);
            } else {
                this.mInputText.setRawInputType(2);
            }
            updateInputTextView();
            initializeSelectorWheelIndices();
            tryComputeMaxWidth();
        }
    }

    protected float getTopFadingEdgeStrength() {
        return TOP_AND_BOTTOM_FADING_EDGE_STRENGTH;
    }

    protected float getBottomFadingEdgeStrength() {
        return TOP_AND_BOTTOM_FADING_EDGE_STRENGTH;
    }

    protected void onDetachedFromWindow() {
        removeAllCallbacks();
    }

    protected void onDraw(Canvas canvas) {
        if (this.mHasSelectorWheel) {
            float x = (float) ((getRight() - getLeft()) / 2);
            float y = (float) this.mCurrentScrollOffset;
            if (this.mVirtualButtonPressedDrawable != null && this.mScrollState == 0) {
                if (this.mDecrementVirtualButtonPressed) {
                    this.mVirtualButtonPressedDrawable.setBounds(0, 0, getRight(), this.mTopSelectionDividerTop);
                    this.mVirtualButtonPressedDrawable.draw(canvas);
                }
                if (this.mIncrementVirtualButtonPressed) {
                    this.mVirtualButtonPressedDrawable.setBounds(0, this.mBottomSelectionDividerBottom, getRight(), getBottom());
                    this.mVirtualButtonPressedDrawable.draw(canvas);
                }
            }
            int[] selectorIndices = this.mSelectorIndices;
            int i = 0;
            while (i < selectorIndices.length) {
                String scrollSelectorValue = (String) this.mSelectorIndexToStringCache.get(selectorIndices[i]);
                if (!(i == 1 && this.mInputText.getVisibility() == 0) && i == 1) {
                    canvas.drawText(scrollSelectorValue, x, y, this.mCurrentWheelPaint);
                }
                y += (float) this.mSelectorElementHeight;
                i++;
            }
            if (this.mSelectionDivider != null) {
                int topOfTopDivider = this.mTopSelectionDividerTop;
                this.mSelectionDivider.setBounds(0, topOfTopDivider, getRight(), topOfTopDivider + this.mSelectionDividerHeight);
                this.mSelectionDivider.draw(canvas);
                int bottomOfBottomDivider = this.mBottomSelectionDividerBottom;
                this.mSelectionDivider.setBounds(0, bottomOfBottomDivider - this.mSelectionDividerHeight, getRight(), bottomOfBottomDivider);
                this.mSelectionDivider.draw(canvas);
                return;
            }
            return;
        }
        super.onDraw(canvas);
    }

    public void release() {
        if (this.mSoundPool != null) {
            this.mSoundPool.release();
            this.mSoundPool = null;
        }
    }

    private int makeMeasureSpec(int measureSpec, int maxSize) {
        if (maxSize == -1) {
            return measureSpec;
        }
        int size = MeasureSpec.getSize(measureSpec);
        int mode = MeasureSpec.getMode(measureSpec);
        switch (mode) {
            case Utilities.FLAG_HOMEKEY_DISPATCHED /*-2147483648*/:
                return MeasureSpec.makeMeasureSpec(Math.min(size, maxSize), 1073741824);
            case 0:
                return MeasureSpec.makeMeasureSpec(maxSize, 1073741824);
            case 1073741824:
                return measureSpec;
            default:
                throw new IllegalArgumentException("Unknown measure mode: " + mode);
        }
    }

    private int resolveSizeAndStateRespectingMinSize(int minSize, int measuredSize, int measureSpec) {
        if (minSize != -1) {
            return resolveSizeAndState(Math.max(minSize, measuredSize), measureSpec, 0);
        }
        return measuredSize;
    }

    private void initializeSelectorWheelIndices() {
        this.mSelectorIndexToStringCache.clear();
        int[] selectorIndices = this.mSelectorIndices;
        int current = getValue();
        for (int i = 0; i < this.mSelectorIndices.length; i++) {
            int selectorIndex = current + (i - 1);
            if (this.mWrapSelectorWheel) {
                selectorIndex = getWrappedSelectorIndex(selectorIndex);
            }
            selectorIndices[i] = selectorIndex;
            ensureCachedScrollSelectorValue(selectorIndices[i]);
        }
    }

    private void setValueInternal(int current, boolean notifyChange) {
        if (this.mValue != current) {
            if (this.mWrapSelectorWheel) {
                current = getWrappedSelectorIndex(current);
            } else {
                current = Math.min(Math.max(current, this.mMinValue), this.mMaxValue);
            }
            int previous = this.mValue;
            this.mValue = current;
            updateInputTextView();
            if (notifyChange) {
                notifyChange(previous, current);
            }
            initializeSelectorWheelIndices();
            invalidate();
        }
    }

    private void changeValueByOne(boolean increment) {
        if (this.mHasSelectorWheel) {
            this.mInputText.setVisibility(4);
            if (!moveToFinalScrollerPosition(this.mFlingScroller)) {
                moveToFinalScrollerPosition(this.mAdjustScroller);
            }
            this.mPreviousScrollerY = 0;
            if (increment) {
                this.mFlingScroller.startScroll(0, 0, 0, -this.mSelectorElementHeight, 300);
                this.mValue = Math.min(this.mValue + 1, this.mMaxValue);
            } else {
                this.mFlingScroller.startScroll(0, 0, 0, this.mSelectorElementHeight, 300);
                this.mValue = Math.max(this.mValue - 1, this.mMinValue);
            }
            invalidate();
        } else if (increment) {
            setValueInternal(this.mValue + 1, true);
        } else {
            setValueInternal(this.mValue - 1, true);
        }
    }

    private void initializeSelectorWheel() {
        initializeSelectorWheelIndices();
        int[] selectorIndices = this.mSelectorIndices;
        this.mSelectorTextGapHeight = (int) ((((float) ((getBottom() - getTop()) - (selectorIndices.length * this.mTextSize))) / ((float) selectorIndices.length)) + 0.5f);
        this.mSelectorElementHeight = (this.mTextSize + this.mSelectorTextGapHeight) - this.mTextGapPadding;
        this.mInitialScrollOffset = (this.mInputText.getBaseline() + this.mInputText.getTop()) - (this.mSelectorElementHeight * 1);
        this.mCurrentScrollOffset = this.mInitialScrollOffset;
        updateInputTextView();
    }

    private void initializeFadingEdges() {
        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength(((getBottom() - getTop()) - this.mTextSize) / 2);
    }

    private void onScrollerFinished(Scroller scroller) {
        if (scroller == this.mFlingScroller) {
            if (!ensureScrollWheelAdjusted()) {
                updateInputTextView();
            }
            onScrollStateChange(0);
        } else if (this.mScrollState != 1) {
            updateInputTextView();
        }
    }

    private void onScrollStateChange(int scrollState) {
        if (this.mScrollState != scrollState) {
            this.mScrollState = scrollState;
            if (this.mOnScrollListener != null) {
                this.mOnScrollListener.onScrollStateChange(this, scrollState);
            }
        }
    }

    private void fling(int velocityY) {
        this.mPreviousScrollerY = 0;
        if (velocityY > 0) {
            this.mFlingScroller.fling(0, 0, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE);
        } else {
            this.mFlingScroller.fling(0, Integer.MAX_VALUE, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE);
        }
        invalidate();
    }

    private int getWrappedSelectorIndex(int selectorIndex) {
        if (selectorIndex > this.mMaxValue) {
            return (this.mMinValue + ((selectorIndex - this.mMaxValue) % (this.mMaxValue - this.mMinValue))) - 1;
        }
        if (selectorIndex < this.mMinValue) {
            return (this.mMaxValue - ((this.mMinValue - selectorIndex) % (this.mMaxValue - this.mMinValue))) + 1;
        }
        return selectorIndex;
    }

    private void incrementSelectorIndices(int[] selectorIndices) {
        for (int i = 0; i < selectorIndices.length - 1; i++) {
            selectorIndices[i] = selectorIndices[i + 1];
        }
        int nextScrollSelectorIndex = selectorIndices[selectorIndices.length - 2] + 1;
        if (this.mWrapSelectorWheel && nextScrollSelectorIndex > this.mMaxValue) {
            nextScrollSelectorIndex = this.mMinValue;
        }
        selectorIndices[selectorIndices.length - 1] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }

    private void decrementSelectorIndices(int[] selectorIndices) {
        for (int i = selectorIndices.length - 1; i > 0; i--) {
            selectorIndices[i] = selectorIndices[i - 1];
        }
        int nextScrollSelectorIndex = selectorIndices[1] - 1;
        if (this.mWrapSelectorWheel && nextScrollSelectorIndex < this.mMinValue) {
            nextScrollSelectorIndex = this.mMaxValue;
        }
        selectorIndices[0] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }

    private void ensureCachedScrollSelectorValue(int selectorIndex) {
        SparseArray<String> cache = this.mSelectorIndexToStringCache;
        if (((String) cache.get(selectorIndex)) == null) {
            String scrollSelectorValue;
            if (selectorIndex < this.mMinValue || selectorIndex > this.mMaxValue) {
                scrollSelectorValue = "";
            } else if (this.mDisplayedValues != null) {
                scrollSelectorValue = this.mDisplayedValues[selectorIndex - this.mMinValue];
            } else {
                scrollSelectorValue = formatNumber(selectorIndex);
            }
            cache.put(selectorIndex, scrollSelectorValue);
        }
    }

    private String formatNumber(int value) {
        return this.mFormatter != null ? this.mFormatter.format(value) : String.valueOf(value);
    }

    private void validateInputTextView(View v) {
        String str = String.valueOf(((TextView) v).getText());
        if (TextUtils.isEmpty(str)) {
            updateInputTextView();
        } else {
            setValueInternal(getSelectedPos(str.toString()), true);
        }
    }

    private boolean updateInputTextView() {
        String text;
        if (this.mDisplayedValues == null) {
            text = formatNumber(this.mValue);
        } else {
            text = this.mDisplayedValues[this.mValue - this.mMinValue];
        }
        if (TextUtils.isEmpty(text) || text.equals(this.mInputText.getText().toString())) {
            return false;
        }
        this.mInputText.setText(text);
        return true;
    }

    private void notifyChange(int previous, int current) {
        if (this.mSoundPool != null) {
            this.mSoundPool.play(this.mDidaSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
        }
        if (this.mOnValueChangeListener != null) {
            this.mOnValueChangeListener.onValueChange(this, previous, this.mValue);
        }
    }

    private void postChangeCurrentByOneFromLongPress(boolean increment, long delayMillis) {
        if (this.mChangeCurrentByOneFromLongPressCommand == null) {
            this.mChangeCurrentByOneFromLongPressCommand = new ChangeCurrentByOneFromLongPressCommand();
        } else {
            removeCallbacks(this.mChangeCurrentByOneFromLongPressCommand);
        }
        this.mChangeCurrentByOneFromLongPressCommand.setStep(increment);
        postDelayed(this.mChangeCurrentByOneFromLongPressCommand, delayMillis);
    }

    private void removeChangeCurrentByOneFromLongPress() {
        if (this.mChangeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(this.mChangeCurrentByOneFromLongPressCommand);
        }
    }

    private void postBeginSoftInputOnLongPressCommand() {
        if (this.mBeginSoftInputOnLongPressCommand == null) {
            this.mBeginSoftInputOnLongPressCommand = new BeginSoftInputOnLongPressCommand();
        } else {
            removeCallbacks(this.mBeginSoftInputOnLongPressCommand);
        }
        postDelayed(this.mBeginSoftInputOnLongPressCommand, (long) ViewConfiguration.getLongPressTimeout());
    }

    private void removeBeginSoftInputCommand() {
        if (this.mBeginSoftInputOnLongPressCommand != null) {
            removeCallbacks(this.mBeginSoftInputOnLongPressCommand);
        }
    }

    private void removeAllCallbacks() {
        if (this.mChangeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(this.mChangeCurrentByOneFromLongPressCommand);
        }
        if (this.mSetSelectionCommand != null) {
            removeCallbacks(this.mSetSelectionCommand);
        }
        if (this.mBeginSoftInputOnLongPressCommand != null) {
            removeCallbacks(this.mBeginSoftInputOnLongPressCommand);
        }
        this.mPressedStateHelper.cancel();
    }

    private int getSelectedPos(String value) {
        if (this.mDisplayedValues == null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return this.mMinValue;
            }
        }
        for (int i = 0; i < this.mDisplayedValues.length; i++) {
            value = value.toLowerCase();
            if (this.mDisplayedValues[i].toLowerCase().startsWith(value)) {
                return this.mMinValue + i;
            }
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e2) {
            return this.mMinValue;
        }
    }

    private void postSetSelectionCommand(int selectionStart, int selectionEnd) {
        if (this.mSetSelectionCommand == null) {
            this.mSetSelectionCommand = new SetSelectionCommand();
        } else {
            removeCallbacks(this.mSetSelectionCommand);
        }
        this.mSetSelectionCommand.mSelectionStart = selectionStart;
        this.mSetSelectionCommand.mSelectionEnd = selectionEnd;
        post(this.mSetSelectionCommand);
    }

    private boolean ensureScrollWheelAdjusted() {
        int deltaY = this.mInitialScrollOffset - this.mCurrentScrollOffset;
        if (deltaY == 0) {
            return false;
        }
        this.mPreviousScrollerY = 0;
        if (Math.abs(deltaY) > this.mSelectorElementHeight / 2) {
            deltaY += deltaY > 0 ? -this.mSelectorElementHeight : this.mSelectorElementHeight;
        }
        this.mAdjustScroller.startScroll(0, 0, 0, deltaY, SELECTOR_ADJUSTMENT_DURATION_MILLIS);
        invalidate();
        return true;
    }

    public void setOnButtonClickedListener(OnButtonClickedListener buttonClickedListener) {
        this.mButtonClickedListener = buttonClickedListener;
    }
}
