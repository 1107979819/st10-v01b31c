package com.yuneec.test;

import android.app.Activity;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.yuneec.flightmode15.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class GpsTestActivity extends Activity implements Listener, LocationListener {
    private static final String KEY_AVERAGE = "average";
    private static final String KEY_CURRENT = "current";
    private static final String KEY_INDEX = "index";
    private static final String KEY_PRN = "PRN";
    private static final String TAG = GpsTestActivity.class.getSimpleName();
    private SimpleAdapter listAdapter;
    private LocationManager mLocationManager;
    private long recordTimes = 0;
    private SparseArray<SatelliteRecordData> satelliteSemaphoreData;
    private ListView satellitesList;
    private ArrayList<HashMap<String, String>> satellitesListViewData;

    class SatelliteRecordData {
        public float averageData;
        public float currentData;
        public LinkedList<Float> recordData = new LinkedList();
        public long recordTimes = 0;

        SatelliteRecordData() {
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_test);
        LinearLayout satellitesListHeader = (LinearLayout) findViewById(R.id.header);
        ((TextView) satellitesListHeader.findViewById(R.id.row_item0)).setText(R.string.gps_satellites_index);
        ((TextView) satellitesListHeader.findViewById(R.id.row_item1)).setText(R.string.gps_satellites_number);
        ((TextView) satellitesListHeader.findViewById(R.id.row_item2)).setText(R.string.gps_satellites_current_semaphore);
        ((TextView) satellitesListHeader.findViewById(R.id.row_item3)).setText(R.string.gps_satellites_average_semaphore);
        this.satellitesList = (ListView) findViewById(R.id.satellites_list);
        this.mLocationManager = (LocationManager) getSystemService("location");
        this.satellitesListViewData = new ArrayList();
        this.satelliteSemaphoreData = new SparseArray();
        this.listAdapter = new SimpleAdapter(this, this.satellitesListViewData, R.layout.gps_test_list_row, new String[]{KEY_INDEX, KEY_PRN, KEY_CURRENT, KEY_AVERAGE}, new int[]{R.id.row_item0, R.id.row_item1, R.id.row_item2, R.id.row_item3});
        this.satellitesList.setAdapter(this.listAdapter);
    }

    protected void onResume() {
        super.onResume();
        this.mLocationManager.addGpsStatusListener(this);
        this.mLocationManager.requestLocationUpdates("gps", 0, 0.0f, this);
        onGpsStatusChanged(1);
    }

    protected void onPause() {
        super.onPause();
        this.mLocationManager.removeUpdates(this);
        this.mLocationManager.removeGpsStatusListener(this);
    }

    public void onGpsStatusChanged(int event) {
        GpsStatus satelliteStatus = this.mLocationManager.getGpsStatus(null);
        if (satelliteStatus != null) {
            this.recordTimes++;
            for (GpsSatellite oSat : satelliteStatus.getSatellites()) {
                if (oSat.usedInFix()) {
                    int prn = oSat.getPrn();
                    float snr = oSat.getSnr();
                    float averageSnr = snr;
                    Log.i(TAG, "prn: " + prn + ", snr: " + snr);
                    SatelliteRecordData previousSatelliteData = (SatelliteRecordData) this.satelliteSemaphoreData.get(prn);
                    if (previousSatelliteData == null) {
                        previousSatelliteData = new SatelliteRecordData();
                        previousSatelliteData.recordData.addLast(Float.valueOf(snr));
                        this.satelliteSemaphoreData.put(prn, previousSatelliteData);
                    } else {
                        if (previousSatelliteData.recordData.size() > 30) {
                            previousSatelliteData.recordData.removeFirst();
                        }
                        previousSatelliteData.recordData.addLast(Float.valueOf(snr));
                        float sumPreSnr = 0.0f;
                        Iterator it = previousSatelliteData.recordData.iterator();
                        while (it.hasNext()) {
                            sumPreSnr += ((Float) it.next()).floatValue();
                        }
                        averageSnr = sumPreSnr / ((float) previousSatelliteData.recordData.size());
                    }
                    previousSatelliteData.recordTimes = this.recordTimes;
                    previousSatelliteData.currentData = snr;
                    previousSatelliteData.averageData = averageSnr;
                }
            }
            this.satellitesListViewData.clear();
            for (int i = 0; i < this.satelliteSemaphoreData.size(); i++) {
                int key = this.satelliteSemaphoreData.keyAt(i);
                SatelliteRecordData satelliteRecordData = (SatelliteRecordData) this.satelliteSemaphoreData.get(key);
                HashMap<String, String> itemMap = new HashMap();
                itemMap.put(KEY_INDEX, String.valueOf(i + 1));
                itemMap.put(KEY_PRN, String.valueOf(key));
                itemMap.put(KEY_CURRENT, String.valueOf(satelliteRecordData.currentData));
                itemMap.put(KEY_AVERAGE, String.format("%.2f", new Object[]{Float.valueOf(satelliteRecordData.averageData)}));
                this.satellitesListViewData.add(itemMap);
                if (this.recordTimes - satelliteRecordData.recordTimes > 5) {
                    Log.i(TAG, "Remove previousSatelliteData");
                    this.satelliteSemaphoreData.remove(key);
                }
            }
            this.listAdapter.notifyDataSetChanged();
        }
    }

    protected void onDestroy() {
        this.satellitesListViewData.clear();
        this.satelliteSemaphoreData.clear();
        super.onDestroy();
    }

    public void onLocationChanged(Location location) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onProviderDisabled(String provider) {
    }
}
