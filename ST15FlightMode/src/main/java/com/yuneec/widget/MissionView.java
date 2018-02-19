package com.yuneec.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;
import com.yuneec.mission.MissionInterface.MissionType;
import com.yuneec.mission.MissionPresentation;
import com.yuneec.mission.MissionPresentation.MissionCallback;
import com.yuneec.mission.MissionPresentation.MissionState;
import com.yuneec.mission.SaveMissionData;
import com.yuneec.uartcontroller.GPSUpLinkData;
import com.yuneec.uartcontroller.RoiData;
import com.yuneec.uartcontroller.UARTInfoMessage.MissionReply;
import com.yuneec.uartcontroller.WaypointData;
import com.yuneec.widget.MissionPromptView.MissionStep;
import java.util.ArrayList;

public class MissionView extends FrameLayout implements OnClickListener {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$yuneec$mission$MissionPresentation$MissionState = null;
    private static final String LAST_POINTS_TABLE_NAME = "last_points_table";
    private Button addPointButton;
    private Button backButton;
    private Button cccButton;
    private Button clearPointButton;
    private Button confirmButton;
    private MissionState currentMissionState = MissionState.NONE;
    private MissionType currentMissionType = MissionType.NONE;
    private Button cycleButton;
    private Button deletePointButton;
    private float droneAltitude;
    private float droneLatitude;
    private float droneLongitude;
    private Button exitButton;
    private int fMode = 0;
    private Button journeyButton;
    private Button loadRecordButton;
    private LinearLayout missionMenuView;
    private MissionPresentation missionPresentation;
    private MissionPromptView missionPromptView;
    private MissionViewCallback missionViewCallback;
    private Button pauseButton;
    private ArrayList<WaypointData> recordPoints = new ArrayList();
    private Button resumeButton;
    private Button roiButton;
    private RoiData roiData = new RoiData();
    private Button setCenterButton;

    public interface MissionViewCallback {
        GPSUpLinkData getControllerGps();

        void updateError(int i);

        void updateStep(MissionState missionState);
    }

    static /* synthetic */ int[] $SWITCH_TABLE$com$yuneec$mission$MissionPresentation$MissionState() {
        int[] iArr = $SWITCH_TABLE$com$yuneec$mission$MissionPresentation$MissionState;
        if (iArr == null) {
            iArr = new int[MissionState.values().length];
            try {
                iArr[MissionState.COMPLETE.ordinal()] = 5;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[MissionState.NONE.ordinal()] = 1;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[MissionState.PAUSE.ordinal()] = 4;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[MissionState.PREPARE.ordinal()] = 2;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[MissionState.RESUME.ordinal()] = 3;
            } catch (NoSuchFieldError e5) {
            }
            $SWITCH_TABLE$com$yuneec$mission$MissionPresentation$MissionState = iArr;
        }
        return iArr;
    }

    public MissionView(Context context) {
        super(context);
        init(context);
    }

    public MissionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void updateFeedback(MissionReply feedbackData) {
        this.missionPresentation.updateFeedback(this.fMode, feedbackData);
    }

    public void setMissionViewCallback(MissionViewCallback missionViewCallback) {
        this.missionViewCallback = missionViewCallback;
    }

    public void updateDroneGps(int fMode, float latitude, float longitude, float altitude) {
        this.droneLatitude = latitude;
        this.droneLongitude = longitude;
        this.droneAltitude = altitude;
        this.fMode = fMode;
    }

    @SuppressLint({"ResourceAsColor"})
    private void init(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.missionPresentation = new MissionPresentation(new MissionCallback() {
            private static /* synthetic */ int[] $SWITCH_TABLE$com$yuneec$mission$MissionPresentation$MissionState;

            static /* synthetic */ int[] $SWITCH_TABLE$com$yuneec$mission$MissionPresentation$MissionState() {
                int[] iArr = $SWITCH_TABLE$com$yuneec$mission$MissionPresentation$MissionState;
                if (iArr == null) {
                    iArr = new int[MissionState.values().length];
                    try {
                        iArr[MissionState.COMPLETE.ordinal()] = 5;
                    } catch (NoSuchFieldError e) {
                    }
                    try {
                        iArr[MissionState.NONE.ordinal()] = 1;
                    } catch (NoSuchFieldError e2) {
                    }
                    try {
                        iArr[MissionState.PAUSE.ordinal()] = 4;
                    } catch (NoSuchFieldError e3) {
                    }
                    try {
                        iArr[MissionState.PREPARE.ordinal()] = 2;
                    } catch (NoSuchFieldError e4) {
                    }
                    try {
                        iArr[MissionState.RESUME.ordinal()] = 3;
                    } catch (NoSuchFieldError e5) {
                    }
                    $SWITCH_TABLE$com$yuneec$mission$MissionPresentation$MissionState = iArr;
                }
                return iArr;
            }

            public void onUpdateState(MissionType type, MissionState state, int progress) {
                switch (AnonymousClass1.$SWITCH_TABLE$com$yuneec$mission$MissionPresentation$MissionState()[state.ordinal()]) {
                    case 1:
                        MissionView.this.missionPromptView.updatStep(MissionStep.PREPARE);
                        break;
                    case 2:
                        MissionView.this.missionPromptView.updatStep(MissionStep.PREPARE);
                        break;
                    case 3:
                        MissionView.this.missionPromptView.updateStepPrompt(MissionStep.RUNING, "Resume");
                        break;
                    case 4:
                        MissionView.this.missionPromptView.updateStepPrompt(MissionStep.RUNING, "Pause");
                        break;
                    case 5:
                        MissionView.this.missionPromptView.updatStep(MissionStep.PREPARE);
                        break;
                }
                MissionView.this.missionViewCallback.updateStep(state);
                MissionView.this.currentMissionState = state;
                MissionView.this.updateControlButtonsEnable(state);
            }

            public void onTerminate(boolean result, int reasonResId) {
                if (result) {
                    MissionView.this.currentMissionState = MissionState.COMPLETE;
                    MissionView.this.missionPromptView.updatStep(MissionStep.PREPARE);
                    MissionView.this.addMissionSelectMenu();
                } else if (MissionView.this.missionViewCallback != null) {
                    MissionView.this.missionViewCallback.updateError(reasonResId);
                }
            }

            public void onResume(MissionType type, boolean result, int reasonResId) {
                if (result) {
                    MissionView.this.currentMissionState = MissionState.RESUME;
                    MissionView.this.missionPromptView.updateStepPrompt(MissionStep.RUNING, "Resume");
                } else if (MissionView.this.missionViewCallback != null) {
                    MissionView.this.missionViewCallback.updateError(reasonResId);
                }
            }

            public void onQuery(MissionType type, boolean result, Object answer) {
                if (result) {
                    if (type == MissionType.CRUVE_CABLE_CAM && (answer instanceof WaypointData)) {
                        WaypointData waypointData = (WaypointData) answer;
                        waypointData.pointerIndex = MissionView.this.recordPoints.size();
                        MissionView.this.recordPoints.add(waypointData);
                        MissionView.this.missionPromptView.updateStepPrompt(MissionStep.PREPARE, "Point: " + MissionView.this.recordPoints.size());
                    }
                } else if (MissionView.this.missionViewCallback != null) {
                    MissionView.this.missionViewCallback.updateError(R.string.mission_command_reason_query_null);
                }
            }

            public void onPrepare(MissionType type, boolean result, int reasonResId) {
                if (result) {
                    MissionView.this.currentMissionState = MissionState.PREPARE;
                    MissionView.this.missionPresentation.confirm(type);
                } else if (MissionView.this.missionViewCallback != null) {
                    MissionView.this.missionViewCallback.updateError(reasonResId);
                }
            }

            public void onPause(MissionType type, boolean result, int reasonResId) {
                if (result) {
                    MissionView.this.currentMissionState = MissionState.PAUSE;
                    MissionView.this.missionPromptView.updateStepPrompt(MissionStep.RUNING, "Pause");
                } else if (MissionView.this.missionViewCallback != null) {
                    MissionView.this.missionViewCallback.updateError(reasonResId);
                }
            }

            public void onConfirm(MissionType type, boolean result, int reasonResId) {
                if (result) {
                    MissionView.this.currentMissionState = MissionState.RESUME;
                    MissionView.this.missionPromptView.updatStep(MissionStep.RUNING);
                    MissionView.this.addMissionControlMenu();
                } else if (MissionView.this.missionViewCallback != null) {
                    MissionView.this.missionViewCallback.updateError(reasonResId);
                }
            }

            public void onCancel(MissionType type, boolean result, int reasonResId) {
                if (result) {
                    MissionView.this.currentMissionState = MissionState.NONE;
                    MissionView.this.missionPromptView.updatStep(MissionStep.PREPARE);
                    MissionView.this.addMissionSelectMenu();
                } else if (MissionView.this.missionViewCallback != null) {
                    MissionView.this.missionViewCallback.updateError(reasonResId);
                }
            }
        });
        initButton(context);
        LayoutParams params = new LayoutParams(-1, -1);
        View view = layoutInflater.inflate(R.layout.mission_frame, null);
        addView(view, params);
        setBackgroundColor(17170445);
        this.missionMenuView = (LinearLayout) view.findViewById(R.id.mission_menu_view);
        this.missionPromptView = (MissionPromptView) view.findViewById(R.id.mission_prompt_view);
        addMissionSelectMenu();
        setVisibility(8);
    }

    private void updateControlButtonsEnable(MissionState missionState) {
        switch ($SWITCH_TABLE$com$yuneec$mission$MissionPresentation$MissionState()[missionState.ordinal()]) {
            case 1:
                this.resumeButton.setEnabled(true);
                this.pauseButton.setEnabled(true);
                return;
            case 2:
                this.resumeButton.setEnabled(false);
                this.pauseButton.setEnabled(false);
                return;
            case 3:
                this.resumeButton.setEnabled(false);
                this.pauseButton.setEnabled(true);
                return;
            case 4:
                this.resumeButton.setEnabled(true);
                this.pauseButton.setEnabled(false);
                return;
            case 5:
                this.resumeButton.setEnabled(true);
                this.pauseButton.setEnabled(true);
                return;
            default:
                return;
        }
    }

    private void initButton(Context context) {
        this.cccButton = new Button(context);
        this.cccButton.setText(R.string.mission_ccc_button);
        this.cccButton.setTextSize(8.0f);
        this.cccButton.setBackgroundResource(R.drawable.corners_round);
        this.cccButton.setOnClickListener(this);
        this.journeyButton = new Button(context);
        this.journeyButton.setText(R.string.mission_journey_button);
        this.journeyButton.setTextSize(8.0f);
        this.journeyButton.setBackgroundResource(R.drawable.corners_round);
        this.journeyButton.setOnClickListener(this);
        this.roiButton = new Button(context);
        this.roiButton.setText(R.string.mission_roi_button);
        this.roiButton.setTextSize(8.0f);
        this.roiButton.setBackgroundResource(R.drawable.corners_round);
        this.roiButton.setOnClickListener(this);
        this.cycleButton = new Button(context);
        this.cycleButton.setText(R.string.mission_cycle_button);
        this.cycleButton.setTextSize(8.0f);
        this.cycleButton.setBackgroundResource(R.drawable.corners_round);
        this.cycleButton.setOnClickListener(this);
        this.backButton = new Button(context);
        this.backButton.setText(R.string.mission_back_button);
        this.backButton.setTextSize(8.0f);
        this.backButton.setBackgroundResource(R.drawable.corners_round);
        this.backButton.setOnClickListener(this);
        this.addPointButton = new Button(context);
        this.addPointButton.setText(R.string.mission_add_point_button);
        this.addPointButton.setTextSize(8.0f);
        this.addPointButton.setBackgroundResource(R.drawable.corners_round);
        this.addPointButton.setOnClickListener(this);
        this.deletePointButton = new Button(context);
        this.deletePointButton.setText(R.string.mission_delete_point_button);
        this.deletePointButton.setTextSize(8.0f);
        this.deletePointButton.setBackgroundResource(R.drawable.corners_round);
        this.deletePointButton.setOnClickListener(this);
        this.clearPointButton = new Button(context);
        this.clearPointButton.setText(R.string.mission_clear_point_button);
        this.clearPointButton.setTextSize(8.0f);
        this.clearPointButton.setBackgroundResource(R.drawable.corners_round);
        this.clearPointButton.setOnClickListener(this);
        this.loadRecordButton = new Button(context);
        this.loadRecordButton.setText(R.string.mission_load_previous_button);
        this.loadRecordButton.setTextSize(8.0f);
        this.loadRecordButton.setBackgroundResource(R.drawable.corners_round);
        this.loadRecordButton.setOnClickListener(this);
        this.setCenterButton = new Button(context);
        this.setCenterButton.setText(R.string.mission_set_center_button);
        this.setCenterButton.setTextSize(8.0f);
        this.setCenterButton.setBackgroundResource(R.drawable.corners_round);
        this.setCenterButton.setOnClickListener(this);
        this.confirmButton = new Button(context);
        this.confirmButton.setText(R.string.mission_start_button);
        this.confirmButton.setTextSize(8.0f);
        this.confirmButton.setBackgroundResource(R.drawable.corners_round);
        this.confirmButton.setOnClickListener(this);
        this.resumeButton = new Button(context);
        this.resumeButton.setText(R.string.mission_resume_button);
        this.resumeButton.setTextSize(8.0f);
        this.resumeButton.setBackgroundResource(R.drawable.corners_round);
        this.resumeButton.setOnClickListener(this);
        this.pauseButton = new Button(context);
        this.pauseButton.setText(R.string.mission_pause_button);
        this.pauseButton.setTextSize(8.0f);
        this.pauseButton.setBackgroundResource(R.drawable.corners_round);
        this.pauseButton.setOnClickListener(this);
        this.exitButton = new Button(context);
        this.exitButton.setText(R.string.mission_exit_button);
        this.exitButton.setTextSize(8.0f);
        this.exitButton.setBackgroundResource(R.drawable.corners_round);
        this.exitButton.setOnClickListener(this);
    }

    private void addMissionSelectMenu() {
        this.missionMenuView.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(75, 50);
        params.rightMargin = 6;
        this.missionMenuView.addView(this.cccButton, params);
        this.missionMenuView.addView(this.journeyButton, params);
        this.missionMenuView.addView(this.roiButton, params);
        LinearLayout.LayoutParams paramsNoMargin = new LinearLayout.LayoutParams(80, 50);
        paramsNoMargin.rightMargin = 0;
        this.missionMenuView.addView(this.cycleButton, paramsNoMargin);
        this.missionPromptView.setVisibility(8);
    }

    private void addCCCSettingMenu() {
        this.missionMenuView.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(75, 50);
        params.rightMargin = 6;
        this.missionMenuView.addView(this.backButton, params);
        this.missionMenuView.addView(this.addPointButton, params);
        this.missionMenuView.addView(this.deletePointButton, params);
        this.missionMenuView.addView(this.clearPointButton, params);
        this.missionMenuView.addView(this.loadRecordButton, params);
        LinearLayout.LayoutParams paramsNoMargin = new LinearLayout.LayoutParams(75, 50);
        paramsNoMargin.rightMargin = 0;
        this.missionMenuView.addView(this.confirmButton, paramsNoMargin);
        this.missionPromptView.setVisibility(0);
    }

    private void addRoiSettingMenu() {
        this.missionMenuView.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(75, 50);
        params.rightMargin = 6;
        this.missionMenuView.addView(this.backButton, params);
        this.missionMenuView.addView(this.setCenterButton, params);
        LinearLayout.LayoutParams paramsNoMargin = new LinearLayout.LayoutParams(75, 50);
        paramsNoMargin.rightMargin = 0;
        this.missionMenuView.addView(this.confirmButton, paramsNoMargin);
        this.missionPromptView.setVisibility(0);
    }

    private void addOtherSettingMenu() {
        this.missionMenuView.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(75, 50);
        params.rightMargin = 6;
        this.missionMenuView.addView(this.backButton, params);
        LinearLayout.LayoutParams paramsNoMargin = new LinearLayout.LayoutParams(75, 50);
        paramsNoMargin.rightMargin = 0;
        this.missionMenuView.addView(this.confirmButton, paramsNoMargin);
        this.missionPromptView.setVisibility(0);
    }

    private void addMissionControlMenu() {
        this.missionMenuView.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(80, 50);
        params.rightMargin = 6;
        this.missionMenuView.addView(this.resumeButton, params);
        this.missionMenuView.addView(this.pauseButton, params);
        LinearLayout.LayoutParams paramsNoMargin = new LinearLayout.LayoutParams(80, 50);
        paramsNoMargin.rightMargin = 0;
        this.missionMenuView.addView(this.exitButton, paramsNoMargin);
        this.missionPromptView.setVisibility(0);
    }

    public void onClick(View v) {
        if (v == this.cccButton) {
            this.currentMissionType = MissionType.CRUVE_CABLE_CAM;
            addCCCSettingMenu();
            this.recordPoints.clear();
        } else if (v == this.journeyButton) {
            this.currentMissionType = MissionType.JOURNEY;
            addOtherSettingMenu();
        } else if (v == this.roiButton) {
            this.currentMissionType = MissionType.ROI;
            this.roiData.reset();
            addRoiSettingMenu();
        } else if (v == this.cycleButton) {
            this.currentMissionType = MissionType.CYCLE;
            addOtherSettingMenu();
        } else if (v == this.backButton) {
            this.currentMissionType = MissionType.NONE;
            this.currentMissionState = MissionState.NONE;
            this.missionPresentation.terminate();
            addMissionSelectMenu();
        } else if (v == this.addPointButton) {
            this.missionPresentation.query(MissionType.CRUVE_CABLE_CAM);
        } else if (v == this.deletePointButton) {
            if (this.recordPoints.size() > 0) {
                this.recordPoints.remove(this.recordPoints.size() - 1);
            }
            this.missionPromptView.updateStepPrompt(MissionStep.PREPARE, "Point: " + this.recordPoints.size());
        } else if (v == this.clearPointButton) {
            this.recordPoints.clear();
            this.missionPromptView.updateStepPrompt(MissionStep.PREPARE, "Clear");
        } else if (v == this.loadRecordButton) {
            this.recordPoints.clear();
            SaveMissionData.loadCruveCableCam(LAST_POINTS_TABLE_NAME, this.recordPoints);
            this.missionPromptView.updateStepPrompt(MissionStep.PREPARE, "Point: " + this.recordPoints.size());
        } else if (v == this.confirmButton) {
            if (this.currentMissionType == MissionType.CRUVE_CABLE_CAM) {
                SaveMissionData.saveCruveCableCam(LAST_POINTS_TABLE_NAME, this.recordPoints);
                this.missionPresentation.prepare(this.currentMissionType, this.recordPoints);
            } else if (this.currentMissionType == MissionType.ROI) {
                if (this.roiData.centerLatitude == 0.0f || this.roiData.centerLongitude == 0.0f) {
                    if (this.missionViewCallback != null) {
                        this.missionViewCallback.updateError(R.string.mission_command_reason_no_center);
                    }
                } else if (this.missionViewCallback != null) {
                    GPSUpLinkData controllerGps = this.missionViewCallback.getControllerGps();
                    float[] result = Utilities.calculateDistanceAndBearing(this.roiData.centerLatitude, this.roiData.centerLongitude, 0.0f, controllerGps.lat, controllerGps.lon, 0.0f);
                    if (result[0] <= 1000.0f) {
                        this.roiData.radius = Math.round(result[0]);
                        this.roiData.speed = 5;
                        this.missionPresentation.prepare(this.currentMissionType, this.roiData);
                    } else if (this.missionViewCallback != null) {
                        this.missionViewCallback.updateError(R.string.mission_command_reason_radius_over_range);
                    }
                }
            } else if (this.currentMissionType == MissionType.JOURNEY) {
                this.missionPresentation.prepare(this.currentMissionType, new Integer(10));
            } else if (this.currentMissionType == MissionType.CYCLE) {
                this.missionPresentation.prepare(this.currentMissionType, null);
            }
        } else if (v == this.setCenterButton) {
            this.roiData.centerLatitude = this.droneLatitude;
            this.roiData.centerLongitude = this.droneLongitude;
            this.roiData.centerAltitude = this.droneAltitude;
            this.missionPromptView.updateStepPrompt(MissionStep.PREPARE, "Set Center");
        } else if (v == this.resumeButton) {
            this.missionPresentation.resume(this.currentMissionType);
        } else if (v == this.pauseButton) {
            this.missionPresentation.pause(this.currentMissionType);
        } else if (v == this.exitButton) {
            this.missionPresentation.terminate();
        }
    }
}
