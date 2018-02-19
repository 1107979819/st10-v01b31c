package com.yuneec.mission;

import com.yuneec.mission.MissionInterface.MissionType;
import com.yuneec.uartcontroller.RoiData;
import com.yuneec.uartcontroller.UARTController;
import com.yuneec.uartcontroller.WaypointData;
import java.util.ArrayList;
import java.util.Iterator;

public class MissionModel {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$yuneec$mission$MissionInterface$MissionType;

    public interface MissionModelInterface {
        int cancel();

        int confirm();

        MissionType getMissionType();

        int pause();

        int prepare(Object obj) throws IllegalArgumentException;

        Object query();

        int resume();

        int terminate();
    }

    public static abstract class BaseMode implements MissionModelInterface {
        protected abstract int getModeTypeCommand();

        public Object query() {
            return null;
        }

        protected int getWaypointSize() {
            return 0;
        }

        public int pause() {
            if (UARTController.getInstance().sendMissionRequest(2, getModeTypeCommand(), getWaypointSize())) {
                return 0;
            }
            return 1;
        }

        public int resume() {
            if (UARTController.getInstance().sendMissionRequest(3, getModeTypeCommand(), getWaypointSize())) {
                return 0;
            }
            return 1;
        }

        public int cancel() {
            if (UARTController.getInstance().sendMissionRequest(4, getModeTypeCommand(), getWaypointSize())) {
                return 0;
            }
            return 1;
        }

        public int terminate() {
            if (UARTController.getInstance().sendMissionRequest(4, getModeTypeCommand(), getWaypointSize())) {
                return 0;
            }
            return 1;
        }
    }

    public static class ModelCcc extends BaseMode {
        private ArrayList<WaypointData> recordWaypoint;

        public MissionType getMissionType() {
            return MissionType.CRUVE_CABLE_CAM;
        }

        protected int getModeTypeCommand() {
            return 3;
        }

        protected int getWaypointSize() {
            return this.recordWaypoint == null ? 0 : this.recordWaypoint.size();
        }

        public Object query() {
            WaypointData waypointData = new WaypointData();
            return UARTController.getInstance().sendMissionRequestGetWaypoint(waypointData) ? waypointData : null;
        }

        public int prepare(Object parameter) throws IllegalArgumentException {
            if (parameter instanceof ArrayList) {
                this.recordWaypoint = (ArrayList) parameter;
                return 0;
            }
            throw new IllegalArgumentException();
        }

        public int confirm() {
            int result = 1;
            if (this.recordWaypoint == null || this.recordWaypoint.size() == 0) {
                return 83;
            }
            if (!UARTController.getInstance().sendMissionRequest(6, getModeTypeCommand(), getWaypointSize())) {
                return 1;
            }
            Iterator it = this.recordWaypoint.iterator();
            while (it.hasNext()) {
                if (!UARTController.getInstance().sendMissionSettingCCC((WaypointData) it.next())) {
                    return 1;
                }
            }
            if (UARTController.getInstance().sendMissionRequest(0, getModeTypeCommand(), getWaypointSize())) {
                result = 0;
            }
            return result;
        }
    }

    public static class ModelCycle extends BaseMode {
        public MissionType getMissionType() {
            return MissionType.CYCLE;
        }

        protected int getModeTypeCommand() {
            return 4;
        }

        public int prepare(Object parameter) throws IllegalArgumentException {
            return 0;
        }

        public int confirm() {
            if (UARTController.getInstance().sendMissionRequest(0, getModeTypeCommand(), 0)) {
                return 0;
            }
            return 1;
        }
    }

    public static class ModelJourney extends BaseMode {
        private Integer journeyDistance;

        public MissionType getMissionType() {
            return MissionType.JOURNEY;
        }

        protected int getModeTypeCommand() {
            return 1;
        }

        public int prepare(Object parameter) throws IllegalArgumentException {
            if (parameter instanceof Integer) {
                this.journeyDistance = (Integer) parameter;
                return 0;
            }
            throw new IllegalArgumentException();
        }

        public int confirm() {
            int i = 0;
            if (this.journeyDistance == null) {
                return 1;
            }
            if (!UARTController.getInstance().sendMissionRequest(0, getModeTypeCommand(), this.journeyDistance.intValue())) {
                i = 1;
            }
            return i;
        }
    }

    public static class ModelRoi extends BaseMode {
        private RoiData roiData;

        public MissionType getMissionType() {
            return MissionType.ROI;
        }

        protected int getModeTypeCommand() {
            return 2;
        }

        public int prepare(Object parameter) throws IllegalArgumentException {
            if (parameter instanceof RoiData) {
                this.roiData = (RoiData) parameter;
                return 0;
            }
            throw new IllegalArgumentException();
        }

        public int confirm() {
            int result = 1;
            if (this.roiData == null) {
                return 83;
            }
            if (!UARTController.getInstance().sendMissionRequest(6, getModeTypeCommand(), getWaypointSize())) {
                return 1;
            }
            if (!UARTController.getInstance().sendMissionSettingRoiCenter(this.roiData)) {
                return 1;
            }
            if (UARTController.getInstance().sendMissionRequest(0, getModeTypeCommand(), getWaypointSize())) {
                result = 0;
            }
            return result;
        }
    }

    static /* synthetic */ int[] $SWITCH_TABLE$com$yuneec$mission$MissionInterface$MissionType() {
        int[] iArr = $SWITCH_TABLE$com$yuneec$mission$MissionInterface$MissionType;
        if (iArr == null) {
            iArr = new int[MissionType.values().length];
            try {
                iArr[MissionType.CRUVE_CABLE_CAM.ordinal()] = 2;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[MissionType.CYCLE.ordinal()] = 4;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[MissionType.JOURNEY.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[MissionType.NONE.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[MissionType.ROI.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            $SWITCH_TABLE$com$yuneec$mission$MissionInterface$MissionType = iArr;
        }
        return iArr;
    }

    public static MissionModelInterface misssionModelCreate(MissionType type) {
        switch ($SWITCH_TABLE$com$yuneec$mission$MissionInterface$MissionType()[type.ordinal()]) {
            case 2:
                return new ModelCcc();
            case 3:
                return new ModelJourney();
            case 4:
                return new ModelCycle();
            case 5:
                return new ModelRoi();
            default:
                return null;
        }
    }
}
