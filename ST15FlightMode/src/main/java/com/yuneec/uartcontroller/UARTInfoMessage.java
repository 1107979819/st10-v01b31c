package com.yuneec.uartcontroller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class UARTInfoMessage {
    public static final int BIND_STATE = 12;
    public static final int CALIBRATION_RAW_INFO = 20;
    public static final int CALIBRATION_STATE_INFO = 18;
    public static final int CHANNEL_INFO = 2;
    public static final int FEEDBACK_TELEMETRY_COORDINATES_INFO = 141;
    public static final int FEEDBACK_TELEMETRY_INFORMATION_INFO = 101;
    public static final int MISSION_REPLY = 23;
    public static final int MIXED_CHANNEL_INFO = 5;
    public static final int RADIO_VERSION = 17;
    public static final int RXBIND_INFO = 11;
    public static final int SIGNAL_VALUE = 22;
    public static final int STICK_INFO = 3;
    public static final int SWITCH_CHANGED = 14;
    public static final int SWITCH_STATE = 15;
    public static final int TELEMETRY_INFO = 13;
    public static final int TRANSMITTER_INFO = 1;
    public static final int TRANSMITTER_VERSION = 16;
    public static final int TRIM_INFO = 4;
    public static final int TXBL_VERSION = 21;
    public static final int TX_IN_UPDATE_MODE = 19;
    public int what;

    public static class BindState extends UARTInfoMessage {
        public static final int BOUND = 1;
        public static final int NOT_BOUND = 0;
        public int state = 0;
    }

    public static class CalibrationRawData extends UARTInfoMessage {
        public ArrayList<Integer> rawData = new ArrayList();
    }

    public static class CalibrationState extends UARTInfoMessage {
        public static final int STATE_MAX = 3;
        public static final int STATE_MID = 2;
        public static final int STATE_MIN = 1;
        public static final int STATE_NONE = 0;
        public static final int STATE_RAG = 4;
        public ArrayList<Integer> hardware_state = new ArrayList();
    }

    public static class Channel extends UARTInfoMessage {
        public ArrayList<Float> channels = new ArrayList();

        public String toString() {
            StringBuilder strBuilder = new StringBuilder();
            String timestamp = new SimpleDateFormat("yyyyMMdd HH:mm:ss:SSS").format(new Date());
            for (int i = 0; i < this.channels.size(); i++) {
                strBuilder.append("," + this.channels.get(i));
            }
            return new StringBuilder(String.valueOf(timestamp)).append(strBuilder.toString()).append("\n").toString();
        }
    }

    public static class MissionReply extends UARTInfoMessage {
        public byte[] replyInfo;

        public MissionReply() {
            this.what = 23;
        }
    }

    public static class RxBindInfo extends UARTInfoMessage {
        public int a_bit;
        public int a_num;
        public int mode;
        public int node_id;
        public int pan_id;
        public int sw_bit;
        public int sw_num;
        public int tx_addr;
    }

    public static class Signal extends UARTInfoMessage {
        public int rf_signal;
        public int tx_signal;
    }

    public static class Stick extends UARTInfoMessage {
        public float ch1;
        public float ch2;
        public float ch3;
        public float ch4;
    }

    public static class SwitchChanged extends UARTInfoMessage {
        public int hw_id;
        public int new_state;
        public int old_state;
    }

    public static class SwitchState extends UARTInfoMessage {
        public int hw_id;
        public int state;
    }

    public static class Telemetry extends UARTInfoMessage {
        public static final int ERROR_FLAG_AIRPORT_WARNING = 128;
        public static final int ERROR_FLAG_COMPASS_CALIBRATION_WARNING = 32;
        public static final int ERROR_FLAG_COMPLETE_MOTOR_ESC_FAILURE = 8;
        public static final int ERROR_FLAG_FLYAWAY_CHECKER_WARNING = 64;
        public static final int ERROR_FLAG_HIGH_TEMPERATURE_WARNING = 16;
        public static final int ERROR_FLAG_MOTOR_FAILSAFE_MODE = 4;
        public static final int ERROR_FLAG_VOLTAGE_WARNING1 = 1;
        public static final int ERROR_FLAG_VOLTAGE_WARNING2 = 2;
        public float altitude;
        public int cgps_status;
        public float current;
        public int error_flags1;
        public int f_mode;
        public int fix_type;
        public int fsk_rssi;
        public float gps_accH;
        public boolean gps_pos_used;
        public int gps_status;
        public boolean gps_used;
        public int imu_status;
        public float latitude;
        public float longitude;
        public int motor_status;
        public float pitch;
        public int press_compass_status;
        public float roll;
        public int satellites_num;
        public float tas;
        public int vehicle_type;
        public float voltage;
        public float yaw;

        public String toString() {
            return new StringBuilder(String.valueOf(new SimpleDateFormat("yyyyMMdd HH:mm:ss:SSS").format(new Date()))).append(",").append(this.fsk_rssi).append(",").append(this.voltage).append(",").append(this.current).append(",").append(this.altitude).append(",").append(this.latitude).append(",").append(this.longitude).append(",").append(this.tas).append(",").append(this.gps_used).append(",").append(this.fix_type).append(",").append(this.satellites_num).append(",").append(this.roll).append(",").append(this.yaw).append(",").append(this.pitch).append(",").append(this.motor_status).append(",").append(this.imu_status).append(",").append(this.gps_status).append(",").append(this.cgps_status).append(",").append(this.press_compass_status).append(",").append(this.f_mode).append(",").append(this.gps_pos_used).append(",").append(this.vehicle_type).append(",").append(this.error_flags1).append(",").append(this.gps_accH).append("\n").toString();
        }

        public static String getParamsName() {
            return ",fsk_rssi,voltage,current,altitude,latitude,longitude,tas,gps_used,fix_type,satellites_num,roll,yaw,pitch,motor_status,imu_status,gps_status,cgps_used,press_compass_status,f_mode,gps_pos_used,vehicle_type,error_flags1,gps_accH\n";
        }
    }

    public static class TransmitterState extends UARTInfoMessage {
        public int status;
    }

    public static class Trim extends UARTInfoMessage {
        public float t1;
        public float t2;
        public float t3;
        public float t4;
    }

    public static class TxNeedUpdate extends UARTInfoMessage {
        public TxNeedUpdate() {
            this.what = 19;
        }
    }

    public static class UARTRelyMessage extends UARTInfoMessage {
        public static final int REPLY_AWAIT_INFO = 1061;
        public static final int REPLY_BIND_INFO = 1011;
        public static final int REPLY_CHANNEL_CALIBRATION_INFO = 1021;
        public static final int REPLY_CHANNEL_CURVE_INFO = 1024;
        public static final int REPLY_CHANNEL_MIXING_INFO = 1023;
        public static final int REPLY_CHANNEL_REVERSE_INFO = 1022;
        public static final int REPLY_CHANNEL_TRAVEL_INFO = 1025;
        public static final int REPLY_CHANNEL_TRIM_INFO = 1026;
        public static final int REPLY_EXECUTE_INFO = 1051;
        public static final int REPLY_JOIN_INFO = 1041;
        public static final int REPLY_SAVE_ALL_INFO = 1081;
        public static final int REPLY_SAVE_MIXING_INFO = 1083;
        public static final int REPLY_SAVE_TRAVEL_TRIM_INFO = 1082;
        public static final int REPLY_SIMULATOR_INFO = 1071;
        public static final int REPLY_TRAINER_QUIT_INFO = 1033;
        public static final int REPLY_TRAINER_STUDENT_INFO = 1032;
        public static final int REPLY_TRAINER_TEACHER_INFO = 1031;
        public int id;
        public boolean isRight;
    }

    public static class Version extends UARTInfoMessage {
        public String version;
    }

    public static class AWaitReply extends UARTRelyMessage {
        public AWaitReply() {
            this.what = UARTRelyMessage.REPLY_AWAIT_INFO;
        }
    }

    public static class Bind extends UARTRelyMessage {
        public Bind() {
            this.what = UARTRelyMessage.REPLY_BIND_INFO;
        }
    }

    public static class ChannelReplyOfCalibration extends UARTRelyMessage {
        public ChannelReplyOfCalibration() {
            this.what = UARTRelyMessage.REPLY_CHANNEL_CALIBRATION_INFO;
        }
    }

    public static class ChannelReplyOfCurve extends UARTRelyMessage {
        public ChannelReplyOfCurve() {
            this.what = 1024;
        }
    }

    public static class ChannelReplyOfMixing extends UARTRelyMessage {
        public ChannelReplyOfMixing() {
            this.what = UARTRelyMessage.REPLY_CHANNEL_MIXING_INFO;
        }
    }

    public static class ChannelReplyOfReverse extends UARTRelyMessage {
        public ChannelReplyOfReverse() {
            this.what = UARTRelyMessage.REPLY_CHANNEL_REVERSE_INFO;
        }
    }

    public static class ChannelReplyOfTravel extends UARTRelyMessage {
        public ChannelReplyOfTravel() {
            this.what = UARTRelyMessage.REPLY_CHANNEL_TRAVEL_INFO;
        }
    }

    public static class ChannelReplyOfTrim extends UARTRelyMessage {
        public ChannelReplyOfTrim() {
            this.what = UARTRelyMessage.REPLY_CHANNEL_TRIM_INFO;
        }
    }

    public static class ExecuteReply extends UARTRelyMessage {
        public ExecuteReply() {
            this.what = UARTRelyMessage.REPLY_EXECUTE_INFO;
        }
    }

    public static class JoinReply extends UARTRelyMessage {
        public JoinReply() {
            this.what = UARTRelyMessage.REPLY_JOIN_INFO;
        }
    }

    public static class SaveReplyOfAll extends UARTRelyMessage {
        public SaveReplyOfAll() {
            this.what = UARTRelyMessage.REPLY_SAVE_ALL_INFO;
        }
    }

    public static class SaveReplyOfMixing extends UARTRelyMessage {
        public SaveReplyOfMixing() {
            this.what = UARTRelyMessage.REPLY_SAVE_MIXING_INFO;
        }
    }

    public static class SaveReplyOfTravelTrim extends UARTRelyMessage {
        public SaveReplyOfTravelTrim() {
            this.what = UARTRelyMessage.REPLY_SAVE_TRAVEL_TRIM_INFO;
        }
    }

    public static class SimulatorReply extends UARTRelyMessage {
        public SimulatorReply() {
            this.what = UARTRelyMessage.REPLY_SIMULATOR_INFO;
        }
    }

    public static class TrainerReplyOfQuit extends UARTRelyMessage {
        public TrainerReplyOfQuit() {
            this.what = UARTRelyMessage.REPLY_TRAINER_QUIT_INFO;
        }
    }

    public static class TrainerReplyOfStudent extends UARTRelyMessage {
        public TrainerReplyOfStudent() {
            this.what = UARTRelyMessage.REPLY_TRAINER_STUDENT_INFO;
        }
    }

    public static class TrainerReplyOfTeacher extends UARTRelyMessage {
        public TrainerReplyOfTeacher() {
            this.what = UARTRelyMessage.REPLY_TRAINER_TEACHER_INFO;
        }
    }
}
