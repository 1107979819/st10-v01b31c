package com.yuneec.mission;

import com.yuneec.uartcontroller.UARTInfoMessage.MissionReply;

public interface MissionInterface {

    public enum MissionType {
        NONE,
        CRUVE_CABLE_CAM,
        JOURNEY,
        CYCLE,
        ROI
    }

    void cancel(MissionType missionType);

    void confirm(MissionType missionType);

    void exitPresentation();

    void pause(MissionType missionType);

    void prepare(MissionType missionType, Object obj);

    void query(MissionType missionType);

    void resume(MissionType missionType);

    void terminate();

    void updateFeedback(int i, MissionReply missionReply);
}
