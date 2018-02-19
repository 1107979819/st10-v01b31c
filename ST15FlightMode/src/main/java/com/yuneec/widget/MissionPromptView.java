package com.yuneec.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import com.yuneec.flightmode15.R;

public class MissionPromptView extends FrameLayout {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$yuneec$widget$MissionPromptView$MissionStep;
    private MissionStep currentStep;
    private String currentStepOriginString;
    private TextView currentStepTextView;
    private Handler mHandler;
    private TextView prepareText;
    private TextView runningText;

    public enum MissionStep {
        PREPARE,
        RUNING
    }

    static /* synthetic */ int[] $SWITCH_TABLE$com$yuneec$widget$MissionPromptView$MissionStep() {
        int[] iArr = $SWITCH_TABLE$com$yuneec$widget$MissionPromptView$MissionStep;
        if (iArr == null) {
            iArr = new int[MissionStep.values().length];
            try {
                iArr[MissionStep.PREPARE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[MissionStep.RUNING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            $SWITCH_TABLE$com$yuneec$widget$MissionPromptView$MissionStep = iArr;
        }
        return iArr;
    }

    public void updatStep(MissionStep step) {
        if (this.currentStep != step) {
            if (this.currentStepTextView != null) {
                this.currentStepTextView.setEnabled(false);
            }
            switch ($SWITCH_TABLE$com$yuneec$widget$MissionPromptView$MissionStep()[step.ordinal()]) {
                case 1:
                    this.currentStepTextView = this.prepareText;
                    break;
                case 2:
                    this.currentStepTextView = this.runningText;
                    break;
            }
            this.currentStepTextView.setEnabled(true);
            this.currentStep = step;
            this.currentStepOriginString = this.currentStepTextView.getText().toString();
        }
    }

    public void updateStepPrompt(MissionStep step, String prompt) {
        updatStep(step);
        final TextView currentStepView = this.currentStepTextView;
        final String currentText = new String(this.currentStepOriginString);
        currentStepView.setText(prompt);
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                currentStepView.setText(currentText);
            }
        }, 3000);
    }

    public void updateStepPrompt(MissionStep step, int promptResId) {
        updateStepPrompt(step, getContext().getString(promptResId));
    }

    public MissionPromptView(Context context) {
        super(context);
        init(context);
    }

    public MissionPromptView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @SuppressLint({"ResourceAsColor"})
    private void init(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        LayoutParams params = new LayoutParams(-1, -1);
        View view = layoutInflater.inflate(R.layout.mission_step_prompt, null);
        addView(view, params);
        this.prepareText = (TextView) view.findViewById(R.id.mission_prompt_prepare);
        this.runningText = (TextView) view.findViewById(R.id.mission_prompt_running);
        setBackgroundColor(17170445);
        this.mHandler = new Handler();
        this.currentStepTextView = this.prepareText;
        this.currentStepOriginString = this.currentStepTextView.getText().toString();
        updatStep(MissionStep.PREPARE);
    }
}
