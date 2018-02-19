package com.yuneec.mission;

import android.os.Environment;
import com.yuneec.uartcontroller.WaypointData;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SaveMissionData {
    private static final String CCC_MISSIONS_KEY = "ccc_missions";
    private static final String WAY_POINT_KEY = "way_points";
    private static final String WAY_POINT_NAME_KEY = "name";
    private static final String WP_ALTITUDE_KEY = "altitude";
    private static final String WP_GIMBAL_PITCH_KEY = "gimbalPitch";
    private static final String WP_GIMBAL_YAM_KEY = "gimbalYam";
    private static final String WP_INDEX_KEY = "pointerIndex";
    private static final String WP_LATITUDE_KEY = "latitude";
    private static final String WP_LONGITUDE_KEY = "longitude";
    private static final String WP_PITCH_KEY = "pitch";
    private static final String WP_ROLL_KEY = "roll";
    private static final String WP_YAW_KEY = "yaw";

    private static File getMissionDirectiory() {
        File missionDataDir = new File(new StringBuilder(String.valueOf(Environment.getExternalStorageDirectory().toString())).append("/flightmode").toString());
        missionDataDir.mkdirs();
        return new File(missionDataDir, "mission_data.json");
    }

    private static String loadMissionFromFile() {
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(getMissionDirectiory()));
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static void saveMissionToFile(String saveMissons) throws IOException {
        OutputStream is = new BufferedOutputStream(new FileOutputStream(getMissionDirectiory()));
        is.write(saveMissons.getBytes());
        is.close();
    }

    private static void writeWaypointsArray(JSONArray jsonArray, List<WaypointData> waypointDatas) throws IOException, JSONException {
        for (WaypointData waypointData : waypointDatas) {
            jsonArray.put(writeWaypoint(waypointData));
        }
    }

    private static JSONObject writeWaypoint(WaypointData waypointData) throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(WP_INDEX_KEY, waypointData.pointerIndex);
        jsonObject.put(WP_LATITUDE_KEY, (double) waypointData.latitude);
        jsonObject.put(WP_LONGITUDE_KEY, (double) waypointData.longitude);
        jsonObject.put(WP_ALTITUDE_KEY, (double) waypointData.altitude);
        jsonObject.put(WP_ROLL_KEY, (double) waypointData.roll);
        jsonObject.put(WP_PITCH_KEY, (double) waypointData.pitch);
        jsonObject.put(WP_YAW_KEY, (double) waypointData.yaw);
        jsonObject.put(WP_GIMBAL_PITCH_KEY, (double) waypointData.gimbalPitch);
        jsonObject.put(WP_GIMBAL_YAM_KEY, (double) waypointData.gimbalYam);
        return jsonObject;
    }

    private static WaypointData readWaypoint(JSONObject reader) throws IOException, JSONException {
        WaypointData waypointData = new WaypointData();
        waypointData.pointerIndex = reader.getInt(WP_INDEX_KEY);
        waypointData.latitude = (float) reader.getDouble(WP_LATITUDE_KEY);
        waypointData.longitude = (float) reader.getDouble(WP_LONGITUDE_KEY);
        waypointData.altitude = (float) reader.getDouble(WP_ALTITUDE_KEY);
        waypointData.roll = (float) reader.getDouble(WP_ROLL_KEY);
        waypointData.pitch = (float) reader.getDouble(WP_PITCH_KEY);
        waypointData.yaw = (float) reader.getDouble(WP_YAW_KEY);
        waypointData.gimbalPitch = (float) reader.getDouble(WP_GIMBAL_PITCH_KEY);
        waypointData.gimbalYam = (float) reader.getDouble(WP_GIMBAL_YAM_KEY);
        return waypointData;
    }

    private static void readWaypointArray(JSONArray wpArray, List<WaypointData> waypointDatas) throws IOException, JSONException {
        for (int i = 0; i < wpArray.length(); i++) {
            waypointDatas.add(readWaypoint((JSONObject) wpArray.get(i)));
        }
    }

    public static void saveCruveCableCam(String name, List<WaypointData> waypointDatas) {
        try {
            JSONObject obj;
            String missionString = loadMissionFromFile();
            JSONArray missionsNew = new JSONArray();
            if (missionString != null) {
                obj = new JSONObject(missionString);
                JSONArray missionsOrig = obj.getJSONArray(CCC_MISSIONS_KEY);
                for (int i = 0; i < missionsOrig.length(); i++) {
                    JSONObject objectItem = missionsOrig.getJSONObject(i);
                    if (!name.equals(objectItem.getString("name"))) {
                        missionsNew.put(objectItem);
                    }
                }
                obj.remove(CCC_MISSIONS_KEY);
            } else {
                obj = new JSONObject();
            }
            JSONArray jsonArray = new JSONArray();
            writeWaypointsArray(jsonArray, waypointDatas);
            JSONObject newNode = new JSONObject();
            newNode.put("name", name);
            newNode.put(WAY_POINT_KEY, jsonArray);
            missionsNew.put(newNode);
            obj.put(CCC_MISSIONS_KEY, missionsNew);
            saveMissionToFile(obj.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    public static void loadCruveCableCam(String name, List<WaypointData> waypointDatas) {
        String missionString = loadMissionFromFile();
        if (missionString != null) {
            try {
                JSONArray missionsOrig = new JSONObject(missionString).getJSONArray(CCC_MISSIONS_KEY);
                for (int i = 0; i < missionsOrig.length(); i++) {
                    JSONObject objectItem = missionsOrig.getJSONObject(i);
                    if (name.equals(objectItem.getString("name"))) {
                        readWaypointArray(objectItem.getJSONArray(WAY_POINT_KEY), waypointDatas);
                        return;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }
}
