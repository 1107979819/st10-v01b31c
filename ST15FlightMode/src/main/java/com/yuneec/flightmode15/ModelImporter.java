package com.yuneec.flightmode15;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;
import com.yuneec.database.DBOpenHelper;
import com.yuneec.database.DataProvider;
import com.yuneec.database.DataProviderHelper;
import com.yuneec.flight_settings.ChannelMap;
import com.yuneec.model_select.ModelSelectMain;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class ModelImporter implements OnScanCompletedListener {
    private static final String TAG = "ModelImporter";
    private boolean isMediaScanning;
    private Context mContext;
    private Uri mMediaUri;

    public ModelImporter(Context context) {
        this.mContext = context;
    }

    public int importModelFromAssets(String path) {
        IOException e;
        Throwable th;
        File dst;
        InputStream s_icon;
        FileOutputStream fos;
        FileOutputStream fileOutputStream;
        byte[] buffer;
        int count;
        long newId;
        InputStream s_thr_datas;
        AssetManager am = this.mContext.getAssets();
        try {
            Uri icon_uri;
            InputStream s_manifest = am.open(new StringBuilder(String.valueOf(path)).append("/manifest.csv").toString());
            HashMap<String, String> kv = new HashMap();
            BufferedReader br = null;
            try {
                BufferedReader br2 = new BufferedReader(new InputStreamReader(s_manifest));
                while (true) {
                    try {
                        String line = br2.readLine();
                        if (line == null) {
                            break;
                        }
                        String[] kvs = line.split(",");
                        if (kvs.length == 2) {
                            kv.put(kvs[0], kvs[1]);
                        }
                    } catch (IOException e2) {
                        e = e2;
                        br = br2;
                    } catch (Throwable th2) {
                        th = th2;
                        br = br2;
                    }
                }
                if (br2 != null) {
                    try {
                        br2.close();
                        br = br2;
                    } catch (IOException e3) {
                        br = br2;
                    }
                }
            } catch (IOException e4) {
                e = e4;
                try {
                    Log.e(TAG, "Parsing Manifest Error :" + e.getMessage());
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e5) {
                        }
                    }
                    dst = null;
                    s_icon = am.open(new StringBuilder(String.valueOf(path)).append("/icon.jpg").toString());
                    if (s_icon != null) {
                        dst = prepare();
                        fos = null;
                        try {
                            fileOutputStream = new FileOutputStream(dst);
                            try {
                                buffer = new byte[10240];
                                while (true) {
                                    count = s_icon.read(buffer);
                                    if (count == -1) {
                                        break;
                                    }
                                    fileOutputStream.write(buffer, 0, count);
                                }
                                if (fileOutputStream != null) {
                                    try {
                                        fileOutputStream.close();
                                    } catch (IOException e6) {
                                    }
                                }
                            } catch (IOException e7) {
                                e = e7;
                                fos = fileOutputStream;
                            } catch (Throwable th3) {
                                th = th3;
                                fos = fileOutputStream;
                            }
                        } catch (IOException e8) {
                            e = e8;
                            try {
                                Log.e(TAG, "Copy Icon failed :" + e.getMessage());
                                dst.delete();
                                dst = null;
                                if (fos != null) {
                                    try {
                                        fos.close();
                                    } catch (IOException e9) {
                                    }
                                }
                                icon_uri = dst == null ? null : Uri.fromFile(dst);
                                if (icon_uri == null) {
                                    newId = writeBriefToDB(kv, icon_uri);
                                } else {
                                    updateGallery(icon_uri);
                                    while (this.isMediaScanning) {
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException e10) {
                                        }
                                    }
                                    icon_uri = this.mMediaUri;
                                    this.mMediaUri = null;
                                    newId = writeBriefToDB(kv, icon_uri);
                                }
                                if (newId != -1) {
                                    return -1;
                                }
                                try {
                                    s_thr_datas = am.open(new StringBuilder(String.valueOf(path)).append("/analog.csv").toString());
                                    try {
                                        try {
                                            try {
                                                importMixingChannels(this.mContext, am.open(new StringBuilder(String.valueOf(path)).append("/channel_map.csv").toString()), am.open(new StringBuilder(String.valueOf(path)).append("/curves.csv").toString()), null, am.open(new StringBuilder(String.valueOf(path)).append("/switches.csv").toString()), newId);
                                                return Integer.parseInt((String) kv.get("type")) / 100;
                                            } catch (IOException e11) {
                                                Log.w(TAG, "s_analogs is not exist!");
                                                return -1;
                                            }
                                        } catch (IOException e12) {
                                            Log.w(TAG, "switches is not exist!");
                                            return -1;
                                        }
                                    } catch (IOException e13) {
                                        Log.w(TAG, "curves is not exist!");
                                        return -1;
                                    }
                                } catch (IOException e14) {
                                    Log.w(TAG, "s_analogs is not exist!");
                                    return -1;
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                if (fos != null) {
                                    try {
                                        fos.close();
                                    } catch (IOException e15) {
                                    }
                                }
                                throw th;
                            }
                        }
                    }
                    if (dst == null) {
                    }
                    if (icon_uri == null) {
                        updateGallery(icon_uri);
                        while (this.isMediaScanning) {
                            Thread.sleep(100);
                        }
                        icon_uri = this.mMediaUri;
                        this.mMediaUri = null;
                        newId = writeBriefToDB(kv, icon_uri);
                    } else {
                        newId = writeBriefToDB(kv, icon_uri);
                    }
                    if (newId != -1) {
                        return -1;
                    }
                    s_thr_datas = am.open(new StringBuilder(String.valueOf(path)).append("/analog.csv").toString());
                    importMixingChannels(this.mContext, am.open(new StringBuilder(String.valueOf(path)).append("/channel_map.csv").toString()), am.open(new StringBuilder(String.valueOf(path)).append("/curves.csv").toString()), null, am.open(new StringBuilder(String.valueOf(path)).append("/switches.csv").toString()), newId);
                    return Integer.parseInt((String) kv.get("type")) / 100;
                } catch (Throwable th5) {
                    th = th5;
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e16) {
                        }
                    }
                    throw th;
                }
            }
            dst = null;
            try {
                s_icon = am.open(new StringBuilder(String.valueOf(path)).append("/icon.jpg").toString());
                if (s_icon != null) {
                    dst = prepare();
                    fos = null;
                    fileOutputStream = new FileOutputStream(dst);
                    buffer = new byte[10240];
                    while (true) {
                        count = s_icon.read(buffer);
                        if (count == -1) {
                            break;
                        }
                        fileOutputStream.write(buffer, 0, count);
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                }
                if (dst == null) {
                }
                if (icon_uri == null) {
                    newId = writeBriefToDB(kv, icon_uri);
                } else {
                    updateGallery(icon_uri);
                    while (this.isMediaScanning) {
                        Thread.sleep(100);
                    }
                    icon_uri = this.mMediaUri;
                    this.mMediaUri = null;
                    newId = writeBriefToDB(kv, icon_uri);
                }
                if (newId != -1) {
                    return -1;
                }
                s_thr_datas = am.open(new StringBuilder(String.valueOf(path)).append("/analog.csv").toString());
                importMixingChannels(this.mContext, am.open(new StringBuilder(String.valueOf(path)).append("/channel_map.csv").toString()), am.open(new StringBuilder(String.valueOf(path)).append("/curves.csv").toString()), null, am.open(new StringBuilder(String.valueOf(path)).append("/switches.csv").toString()), newId);
                return Integer.parseInt((String) kv.get("type")) / 100;
            } catch (IOException e17) {
                return -1;
            }
        } catch (IOException e18) {
            Log.e(TAG, "import default data failed :" + e18.getMessage());
            return -1;
        }
    }

    public int importModelFromSD(String location, String floder) {
        IOException e;
        Throwable th;
        FileOutputStream fos;
        FileInputStream fis;
        FileOutputStream fos2;
        FileInputStream fis2;
        byte[] buffer;
        int count;
        Uri icon_uri;
        long newId;
        int type;
        File file = new File(new File(location, floder), "manifest.csv");
        if (file.exists()) {
            File dst;
            HashMap<String, String> kv = new HashMap();
            BufferedReader br = null;
            try {
                BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                while (true) {
                    try {
                        String line = br2.readLine();
                        if (line == null) {
                            break;
                        }
                        String[] kvs = line.split(",");
                        if (kvs.length == 2) {
                            kv.put(kvs[0], kvs[1]);
                        }
                    } catch (FileNotFoundException e2) {
                        br = br2;
                    } catch (IOException e3) {
                        e = e3;
                        br = br2;
                    } catch (Throwable th2) {
                        th = th2;
                        br = br2;
                    }
                }
                if (br2 != null) {
                    try {
                        br2.close();
                        br = br2;
                    } catch (IOException e4) {
                        br = br2;
                    }
                }
            } catch (FileNotFoundException e5) {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e6) {
                    }
                }
                file = new File(new File(location, floder), "icon.jpg");
                dst = null;
                if (file.exists()) {
                    dst = prepare();
                    fos = null;
                    fis = null;
                    try {
                        fos2 = new FileOutputStream(dst);
                        try {
                            fis2 = new FileInputStream(file);
                            try {
                                buffer = new byte[10240];
                                while (true) {
                                    count = fis2.read(buffer);
                                    if (count == -1) {
                                        break;
                                    }
                                    fos2.write(buffer, 0, count);
                                }
                                if (fis2 != null) {
                                    try {
                                        fis2.close();
                                    } catch (IOException e7) {
                                    }
                                }
                                if (fos2 != null) {
                                    fos2.close();
                                }
                            } catch (IOException e8) {
                                e = e8;
                                fis = fis2;
                                fos = fos2;
                            } catch (Throwable th3) {
                                th = th3;
                                fis = fis2;
                                fos = fos2;
                            }
                        } catch (IOException e9) {
                            e = e9;
                            fos = fos2;
                            try {
                                Log.e(TAG, "Copy Icon failed :" + e.getMessage());
                                dst.delete();
                                dst = null;
                                if (fis != null) {
                                    try {
                                        fis.close();
                                    } catch (IOException e10) {
                                    }
                                }
                                if (fos != null) {
                                    fos.close();
                                }
                                icon_uri = dst != null ? Uri.fromFile(dst) : null;
                                if (icon_uri != null) {
                                    updateGallery(icon_uri);
                                    while (this.isMediaScanning) {
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException e11) {
                                        }
                                    }
                                    icon_uri = this.mMediaUri;
                                    this.mMediaUri = null;
                                    newId = writeBriefToDB(kv, icon_uri);
                                } else {
                                    newId = writeBriefToDB(kv, icon_uri);
                                }
                                if (newId != -1) {
                                    return -1;
                                }
                                type = Integer.parseInt((String) kv.get("type")) / 100;
                                importMixingChannels(this.mContext, location, floder, newId);
                                return type;
                            } catch (Throwable th4) {
                                th = th4;
                                if (fis != null) {
                                    try {
                                        fis.close();
                                    } catch (IOException e12) {
                                        throw th;
                                    }
                                }
                                if (fos != null) {
                                    fos.close();
                                }
                                throw th;
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            fos = fos2;
                            if (fis != null) {
                                fis.close();
                            }
                            if (fos != null) {
                                fos.close();
                            }
                            throw th;
                        }
                    } catch (IOException e13) {
                        e = e13;
                        Log.e(TAG, "Copy Icon failed :" + e.getMessage());
                        dst.delete();
                        dst = null;
                        if (fis != null) {
                            fis.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                        if (dst != null) {
                        }
                        if (icon_uri != null) {
                            updateGallery(icon_uri);
                            while (this.isMediaScanning) {
                                Thread.sleep(100);
                            }
                            icon_uri = this.mMediaUri;
                            this.mMediaUri = null;
                            newId = writeBriefToDB(kv, icon_uri);
                        } else {
                            newId = writeBriefToDB(kv, icon_uri);
                        }
                        if (newId != -1) {
                            return -1;
                        }
                        type = Integer.parseInt((String) kv.get("type")) / 100;
                        importMixingChannels(this.mContext, location, floder, newId);
                        return type;
                    }
                }
                if (dst != null) {
                }
                if (icon_uri != null) {
                    newId = writeBriefToDB(kv, icon_uri);
                } else {
                    updateGallery(icon_uri);
                    while (this.isMediaScanning) {
                        Thread.sleep(100);
                    }
                    icon_uri = this.mMediaUri;
                    this.mMediaUri = null;
                    newId = writeBriefToDB(kv, icon_uri);
                }
                if (newId != -1) {
                    return -1;
                }
                type = Integer.parseInt((String) kv.get("type")) / 100;
                importMixingChannels(this.mContext, location, floder, newId);
                return type;
            } catch (IOException e14) {
                e = e14;
                try {
                    Log.e(TAG, "Parsing Manifest Error :" + e.getMessage());
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e15) {
                        }
                    }
                    return -1;
                } catch (Throwable th6) {
                    th = th6;
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e16) {
                        }
                    }
                    throw th;
                }
            }
            file = new File(new File(location, floder), "icon.jpg");
            dst = null;
            if (file.exists()) {
                dst = prepare();
                fos = null;
                fis = null;
                fos2 = new FileOutputStream(dst);
                fis2 = new FileInputStream(file);
                buffer = new byte[10240];
                while (true) {
                    count = fis2.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    fos2.write(buffer, 0, count);
                }
                if (fis2 != null) {
                    fis2.close();
                }
                if (fos2 != null) {
                    fos2.close();
                }
            }
            if (dst != null) {
            }
            if (icon_uri != null) {
                newId = writeBriefToDB(kv, icon_uri);
            } else {
                updateGallery(icon_uri);
                while (this.isMediaScanning) {
                    Thread.sleep(100);
                }
                icon_uri = this.mMediaUri;
                this.mMediaUri = null;
                newId = writeBriefToDB(kv, icon_uri);
            }
            if (newId != -1) {
                return -1;
            }
            type = Integer.parseInt((String) kv.get("type")) / 100;
            importMixingChannels(this.mContext, location, floder, newId);
            return type;
        }
        Log.e(TAG, file.getAbsolutePath() + " is not exist!");
        return -1;
    }

    private File prepare() {
        File dir;
        if (Utilities.hasSDCard(this.mContext)) {
            dir = new File(Utilities.getExterPath(), "ModelsIcon");
        } else {
            dir = new File(Environment.getExternalStorageDirectory(), "ModelsIcon");
        }
        if (!(dir.exists() || dir.mkdir())) {
            Log.e(TAG, "mkdir error dir:" + dir.getAbsolutePath());
        }
        Time now = new Time();
        now.setToNow();
        String filename = dir + "/" + "Icon-" + now.format("%Y-%m-%d %H.%M.%S");
        return checkVaild(filename, 1, new File(new StringBuilder(String.valueOf(filename)).append(ModelSelectMain.IMAGE_SUFFIX).toString()));
    }

    private File checkVaild(String oldname, int suffix_count, File file) {
        if (!file.exists()) {
            return file;
        }
        return checkVaild(oldname, suffix_count + 1, new File(new StringBuilder(String.valueOf(oldname)).append("-").append(suffix_count).append(ModelSelectMain.IMAGE_SUFFIX).toString()));
    }

    private long writeBriefToDB(HashMap<String, String> kv, Uri icon_uri) {
        ContentValues cv = new ContentValues();
        String value = (String) kv.get(DBOpenHelper.KEY_NAME);
        if (value == null) {
            Log.e(TAG, "model name missing, import abort!");
            return -1;
        }
        cv.put(DBOpenHelper.KEY_NAME, value);
        value = (String) kv.get("type");
        if (value == null) {
            Log.e(TAG, "model type missing, import abort!");
            return -1;
        }
        try {
            cv.put("type", Integer.valueOf(Integer.parseInt(value)));
            value = (String) kv.get(DBOpenHelper.KEY_FPV);
            if (value == null) {
                Log.e(TAG, "model fpv missing, import abort");
                return -1;
            }
            try {
                int fpv = Integer.parseInt(value);
                cv.put(DBOpenHelper.KEY_FPV, Integer.valueOf(fpv));
                value = (String) kv.get(DBOpenHelper.KEY_F_MODE_KEY);
                if (value == null) {
                    Log.e(TAG, "model fmode key missing, import abort");
                    return -1;
                }
                try {
                    int fmodekey = Integer.parseInt(value);
                    cv.put(DBOpenHelper.KEY_F_MODE_KEY, Integer.valueOf(fmodekey));
                    value = (String) kv.get(DBOpenHelper.KEY_ANALOG_MIN);
                    if (value == null) {
                        Log.e(TAG, "model minium analog missing, import abort");
                        return -1;
                    }
                    try {
                        int analog_min = Integer.parseInt(value);
                        cv.put(DBOpenHelper.KEY_ANALOG_MIN, Integer.valueOf(analog_min));
                        value = (String) kv.get(DBOpenHelper.KEY_RX_ANALOG_NUM);
                        if (value == null) {
                            Log.e(TAG, "model analog missing, import abort");
                            return -1;
                        }
                        try {
                            int analog = Integer.parseInt(value);
                            value = (String) kv.get(DBOpenHelper.KEY_SWITCH_MIN);
                            if (value == null) {
                                Log.e(TAG, "model minium switch missing, import abort");
                                return -1;
                            }
                            try {
                                int switch_min = Integer.parseInt(value);
                                cv.put(DBOpenHelper.KEY_SWITCH_MIN, Integer.valueOf(switch_min));
                                value = (String) kv.get(DBOpenHelper.KEY_RX_SWITCH_NUM);
                                if (value == null) {
                                    Log.e(TAG, "model switch missing, import abort");
                                    return -1;
                                }
                                try {
                                    int switches = Integer.parseInt(value);
                                    if (!(analog == 0 && switches == 0)) {
                                        cv.put(DBOpenHelper.KEY_RX_ANALOG_NUM, Integer.valueOf(analog));
                                        cv.put(DBOpenHelper.KEY_RX_SWITCH_NUM, Integer.valueOf(switches));
                                    }
                                    if (icon_uri == null) {
                                        Log.i(TAG, "model icon missing, skip import icon");
                                    } else {
                                        cv.put(DBOpenHelper.KEY_ICON, icon_uri.toString());
                                    }
                                    Uri insertUri = this.mContext.getContentResolver().insert(DataProvider.MODEL_URI, cv);
                                    if (insertUri == null) {
                                        return -1;
                                    }
                                    long newId = -1;
                                    try {
                                        newId = ContentUris.parseId(insertUri);
                                    } catch (NumberFormatException e) {
                                        Log.e(TAG, "Import encounter Bad Uri :" + insertUri);
                                    }
                                    if (newId == -1) {
                                        return newId;
                                    }
                                    value = (String) kv.get(DBOpenHelper.KEY_ALIAS);
                                    if (value == null) {
                                        Log.i(TAG, "channel alias missing");
                                        return newId;
                                    }
                                    String[] alias = value.split("&");
                                    for (int i = 0; i < alias.length; i++) {
                                        DataProviderHelper.setChannelAlias(this.mContext, newId, i + 1, alias[i]);
                                    }
                                    return newId;
                                } catch (NumberFormatException e2) {
                                    Log.e(TAG, "model switch error:" + value + ", import abort!");
                                    return -1;
                                }
                            } catch (NumberFormatException e3) {
                                Log.e(TAG, "model minium switch error:" + value + ", import abort!");
                                return -1;
                            }
                        } catch (NumberFormatException e4) {
                            Log.e(TAG, "model analog error:" + value + ", import abort!");
                            return -1;
                        }
                    } catch (NumberFormatException e5) {
                        Log.e(TAG, "model minium analog error:" + value + ", import abort!");
                        return -1;
                    }
                } catch (NumberFormatException e6) {
                    Log.e(TAG, "model fmode key error:" + value + ", import abort!");
                    return -1;
                }
            } catch (NumberFormatException e7) {
                Log.e(TAG, "model fpv error:" + value + ", import abort!");
                return -1;
            }
        } catch (NumberFormatException e8) {
            Log.e(TAG, "model type error:" + value + ", import abort!");
            return -1;
        }
    }

    private void updateGallery(Uri uri) {
        MediaScannerConnection.scanFile(this.mContext, new String[]{Uri.decode(uri.getPath())}, null, this);
        this.isMediaScanning = true;
    }

    public static boolean importMixingChannels(Context context, String location, String folder, long model_id) {
        FileInputStream s_channelmap;
        FileInputStream s_thr_datas;
        FileInputStream s_dr_datas;
        FileInputStream s_servos;
        File f_channelmap = new File(new File(location, folder), "channelmap.csv");
        try {
            s_channelmap = new FileInputStream(f_channelmap);
        } catch (FileNotFoundException e) {
            Log.w(TAG, f_channelmap.getAbsolutePath() + " is not exist!");
            s_channelmap = null;
        }
        File f_thr_datas = new File(new File(location, folder), "thr_datas.csv");
        try {
            s_thr_datas = new FileInputStream(f_thr_datas);
        } catch (FileNotFoundException e2) {
            Log.w(TAG, f_thr_datas.getAbsolutePath() + " is not exist!");
            s_thr_datas = null;
        }
        File f_dr_datas = new File(new File(location, folder), "dr_datas.csv");
        try {
            s_dr_datas = new FileInputStream(f_dr_datas);
        } catch (FileNotFoundException e3) {
            Log.w(TAG, f_dr_datas.getAbsolutePath() + " is not exist!");
            s_dr_datas = null;
        }
        File f_servos = new File(new File(location, folder), "servos.csv");
        try {
            s_servos = new FileInputStream(f_servos);
        } catch (FileNotFoundException e4) {
            Log.w(TAG, f_servos.getAbsolutePath() + " is not exist!");
            s_servos = null;
        }
        importMixingChannels(context, s_channelmap, s_thr_datas, s_dr_datas, s_servos, model_id);
        if (s_channelmap != null) {
            try {
                s_channelmap.close();
            } catch (IOException e5) {
            }
        }
        if (s_thr_datas != null) {
            s_thr_datas.close();
        }
        if (s_dr_datas != null) {
            s_dr_datas.close();
        }
        if (s_servos != null) {
            s_servos.close();
        }
        return true;
    }

    public static boolean importMixingChannels(Context context, InputStream cm_IS, InputStream td_IS, InputStream dr_IS, InputStream sd_IS, long model_id) {
        if (importChannelMap(context, cm_IS, model_id) && importThrDatas(context, td_IS, model_id) && importDRDatas(context, dr_IS, model_id) && importServos(context, sd_IS, model_id)) {
            return true;
        }
        return false;
    }

    private static boolean importChannelMap(Context context, InputStream ips, long model_id) {
        BufferedReader bufferedReader;
        boolean skip = false;
        if (ips == null) {
            skip = true;
        }
        if (!skip) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(ips));
                ChannelMap[] cm = DataProviderHelper.readChannelMapFromDatabase(context, model_id);
                int i = 0;
                while (true) {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    String[] fields = line.split(",");
                    if (fields.length != 3) {
                        Log.w(TAG, "fields corrputted len:" + fields.length + " content:" + line);
                    } else {
                        try {
                            cm[i].channel = Integer.parseInt(fields[0]);
                            try {
                                if (i < cm.length) {
                                    cm[i].function = fields[1];
                                    cm[i].hardware = fields[2];
                                    cm[i].alias = fields[3];
                                    i++;
                                } else {
                                    Log.e(TAG, "index >= cm_olde.length");
                                }
                            } catch (Exception e) {
                                e = e;
                                bufferedReader = br;
                            }
                        } catch (NumberFormatException e2) {
                            Log.w(TAG, "fields corrputted len:" + fields.length + " content:" + line);
                        }
                    }
                }
                DataProviderHelper.writeChannelMapFromDatabase(context, cm);
                bufferedReader = br;
            } catch (Exception e3) {
                Exception e4;
                e4 = e3;
                Log.e(TAG, "Parsing Mixing Params Error :" + e4.getMessage());
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean importThrDatas(android.content.Context r12, java.io.InputStream r13, long r14) {
        /*
        r6 = 0;
        r0 = 0;
        if (r13 != 0) goto L_0x0005;
    L_0x0004:
        r6 = 1;
    L_0x0005:
        if (r6 != 0) goto L_0x0031;
    L_0x0007:
        r1 = new java.io.BufferedReader;	 Catch:{ Exception -> 0x01bb }
        r9 = new java.io.InputStreamReader;	 Catch:{ Exception -> 0x01bb }
        r9.<init>(r13);	 Catch:{ Exception -> 0x01bb }
        r1.<init>(r9);	 Catch:{ Exception -> 0x01bb }
        r9 = 3;
        r8 = new com.yuneec.channelsettings.ThrottleData[r9];	 Catch:{ Exception -> 0x0068 }
        r4 = 0;
    L_0x0015:
        r9 = r8.length;	 Catch:{ Exception -> 0x0068 }
        if (r4 < r9) goto L_0x0033;
    L_0x0018:
        r5 = r1.readLine();	 Catch:{ Exception -> 0x0068 }
        if (r5 != 0) goto L_0x003d;
    L_0x001e:
        r9 = 0;
        r9 = r8[r9];	 Catch:{ Exception -> 0x0068 }
        com.yuneec.database.DataProviderHelper.writeThrDataToDatabase(r12, r9);	 Catch:{ Exception -> 0x0068 }
        r9 = 1;
        r9 = r8[r9];	 Catch:{ Exception -> 0x0068 }
        com.yuneec.database.DataProviderHelper.writeThrDataToDatabase(r12, r9);	 Catch:{ Exception -> 0x0068 }
        r9 = 2;
        r9 = r8[r9];	 Catch:{ Exception -> 0x0068 }
        com.yuneec.database.DataProviderHelper.writeThrDataToDatabase(r12, r9);	 Catch:{ Exception -> 0x0068 }
        r0 = r1;
    L_0x0031:
        r9 = 1;
    L_0x0032:
        return r9;
    L_0x0033:
        r9 = 0;
        r9 = com.yuneec.database.DataProviderHelper.readThrDataFromDatabase(r12, r14, r9);	 Catch:{ Exception -> 0x0068 }
        r8[r4] = r9;	 Catch:{ Exception -> 0x0068 }
        r4 = r4 + 1;
        goto L_0x0015;
    L_0x003d:
        r9 = ",";
        r3 = r5.split(r9);	 Catch:{ Exception -> 0x0068 }
        r9 = r3.length;	 Catch:{ Exception -> 0x0068 }
        r10 = 12;
        if (r9 == r10) goto L_0x0084;
    L_0x0048:
        r9 = "ModelImporter";
        r10 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0068 }
        r11 = "fields corrputted len:";
        r10.<init>(r11);	 Catch:{ Exception -> 0x0068 }
        r11 = r3.length;	 Catch:{ Exception -> 0x0068 }
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x0068 }
        r11 = " content:";
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x0068 }
        r10 = r10.append(r5);	 Catch:{ Exception -> 0x0068 }
        r10 = r10.toString();	 Catch:{ Exception -> 0x0068 }
        android.util.Log.w(r9, r10);	 Catch:{ Exception -> 0x0068 }
        goto L_0x0018;
    L_0x0068:
        r2 = move-exception;
        r0 = r1;
    L_0x006a:
        r9 = "ModelImporter";
        r10 = new java.lang.StringBuilder;
        r11 = "Parsing Mixing Params Error :";
        r10.<init>(r11);
        r11 = r2.getMessage();
        r10 = r10.append(r11);
        r10 = r10.toString();
        android.util.Log.e(r9, r10);
        r9 = 0;
        goto L_0x0032;
    L_0x0084:
        r4 = -1;
        r9 = 0;
        r9 = r3[r9];	 Catch:{ NumberFormatException -> 0x0152 }
        r4 = java.lang.Integer.parseInt(r9);	 Catch:{ NumberFormatException -> 0x0152 }
        r10 = r8[r4];	 Catch:{ Exception -> 0x0068 }
        r9 = 1;
        r9 = r3[r9];	 Catch:{ Exception -> 0x0068 }
        r11 = "true";
        r9 = r9.equals(r11);	 Catch:{ Exception -> 0x0068 }
        if (r9 == 0) goto L_0x0174;
    L_0x0099:
        r9 = 1;
    L_0x009a:
        r10.expo = r9;	 Catch:{ Exception -> 0x0068 }
        r9 = r8[r4];	 Catch:{ Exception -> 0x0068 }
        r10 = 2;
        r10 = r3[r10];	 Catch:{ Exception -> 0x0068 }
        r9.sw = r10;	 Catch:{ Exception -> 0x0068 }
        r9 = r8[r4];	 Catch:{ Exception -> 0x0068 }
        r10 = 3;
        r10 = r3[r10];	 Catch:{ Exception -> 0x0068 }
        r9.cut_sw = r10;	 Catch:{ Exception -> 0x0068 }
        r9 = r8[r4];	 Catch:{ Exception -> 0x0177 }
        r10 = 4;
        r10 = r3[r10];	 Catch:{ Exception -> 0x0177 }
        r10 = java.lang.Integer.parseInt(r10);	 Catch:{ Exception -> 0x0177 }
        r9.cut_value1 = r10;	 Catch:{ Exception -> 0x0177 }
        r9 = r8[r4];	 Catch:{ Exception -> 0x0177 }
        r10 = 5;
        r10 = r3[r10];	 Catch:{ Exception -> 0x0177 }
        r10 = java.lang.Integer.parseInt(r10);	 Catch:{ Exception -> 0x0177 }
        r9.cut_value2 = r10;	 Catch:{ Exception -> 0x0177 }
        r9 = 7;
        r9 = r3[r9];	 Catch:{ NumberFormatException -> 0x0199 }
        r7 = java.lang.Integer.parseInt(r9);	 Catch:{ NumberFormatException -> 0x0199 }
        r9 = r8[r4];	 Catch:{ Exception -> 0x0068 }
        r9 = r9.thrCurve;	 Catch:{ Exception -> 0x0068 }
        r9 = r9[r7];	 Catch:{ Exception -> 0x0068 }
        r9.sw_state = r7;	 Catch:{ Exception -> 0x0068 }
        r9 = r8[r4];	 Catch:{ Exception -> 0x0130 }
        r9 = r9.thrCurve;	 Catch:{ Exception -> 0x0130 }
        r9 = r9[r7];	 Catch:{ Exception -> 0x0130 }
        r9 = r9.curvePoints;	 Catch:{ Exception -> 0x0130 }
        r10 = 0;
        r11 = 8;
        r11 = r3[r11];	 Catch:{ Exception -> 0x0130 }
        r11 = java.lang.Float.parseFloat(r11);	 Catch:{ Exception -> 0x0130 }
        r9[r10] = r11;	 Catch:{ Exception -> 0x0130 }
        r9 = r8[r4];	 Catch:{ Exception -> 0x0130 }
        r9 = r9.thrCurve;	 Catch:{ Exception -> 0x0130 }
        r9 = r9[r7];	 Catch:{ Exception -> 0x0130 }
        r9 = r9.curvePoints;	 Catch:{ Exception -> 0x0130 }
        r10 = 0;
        r11 = 9;
        r11 = r3[r11];	 Catch:{ Exception -> 0x0130 }
        r11 = java.lang.Float.parseFloat(r11);	 Catch:{ Exception -> 0x0130 }
        r9[r10] = r11;	 Catch:{ Exception -> 0x0130 }
        r9 = r8[r4];	 Catch:{ Exception -> 0x0130 }
        r9 = r9.thrCurve;	 Catch:{ Exception -> 0x0130 }
        r9 = r9[r7];	 Catch:{ Exception -> 0x0130 }
        r9 = r9.curvePoints;	 Catch:{ Exception -> 0x0130 }
        r10 = 0;
        r11 = 10;
        r11 = r3[r11];	 Catch:{ Exception -> 0x0130 }
        r11 = java.lang.Float.parseFloat(r11);	 Catch:{ Exception -> 0x0130 }
        r9[r10] = r11;	 Catch:{ Exception -> 0x0130 }
        r9 = r8[r4];	 Catch:{ Exception -> 0x0130 }
        r9 = r9.thrCurve;	 Catch:{ Exception -> 0x0130 }
        r9 = r9[r7];	 Catch:{ Exception -> 0x0130 }
        r9 = r9.curvePoints;	 Catch:{ Exception -> 0x0130 }
        r10 = 0;
        r11 = 11;
        r11 = r3[r11];	 Catch:{ Exception -> 0x0130 }
        r11 = java.lang.Float.parseFloat(r11);	 Catch:{ Exception -> 0x0130 }
        r9[r10] = r11;	 Catch:{ Exception -> 0x0130 }
        r9 = r8[r4];	 Catch:{ Exception -> 0x0130 }
        r9 = r9.thrCurve;	 Catch:{ Exception -> 0x0130 }
        r9 = r9[r7];	 Catch:{ Exception -> 0x0130 }
        r9 = r9.curvePoints;	 Catch:{ Exception -> 0x0130 }
        r10 = 0;
        r11 = 12;
        r11 = r3[r11];	 Catch:{ Exception -> 0x0130 }
        r11 = java.lang.Float.parseFloat(r11);	 Catch:{ Exception -> 0x0130 }
        r9[r10] = r11;	 Catch:{ Exception -> 0x0130 }
        goto L_0x0018;
    L_0x0130:
        r2 = move-exception;
        r9 = "ModelImporter";
        r10 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0068 }
        r11 = "fields corrputted len:";
        r10.<init>(r11);	 Catch:{ Exception -> 0x0068 }
        r11 = r3.length;	 Catch:{ Exception -> 0x0068 }
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x0068 }
        r11 = " content:";
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x0068 }
        r10 = r10.append(r5);	 Catch:{ Exception -> 0x0068 }
        r10 = r10.toString();	 Catch:{ Exception -> 0x0068 }
        android.util.Log.w(r9, r10);	 Catch:{ Exception -> 0x0068 }
        goto L_0x0018;
    L_0x0152:
        r2 = move-exception;
        r9 = "ModelImporter";
        r10 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0068 }
        r11 = "fields corrputted len:";
        r10.<init>(r11);	 Catch:{ Exception -> 0x0068 }
        r11 = r3.length;	 Catch:{ Exception -> 0x0068 }
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x0068 }
        r11 = " content:";
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x0068 }
        r10 = r10.append(r5);	 Catch:{ Exception -> 0x0068 }
        r10 = r10.toString();	 Catch:{ Exception -> 0x0068 }
        android.util.Log.w(r9, r10);	 Catch:{ Exception -> 0x0068 }
        goto L_0x0018;
    L_0x0174:
        r9 = 0;
        goto L_0x009a;
    L_0x0177:
        r2 = move-exception;
        r9 = "ModelImporter";
        r10 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0068 }
        r11 = "fields corrputted len:";
        r10.<init>(r11);	 Catch:{ Exception -> 0x0068 }
        r11 = r3.length;	 Catch:{ Exception -> 0x0068 }
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x0068 }
        r11 = " content:";
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x0068 }
        r10 = r10.append(r5);	 Catch:{ Exception -> 0x0068 }
        r10 = r10.toString();	 Catch:{ Exception -> 0x0068 }
        android.util.Log.w(r9, r10);	 Catch:{ Exception -> 0x0068 }
        goto L_0x0018;
    L_0x0199:
        r2 = move-exception;
        r9 = "ModelImporter";
        r10 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0068 }
        r11 = "fields corrputted len:";
        r10.<init>(r11);	 Catch:{ Exception -> 0x0068 }
        r11 = r3.length;	 Catch:{ Exception -> 0x0068 }
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x0068 }
        r11 = " content:";
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x0068 }
        r10 = r10.append(r5);	 Catch:{ Exception -> 0x0068 }
        r10 = r10.toString();	 Catch:{ Exception -> 0x0068 }
        android.util.Log.w(r9, r10);	 Catch:{ Exception -> 0x0068 }
        goto L_0x0018;
    L_0x01bb:
        r2 = move-exception;
        goto L_0x006a;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.yuneec.flightmode15.ModelImporter.importThrDatas(android.content.Context, java.io.InputStream, long):boolean");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean importDRDatas(android.content.Context r18, java.io.InputStream r19, long r20) {
        /*
        r13 = 0;
        r4 = 0;
        if (r19 != 0) goto L_0x0005;
    L_0x0004:
        r13 = 1;
    L_0x0005:
        if (r13 != 0) goto L_0x004a;
    L_0x0007:
        r5 = new java.io.BufferedReader;	 Catch:{ Exception -> 0x01cc }
        r15 = new java.io.InputStreamReader;	 Catch:{ Exception -> 0x01cc }
        r0 = r19;
        r15.<init>(r0);	 Catch:{ Exception -> 0x01cc }
        r5.<init>(r15);	 Catch:{ Exception -> 0x01cc }
        r3 = new java.util.ArrayList;	 Catch:{ Exception -> 0x008b }
        r3.<init>();	 Catch:{ Exception -> 0x008b }
        r15 = 3;
        r6 = new com.yuneec.channelsettings.DRData[r15];	 Catch:{ Exception -> 0x008b }
        r9 = 0;
    L_0x001c:
        r15 = 3;
        if (r9 < r15) goto L_0x004c;
    L_0x001f:
        r12 = r5.readLine();	 Catch:{ Exception -> 0x008b }
        if (r12 != 0) goto L_0x005a;
    L_0x0025:
        r15 = 0;
        r15 = r3.get(r15);	 Catch:{ Exception -> 0x008b }
        r15 = (com.yuneec.channelsettings.DRData[]) r15;	 Catch:{ Exception -> 0x008b }
        r0 = r18;
        com.yuneec.database.DataProviderHelper.writeDRDataToDatabase(r0, r15);	 Catch:{ Exception -> 0x008b }
        r15 = 1;
        r15 = r3.get(r15);	 Catch:{ Exception -> 0x008b }
        r15 = (com.yuneec.channelsettings.DRData[]) r15;	 Catch:{ Exception -> 0x008b }
        r0 = r18;
        com.yuneec.database.DataProviderHelper.writeDRDataToDatabase(r0, r15);	 Catch:{ Exception -> 0x008b }
        r15 = 2;
        r15 = r3.get(r15);	 Catch:{ Exception -> 0x008b }
        r15 = (com.yuneec.channelsettings.DRData[]) r15;	 Catch:{ Exception -> 0x008b }
        r0 = r18;
        com.yuneec.database.DataProviderHelper.writeDRDataToDatabase(r0, r15);	 Catch:{ Exception -> 0x008b }
        r4 = r5;
    L_0x004a:
        r15 = 1;
    L_0x004b:
        return r15;
    L_0x004c:
        r0 = r18;
        r1 = r20;
        r6 = com.yuneec.database.DataProviderHelper.readDRDataFromDatabase(r0, r1, r9);	 Catch:{ Exception -> 0x008b }
        r3.add(r9, r6);	 Catch:{ Exception -> 0x008b }
        r9 = r9 + 1;
        goto L_0x001c;
    L_0x005a:
        r15 = ",";
        r8 = r12.split(r15);	 Catch:{ Exception -> 0x008b }
        r15 = r8.length;	 Catch:{ Exception -> 0x008b }
        r16 = 9;
        r0 = r16;
        if (r15 == r0) goto L_0x00a7;
    L_0x0067:
        r15 = "ModelImporter";
        r16 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x008b }
        r17 = "fields corrputted len:";
        r16.<init>(r17);	 Catch:{ Exception -> 0x008b }
        r0 = r8.length;	 Catch:{ Exception -> 0x008b }
        r17 = r0;
        r16 = r16.append(r17);	 Catch:{ Exception -> 0x008b }
        r17 = " content:";
        r16 = r16.append(r17);	 Catch:{ Exception -> 0x008b }
        r0 = r16;
        r16 = r0.append(r12);	 Catch:{ Exception -> 0x008b }
        r16 = r16.toString();	 Catch:{ Exception -> 0x008b }
        android.util.Log.w(r15, r16);	 Catch:{ Exception -> 0x008b }
        goto L_0x001f;
    L_0x008b:
        r7 = move-exception;
        r4 = r5;
    L_0x008d:
        r15 = "ModelImporter";
        r16 = new java.lang.StringBuilder;
        r17 = "Parsing Mixing Params Error :";
        r16.<init>(r17);
        r17 = r7.getMessage();
        r16 = r16.append(r17);
        r16 = r16.toString();
        android.util.Log.e(r15, r16);
        r15 = 0;
        goto L_0x004b;
    L_0x00a7:
        r9 = -1;
        r15 = 0;
        r15 = r8[r15];	 Catch:{ NumberFormatException -> 0x0161 }
        r9 = java.lang.Integer.parseInt(r15);	 Catch:{ NumberFormatException -> 0x0161 }
        r6 = r3.get(r9);	 Catch:{ Exception -> 0x008b }
        r6 = (com.yuneec.channelsettings.DRData[]) r6;	 Catch:{ Exception -> 0x008b }
        r15 = 1;
        r10 = r8[r15];	 Catch:{ Exception -> 0x008b }
        r11 = -1;
        r15 = "ail";
        r15 = r10.equals(r15);	 Catch:{ Exception -> 0x008b }
        if (r15 == 0) goto L_0x0187;
    L_0x00c1:
        r11 = 0;
    L_0x00c2:
        r15 = r6[r11];	 Catch:{ Exception -> 0x008b }
        r15.func = r10;	 Catch:{ Exception -> 0x008b }
        r15 = r6[r11];	 Catch:{ Exception -> 0x008b }
        r16 = 2;
        r16 = r8[r16];	 Catch:{ Exception -> 0x008b }
        r0 = r16;
        r15.sw = r0;	 Catch:{ Exception -> 0x008b }
        r15 = 3;
        r15 = r8[r15];	 Catch:{ NumberFormatException -> 0x01a6 }
        r14 = java.lang.Integer.parseInt(r15);	 Catch:{ NumberFormatException -> 0x01a6 }
        r15 = r6[r11];	 Catch:{ Exception -> 0x008b }
        r15 = r15.curveparams;	 Catch:{ Exception -> 0x008b }
        r15 = r15[r14];	 Catch:{ Exception -> 0x008b }
        r15.sw_state = r14;	 Catch:{ Exception -> 0x008b }
        r15 = r6[r11];	 Catch:{ Exception -> 0x013b }
        r15 = r15.curveparams;	 Catch:{ Exception -> 0x013b }
        r15 = r15[r14];	 Catch:{ Exception -> 0x013b }
        r16 = 4;
        r16 = r8[r16];	 Catch:{ Exception -> 0x013b }
        r16 = java.lang.Float.parseFloat(r16);	 Catch:{ Exception -> 0x013b }
        r0 = r16;
        r15.rate1 = r0;	 Catch:{ Exception -> 0x013b }
        r15 = r6[r11];	 Catch:{ Exception -> 0x013b }
        r15 = r15.curveparams;	 Catch:{ Exception -> 0x013b }
        r15 = r15[r14];	 Catch:{ Exception -> 0x013b }
        r16 = 5;
        r16 = r8[r16];	 Catch:{ Exception -> 0x013b }
        r16 = java.lang.Float.parseFloat(r16);	 Catch:{ Exception -> 0x013b }
        r0 = r16;
        r15.rate2 = r0;	 Catch:{ Exception -> 0x013b }
        r15 = r6[r11];	 Catch:{ Exception -> 0x013b }
        r15 = r15.curveparams;	 Catch:{ Exception -> 0x013b }
        r15 = r15[r14];	 Catch:{ Exception -> 0x013b }
        r16 = 6;
        r16 = r8[r16];	 Catch:{ Exception -> 0x013b }
        r16 = java.lang.Float.parseFloat(r16);	 Catch:{ Exception -> 0x013b }
        r0 = r16;
        r15.expo1 = r0;	 Catch:{ Exception -> 0x013b }
        r15 = r6[r11];	 Catch:{ Exception -> 0x013b }
        r15 = r15.curveparams;	 Catch:{ Exception -> 0x013b }
        r15 = r15[r14];	 Catch:{ Exception -> 0x013b }
        r16 = 7;
        r16 = r8[r16];	 Catch:{ Exception -> 0x013b }
        r16 = java.lang.Float.parseFloat(r16);	 Catch:{ Exception -> 0x013b }
        r0 = r16;
        r15.expo2 = r0;	 Catch:{ Exception -> 0x013b }
        r15 = r6[r11];	 Catch:{ Exception -> 0x013b }
        r15 = r15.curveparams;	 Catch:{ Exception -> 0x013b }
        r15 = r15[r14];	 Catch:{ Exception -> 0x013b }
        r16 = 8;
        r16 = r8[r16];	 Catch:{ Exception -> 0x013b }
        r16 = java.lang.Float.parseFloat(r16);	 Catch:{ Exception -> 0x013b }
        r0 = r16;
        r15.offset = r0;	 Catch:{ Exception -> 0x013b }
        goto L_0x001f;
    L_0x013b:
        r7 = move-exception;
        r15 = "ModelImporter";
        r16 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x008b }
        r17 = "fields corrputted len:";
        r16.<init>(r17);	 Catch:{ Exception -> 0x008b }
        r0 = r8.length;	 Catch:{ Exception -> 0x008b }
        r17 = r0;
        r16 = r16.append(r17);	 Catch:{ Exception -> 0x008b }
        r17 = " content:";
        r16 = r16.append(r17);	 Catch:{ Exception -> 0x008b }
        r0 = r16;
        r16 = r0.append(r12);	 Catch:{ Exception -> 0x008b }
        r16 = r16.toString();	 Catch:{ Exception -> 0x008b }
        android.util.Log.w(r15, r16);	 Catch:{ Exception -> 0x008b }
        goto L_0x001f;
    L_0x0161:
        r7 = move-exception;
        r15 = "ModelImporter";
        r16 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x008b }
        r17 = "fields corrputted len:";
        r16.<init>(r17);	 Catch:{ Exception -> 0x008b }
        r0 = r8.length;	 Catch:{ Exception -> 0x008b }
        r17 = r0;
        r16 = r16.append(r17);	 Catch:{ Exception -> 0x008b }
        r17 = " content:";
        r16 = r16.append(r17);	 Catch:{ Exception -> 0x008b }
        r0 = r16;
        r16 = r0.append(r12);	 Catch:{ Exception -> 0x008b }
        r16 = r16.toString();	 Catch:{ Exception -> 0x008b }
        android.util.Log.w(r15, r16);	 Catch:{ Exception -> 0x008b }
        goto L_0x001f;
    L_0x0187:
        r15 = "ele";
        r15 = r10.equals(r15);	 Catch:{ Exception -> 0x008b }
        if (r15 == 0) goto L_0x0192;
    L_0x018f:
        r11 = 1;
        goto L_0x00c2;
    L_0x0192:
        r15 = "rud";
        r15 = r10.equals(r15);	 Catch:{ Exception -> 0x008b }
        if (r15 == 0) goto L_0x019d;
    L_0x019a:
        r11 = 2;
        goto L_0x00c2;
    L_0x019d:
        r15 = "ModelImporter";
        r16 = "Invalid function";
        android.util.Log.e(r15, r16);	 Catch:{ Exception -> 0x008b }
        goto L_0x00c2;
    L_0x01a6:
        r7 = move-exception;
        r15 = "ModelImporter";
        r16 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x008b }
        r17 = "fields corrputted len:";
        r16.<init>(r17);	 Catch:{ Exception -> 0x008b }
        r0 = r8.length;	 Catch:{ Exception -> 0x008b }
        r17 = r0;
        r16 = r16.append(r17);	 Catch:{ Exception -> 0x008b }
        r17 = " content:";
        r16 = r16.append(r17);	 Catch:{ Exception -> 0x008b }
        r0 = r16;
        r16 = r0.append(r12);	 Catch:{ Exception -> 0x008b }
        r16 = r16.toString();	 Catch:{ Exception -> 0x008b }
        android.util.Log.w(r15, r16);	 Catch:{ Exception -> 0x008b }
        goto L_0x001f;
    L_0x01cc:
        r7 = move-exception;
        goto L_0x008d;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.yuneec.flightmode15.ModelImporter.importDRDatas(android.content.Context, java.io.InputStream, long):boolean");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean importServos(android.content.Context r18, java.io.InputStream r19, long r20) {
        /*
        r14 = 0;
        r4 = 0;
        if (r19 != 0) goto L_0x0005;
    L_0x0004:
        r14 = 1;
    L_0x0005:
        if (r14 != 0) goto L_0x0055;
    L_0x0007:
        r5 = new java.io.BufferedReader;	 Catch:{ Exception -> 0x016c }
        r15 = new java.io.InputStreamReader;	 Catch:{ Exception -> 0x016c }
        r0 = r19;
        r15.<init>(r0);	 Catch:{ Exception -> 0x016c }
        r5.<init>(r15);	 Catch:{ Exception -> 0x016c }
        r15 = r18.getResources();	 Catch:{ Exception -> 0x0096 }
        r16 = 2131361797; // 0x7f0a0005 float:1.8343356E38 double:1.053032643E-314;
        r10 = r15.getStringArray(r16);	 Catch:{ Exception -> 0x0096 }
        r15 = r10.length;	 Catch:{ Exception -> 0x0096 }
        r13 = new com.yuneec.channelsettings.ServoData[r15];	 Catch:{ Exception -> 0x0096 }
        r3 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0096 }
        r3.<init>();	 Catch:{ Exception -> 0x0096 }
        r8 = 0;
    L_0x0027:
        r15 = 3;
        if (r8 < r15) goto L_0x0057;
    L_0x002a:
        r12 = r5.readLine();	 Catch:{ Exception -> 0x0096 }
        if (r12 != 0) goto L_0x0065;
    L_0x0030:
        r15 = 0;
        r15 = r3.get(r15);	 Catch:{ Exception -> 0x0096 }
        r15 = (com.yuneec.channelsettings.ServoData[]) r15;	 Catch:{ Exception -> 0x0096 }
        r0 = r18;
        com.yuneec.database.DataProviderHelper.writeServoDataToDatabase(r0, r15);	 Catch:{ Exception -> 0x0096 }
        r15 = 1;
        r15 = r3.get(r15);	 Catch:{ Exception -> 0x0096 }
        r15 = (com.yuneec.channelsettings.ServoData[]) r15;	 Catch:{ Exception -> 0x0096 }
        r0 = r18;
        com.yuneec.database.DataProviderHelper.writeServoDataToDatabase(r0, r15);	 Catch:{ Exception -> 0x0096 }
        r15 = 2;
        r15 = r3.get(r15);	 Catch:{ Exception -> 0x0096 }
        r15 = (com.yuneec.channelsettings.ServoData[]) r15;	 Catch:{ Exception -> 0x0096 }
        r0 = r18;
        com.yuneec.database.DataProviderHelper.writeServoDataToDatabase(r0, r15);	 Catch:{ Exception -> 0x0096 }
        r4 = r5;
    L_0x0055:
        r15 = 1;
    L_0x0056:
        return r15;
    L_0x0057:
        r0 = r18;
        r1 = r20;
        r13 = com.yuneec.database.DataProviderHelper.readServoDataFromDatabase(r0, r1, r8);	 Catch:{ Exception -> 0x0096 }
        r3.add(r8, r13);	 Catch:{ Exception -> 0x0096 }
        r8 = r8 + 1;
        goto L_0x0027;
    L_0x0065:
        r15 = ",";
        r7 = r12.split(r15);	 Catch:{ Exception -> 0x0096 }
        r15 = r7.length;	 Catch:{ Exception -> 0x0096 }
        r16 = 7;
        r0 = r16;
        if (r15 == r0) goto L_0x00b2;
    L_0x0072:
        r15 = "ModelImporter";
        r16 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0096 }
        r17 = "fields corrputted len:";
        r16.<init>(r17);	 Catch:{ Exception -> 0x0096 }
        r0 = r7.length;	 Catch:{ Exception -> 0x0096 }
        r17 = r0;
        r16 = r16.append(r17);	 Catch:{ Exception -> 0x0096 }
        r17 = " content:";
        r16 = r16.append(r17);	 Catch:{ Exception -> 0x0096 }
        r0 = r16;
        r16 = r0.append(r12);	 Catch:{ Exception -> 0x0096 }
        r16 = r16.toString();	 Catch:{ Exception -> 0x0096 }
        android.util.Log.w(r15, r16);	 Catch:{ Exception -> 0x0096 }
        goto L_0x002a;
    L_0x0096:
        r6 = move-exception;
        r4 = r5;
    L_0x0098:
        r15 = "ModelImporter";
        r16 = new java.lang.StringBuilder;
        r17 = "Parsing Mixing Params Error :";
        r16.<init>(r17);
        r17 = r6.getMessage();
        r16 = r16.append(r17);
        r16 = r16.toString();
        android.util.Log.e(r15, r16);
        r15 = 0;
        goto L_0x0056;
    L_0x00b2:
        r8 = -1;
        r15 = 0;
        r15 = r7[r15];	 Catch:{ NumberFormatException -> 0x0143 }
        r8 = java.lang.Integer.parseInt(r15);	 Catch:{ NumberFormatException -> 0x0143 }
        r13 = r3.get(r8);	 Catch:{ Exception -> 0x0096 }
        r13 = (com.yuneec.channelsettings.ServoData[]) r13;	 Catch:{ Exception -> 0x0096 }
        r15 = 1;
        r9 = r7[r15];	 Catch:{ Exception -> 0x0096 }
        r15 = java.util.Arrays.asList(r10);	 Catch:{ Exception -> 0x0096 }
        r11 = r15.indexOf(r9);	 Catch:{ Exception -> 0x0096 }
        r15 = r13[r11];	 Catch:{ Exception -> 0x0096 }
        r15.func = r9;	 Catch:{ Exception -> 0x0096 }
        r16 = r13[r11];	 Catch:{ Exception -> 0x0096 }
        r15 = 3;
        r15 = r7[r15];	 Catch:{ Exception -> 0x0096 }
        r17 = "true";
        r0 = r17;
        r15 = r15.equals(r0);	 Catch:{ Exception -> 0x0096 }
        if (r15 == 0) goto L_0x0169;
    L_0x00de:
        r15 = 1;
    L_0x00df:
        r0 = r16;
        r0.reverse = r15;	 Catch:{ Exception -> 0x0096 }
        r15 = r13[r11];	 Catch:{ NumberFormatException -> 0x011d }
        r16 = 2;
        r16 = r7[r16];	 Catch:{ NumberFormatException -> 0x011d }
        r16 = java.lang.Integer.parseInt(r16);	 Catch:{ NumberFormatException -> 0x011d }
        r0 = r16;
        r15.subTrim = r0;	 Catch:{ NumberFormatException -> 0x011d }
        r15 = r13[r11];	 Catch:{ NumberFormatException -> 0x011d }
        r16 = 4;
        r16 = r7[r16];	 Catch:{ NumberFormatException -> 0x011d }
        r16 = java.lang.Integer.parseInt(r16);	 Catch:{ NumberFormatException -> 0x011d }
        r0 = r16;
        r15.speed = r0;	 Catch:{ NumberFormatException -> 0x011d }
        r15 = r13[r11];	 Catch:{ NumberFormatException -> 0x011d }
        r16 = 5;
        r16 = r7[r16];	 Catch:{ NumberFormatException -> 0x011d }
        r16 = java.lang.Integer.parseInt(r16);	 Catch:{ NumberFormatException -> 0x011d }
        r0 = r16;
        r15.travelL = r0;	 Catch:{ NumberFormatException -> 0x011d }
        r15 = r13[r11];	 Catch:{ NumberFormatException -> 0x011d }
        r16 = 6;
        r16 = r7[r16];	 Catch:{ NumberFormatException -> 0x011d }
        r16 = java.lang.Integer.parseInt(r16);	 Catch:{ NumberFormatException -> 0x011d }
        r0 = r16;
        r15.travelR = r0;	 Catch:{ NumberFormatException -> 0x011d }
        goto L_0x002a;
    L_0x011d:
        r6 = move-exception;
        r15 = "ModelImporter";
        r16 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0096 }
        r17 = "fields corrputted len:";
        r16.<init>(r17);	 Catch:{ Exception -> 0x0096 }
        r0 = r7.length;	 Catch:{ Exception -> 0x0096 }
        r17 = r0;
        r16 = r16.append(r17);	 Catch:{ Exception -> 0x0096 }
        r17 = " content:";
        r16 = r16.append(r17);	 Catch:{ Exception -> 0x0096 }
        r0 = r16;
        r16 = r0.append(r12);	 Catch:{ Exception -> 0x0096 }
        r16 = r16.toString();	 Catch:{ Exception -> 0x0096 }
        android.util.Log.w(r15, r16);	 Catch:{ Exception -> 0x0096 }
        goto L_0x002a;
    L_0x0143:
        r6 = move-exception;
        r15 = "ModelImporter";
        r16 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0096 }
        r17 = "fields corrputted len:";
        r16.<init>(r17);	 Catch:{ Exception -> 0x0096 }
        r0 = r7.length;	 Catch:{ Exception -> 0x0096 }
        r17 = r0;
        r16 = r16.append(r17);	 Catch:{ Exception -> 0x0096 }
        r17 = " content:";
        r16 = r16.append(r17);	 Catch:{ Exception -> 0x0096 }
        r0 = r16;
        r16 = r0.append(r12);	 Catch:{ Exception -> 0x0096 }
        r16 = r16.toString();	 Catch:{ Exception -> 0x0096 }
        android.util.Log.w(r15, r16);	 Catch:{ Exception -> 0x0096 }
        goto L_0x002a;
    L_0x0169:
        r15 = 0;
        goto L_0x00df;
    L_0x016c:
        r6 = move-exception;
        goto L_0x0098;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.yuneec.flightmode15.ModelImporter.importServos(android.content.Context, java.io.InputStream, long):boolean");
    }

    private static void buildContentValue(ContentValues cv, String field, String value) {
        if (!value.equals("null")) {
            cv.put(field, value);
        }
    }

    public void onScanCompleted(String path, Uri uri) {
        this.isMediaScanning = false;
        this.mMediaUri = uri;
    }
}
