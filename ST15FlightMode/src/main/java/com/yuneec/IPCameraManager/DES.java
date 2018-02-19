package com.yuneec.IPCameraManager;

import android.support.v4.view.MotionEventCompat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class DES {
    private static final String key = "ksYuN2eC";

    public static void decrypt(String srcFile, String distFile) throws Exception {
        OutputStream out;
        Exception e;
        Throwable th;
        InputStream is = null;
        OutputStream out2 = null;
        CipherOutputStream cos = null;
        try {
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(2, SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(key.getBytes("UTF-8"))), new IvParameterSpec(key.getBytes("UTF-8")));
            byte[] buffer = new byte[1024];
            InputStream is2 = new FileInputStream(srcFile);
            try {
                File f = new File(distFile);
                if (!f.exists()) {
                    f.createNewFile();
                }
                out = new FileOutputStream(distFile);
            } catch (Exception e2) {
                e = e2;
                is = is2;
                try {
                    throw e;
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (Throwable th3) {
                th = th3;
                is = is2;
                cos.close();
                is.close();
                out2.close();
                throw th;
            }
            try {
                CipherOutputStream cos2 = new CipherOutputStream(out, cipher);
                while (true) {
                    try {
                        int r = is2.read(buffer);
                        if (r < 0) {
                            cos2.close();
                            is2.close();
                            out.close();
                            return;
                        }
                        cos2.write(buffer, 0, r);
                    } catch (Exception e3) {
                        e = e3;
                        cos = cos2;
                        out2 = out;
                        is = is2;
                    } catch (Throwable th4) {
                        th = th4;
                        cos = cos2;
                        out2 = out;
                        is = is2;
                    }
                }
            } catch (Exception e4) {
                e = e4;
                out2 = out;
                is = is2;
                throw e;
            } catch (Throwable th5) {
                th = th5;
                out2 = out;
                is = is2;
                cos.close();
                is.close();
                out2.close();
                throw th;
            }
        } catch (Exception e5) {
            e = e5;
            throw e;
        }
    }

    public static void encrypt(String srcFile, String distFile) throws Exception {
        Exception e;
        Throwable th;
        InputStream is = null;
        OutputStream out = null;
        CipherInputStream cis = null;
        try {
            OutputStream out2;
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(1, SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(key.getBytes("UTF-8"))), new IvParameterSpec(key.getBytes("UTF-8")));
            InputStream is2 = new FileInputStream(srcFile);
            try {
                File f = new File(distFile);
                if (!f.exists()) {
                    f.createNewFile();
                }
                out2 = new FileOutputStream(distFile);
            } catch (Exception e2) {
                e = e2;
                is = is2;
                try {
                    throw e;
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (Throwable th3) {
                th = th3;
                is = is2;
                cis.close();
                is.close();
                out.close();
                throw th;
            }
            try {
                CipherInputStream cis2 = new CipherInputStream(is2, cipher);
                try {
                    byte[] buffer = new byte[1024];
                    while (true) {
                        int r = cis2.read(buffer);
                        if (r <= 0) {
                            cis2.close();
                            is2.close();
                            out2.close();
                            return;
                        }
                        out2.write(buffer, 0, r);
                    }
                } catch (Exception e3) {
                    e = e3;
                    cis = cis2;
                    out = out2;
                    is = is2;
                } catch (Throwable th4) {
                    th = th4;
                    cis = cis2;
                    out = out2;
                    is = is2;
                }
            } catch (Exception e4) {
                e = e4;
                out = out2;
                is = is2;
                throw e;
            } catch (Throwable th5) {
                th = th5;
                out = out2;
                is = is2;
                cis.close();
                is.close();
                out.close();
                throw th;
            }
        } catch (Exception e5) {
            e = e5;
            throw e;
        }
    }

    public static String decrypt(String message) throws Exception {
        byte[] bytesrc = convertHexString(message);
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        cipher.init(2, SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(key.getBytes("UTF-8"))), new IvParameterSpec(key.getBytes("UTF-8")));
        return new String(cipher.doFinal(bytesrc));
    }

    public static byte[] encrypt(String message) throws Exception {
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        cipher.init(1, SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(key.getBytes("UTF-8"))), new IvParameterSpec(key.getBytes("UTF-8")));
        return cipher.doFinal(message.getBytes("UTF-8"));
    }

    public static byte[] convertHexString(String ss) {
        byte[] digest = new byte[(ss.length() / 2)];
        for (int i = 0; i < digest.length; i++) {
            digest[i] = (byte) Integer.parseInt(ss.substring(i * 2, (i * 2) + 2), 16);
        }
        return digest;
    }

    public static String toHexString(byte[] b) {
        StringBuffer hexString = new StringBuffer();
        for (byte b2 : b) {
            String plainText = Integer.toHexString(b2 & MotionEventCompat.ACTION_MASK);
            if (plainText.length() < 2) {
                plainText = "0" + plainText;
            }
            hexString.append(plainText);
        }
        return hexString.toString();
    }

    public static void main(String[] args) throws Exception {
        String jiami = URLEncoder.encode("test1234 ", "utf-8").toLowerCase();
        System.out.println("鍔犲瘑鏁版嵁:" + jiami);
        String a = toHexString(encrypt(jiami)).toUpperCase();
        System.out.println("鍔犲瘑鍚庣殑鏁版嵁涓�:" + a);
        System.out.println("瑙ｅ瘑鍚庣殑鏁版嵁:" + URLDecoder.decode(decrypt(a), "utf-8"));
    }

    public static void renameFile(String oldname, String newname) {
        if (oldname.equals(newname)) {
            System.out.println("鏂版枃浠跺悕鍜屾棫鏂囦欢鍚嶇浉鍚�...");
            return;
        }
        File oldfile = new File(oldname);
        File newfile = new File(newname);
        if (!oldfile.exists()) {
            return;
        }
        if (newfile.exists()) {
            System.out.println(new StringBuilder(String.valueOf(newname)).append("宸茬粡瀛樺湪锛�").toString());
        } else {
            oldfile.renameTo(newfile);
        }
    }

    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        if (!file.isFile() || !file.exists()) {
            return false;
        }
        file.delete();
        return true;
    }

    public static String tempFileName(String fileName) {
        String tempFileName = "";
        int length = fileName.length();
        return fileName.substring(0, length - 4) + "_temp" + fileName.substring((length - 4) + 1, length);
    }

    public static String creatEncryptPath(String fileName) {
        String[] paths = fileName.split("/");
        String sdcardPath = fileName.substring(0, fileName.indexOf("/FlightLog"));
        String flightPath = paths[1];
        String encryptName = paths[3];
        String FlightEncryptLog = "FlightEncryptLog";
        String FileEncryptPath = fileName.replace("FlightLog", "FlightEncryptLog");
        String telFileEncryptPath = new StringBuilder(String.valueOf(sdcardPath)).append("/FlightEncryptLog/Telemetry").toString();
        String remFileEncryptPath = new StringBuilder(String.valueOf(sdcardPath)).append("/FlightEncryptLog/Remote").toString();
        String remGPSFileEncryptPath = new StringBuilder(String.valueOf(sdcardPath)).append("/FlightEncryptLog/RemoteGPS").toString();
        File file1 = new File(telFileEncryptPath);
        if (!file1.exists()) {
            file1.mkdirs();
        }
        File file2 = new File(remFileEncryptPath);
        if (!file2.exists()) {
            file2.mkdirs();
        }
        File file3 = new File(remGPSFileEncryptPath);
        if (!file3.exists()) {
            file3.mkdirs();
        }
        return FileEncryptPath;
    }

    public static void logEncrypt(String fileName) {
        if (fileName != null && new File(fileName).exists()) {
            try {
                encrypt(fileName, creatEncryptPath(fileName));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
