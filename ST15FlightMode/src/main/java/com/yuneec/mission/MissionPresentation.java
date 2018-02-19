package com.yuneec.mission;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.view.MotionEventCompat;
import com.yuneec.flightmode15.R;
import com.yuneec.mission.MissionInterface.MissionType;
import com.yuneec.mission.MissionModel.MissionModelInterface;
import com.yuneec.uartcontroller.MissionData;
import com.yuneec.uartcontroller.UARTInfoMessage.MissionReply;

public class MissionPresentation implements MissionInterface {
    private Handler backgroundHandler;
    private MissionCallback callback;
    private MissionModelInterface missionModel;
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    private interface CommandCallback {
        void callback(MissionType missionType, boolean z, int i);
    }

    public interface MissionCallback {
        void onCancel(MissionType missionType, boolean z, int i);

        void onConfirm(MissionType missionType, boolean z, int i);

        void onPause(MissionType missionType, boolean z, int i);

        void onPrepare(MissionType missionType, boolean z, int i);

        void onQuery(MissionType missionType, boolean z, Object obj);

        void onResume(MissionType missionType, boolean z, int i);

        void onTerminate(boolean z, int i);

        void onUpdateState(MissionType missionType, MissionState missionState, int i);
    }

    public enum MissionState {
        NONE,
        PREPARE,
        RESUME,
        PAUSE,
        COMPLETE
    }

    public MissionPresentation(MissionCallback callback) {
        HandlerThread handlerThread = new HandlerThread("drone mission");
        handlerThread.start();
        this.backgroundHandler = new Handler(handlerThread.getLooper());
        this.callback = callback;
    }

    public void updateFeedback(int fMode, MissionReply feedbackData) {
        MissionType type;
        MissionState state;
        byte[] feedback = feedbackData.replyInfo;
        int missionState = feedback[1] & MotionEventCompat.ACTION_MASK;
        int missionProgress = feedback[3] & MotionEventCompat.ACTION_MASK;
        switch (feedback[2] & MotionEventCompat.ACTION_MASK) {
            case 0:
                type = MissionType.NONE;
                break;
            case 1:
                type = MissionType.JOURNEY;
                break;
            case 2:
                type = MissionType.ROI;
                break;
            case 3:
                type = MissionType.CRUVE_CABLE_CAM;
                break;
            case 4:
                type = MissionType.CYCLE;
                break;
            default:
                return;
        }
        switch (missionState) {
            case 0:
                state = MissionState.NONE;
                break;
            case 1:
                state = MissionState.PREPARE;
                break;
            case 2:
                state = MissionState.RESUME;
                break;
            case 3:
                state = MissionState.PAUSE;
                break;
            case 4:
                state = MissionState.COMPLETE;
                break;
            default:
                return;
        }
        this.callback.onUpdateState(type, state, missionProgress);
    }

    private void onCommandCallback(final CommandCallback callbackImplemention, final MissionType missionType, final int result) {
        if (result == 0) {
            this.uiHandler.post(new Runnable() {
                public void run() {
                    callbackImplemention.callback(missionType, true, R.string.mission_command_reason_ok);
                }
            });
        } else {
            this.uiHandler.post(new Runnable() {
                public void run() {
                    int reasonResourceId = R.string.mission_command_reason_execute_fail;
                    switch (result) {
                        case 80:
                            reasonResourceId = R.string.mission_command_reason_error_mission;
                            break;
                        case 81:
                            reasonResourceId = R.string.mission_command_reason_other_mission_occupited;
                            break;
                        case 82:
                            reasonResourceId = R.string.mission_command_reason_setting_step_error;
                            break;
                        case MissionData.ACTION_RESULT_ERR_SETTING_NO_WAYPOINT /*83*/:
                            reasonResourceId = R.string.mission_command_reason_no_waypoint;
                            break;
                    }
                    callbackImplemention.callback(missionType, false, reasonResourceId);
                }
            });
        }
    }

    public void prepare(final MissionType missionType, final Object parameter) {
        if (this.missionModel == null || missionType != this.missionModel.getMissionType()) {
            this.missionModel = MissionModel.misssionModelCreate(missionType);
        }
        this.backgroundHandler.post(new Runnable() {
            public void run() {
                MissionPresentation.this.onCommandCallback(new CommandCallback() {
                    public void callback(MissionType missionType, boolean result, int reasonResouceId) {
                        MissionPresentation.this.callback.onPrepare(missionType, result, reasonResouceId);
                    }
                }, missionType, MissionPresentation.this.missionModel.prepare(parameter));
            }
        });
    }

    public void confirm(final MissionType missionType) {
        if (this.missionModel == null || missionType != this.missionModel.getMissionType()) {
            onCommandCallback(new CommandCallback() {
                public void callback(MissionType missionType, boolean result, int reasonResouceId) {
                    MissionPresentation.this.callback.onConfirm(missionType, result, reasonResouceId);
                }
            }, missionType, 82);
        } else {
            this.backgroundHandler.post(new Runnable() {
                public void run() {
                    MissionPresentation.this.onCommandCallback(new CommandCallback() {
                        public void callback(MissionType missionType, boolean result, int reasonResouceId) {
                            MissionPresentation.this.callback.onConfirm(missionType, result, reasonResouceId);
                        }
                    }, missionType, MissionPresentation.this.missionModel.confirm());
                }
            });
        }
    }

    public void cancel(final MissionType missionType) {
        if (this.missionModel == null || missionType != this.missionModel.getMissionType()) {
            onCommandCallback(new CommandCallback() {
                public void callback(MissionType missionType, boolean result, int reasonResouceId) {
                    MissionPresentation.this.callback.onCancel(missionType, result, reasonResouceId);
                }
            }, missionType, 82);
        } else {
            this.backgroundHandler.post(new Runnable() {
                public void run() {
                    MissionPresentation.this.onCommandCallback(new CommandCallback() {
                        public void callback(MissionType missionType, boolean result, int reasonResouceId) {
                            MissionPresentation.this.callback.onCancel(missionType, result, reasonResouceId);
                        }
                    }, missionType, MissionPresentation.this.missionModel.cancel());
                }
            });
        }
    }

    public void terminate() {
        if (this.missionModel == null) {
            onCommandCallback(new CommandCallback() {
                public void callback(MissionType missionType, boolean result, int reasonResouceId) {
                    MissionPresentation.this.callback.onTerminate(result, reasonResouceId);
                }
            }, MissionType.NONE, 82);
        } else {
            this.backgroundHandler.post(new Runnable() {
                public void run() {
                    MissionPresentation.this.onCommandCallback(new CommandCallback() {
                        public void callback(MissionType missionType, boolean result, int reasonResouceId) {
                            MissionPresentation.this.callback.onTerminate(result, reasonResouceId);
                        }
                    }, MissionPresentation.this.missionModel.getMissionType(), MissionPresentation.this.missionModel.terminate());
                }
            });
        }
    }

    public void exitPresentation() {
        if (this.backgroundHandler != null) {
            this.backgroundHandler.removeCallbacksAndMessages(null);
            this.backgroundHandler.getLooper().quit();
            this.backgroundHandler = null;
        }
    }

    public void query(final MissionType missionType) {
        if (this.missionModel == null || missionType != this.missionModel.getMissionType()) {
            this.missionModel = MissionModel.misssionModelCreate(missionType);
        }
        this.backgroundHandler.post(new Runnable() {
            public void run() {
                final Object result = MissionPresentation.this.missionModel.query();
                Handler access$3 = MissionPresentation.this.uiHandler;
                final MissionType missionType = missionType;
                access$3.post(new Runnable() {
                    public void run() {
                        MissionPresentation.this.callback.onQuery(missionType, result != null, result);
                    }
                });
            }
        });
    }

    public void pause(final MissionType missionType) {
        if (this.missionModel == null || missionType != this.missionModel.getMissionType()) {
            onCommandCallback(new CommandCallback() {
                public void callback(MissionType missionType, boolean result, int reasonResouceId) {
                    MissionPresentation.this.callback.onPause(missionType, result, reasonResouceId);
                }
            }, missionType, 82);
        } else {
            this.backgroundHandler.post(new Runnable() {
                public void run() {
                    MissionPresentation.this.onCommandCallback(new CommandCallback() {
                        public void callback(MissionType missionType, boolean result, int reasonResouceId) {
                            MissionPresentation.this.callback.onPause(missionType, result, reasonResouceId);
                        }
                    }, missionType, MissionPresentation.this.missionModel.pause());
                }
            });
        }
    }

    public void resume(final MissionType missionType) {
        if (this.missionModel == null || missionType != this.missionModel.getMissionType()) {
            onCommandCallback(new CommandCallback() {
                public void callback(MissionType missionType, boolean result, int reasonResouceId) {
                    MissionPresentation.this.callback.onResume(missionType, result, reasonResouceId);
                }
            }, missionType, 82);
        } else {
            this.backgroundHandler.post(new Runnable() {
                public void run() {
                    MissionPresentation.this.onCommandCallback(new CommandCallback() {
                        public void callback(MissionType missionType, boolean result, int reasonResouceId) {
                            MissionPresentation.this.callback.onResume(missionType, result, reasonResouceId);
                        }
                    }, missionType, MissionPresentation.this.missionModel.resume());
                }
            });
        }
    }
}
