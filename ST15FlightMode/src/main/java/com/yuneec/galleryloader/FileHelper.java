package com.yuneec.galleryloader;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileHelper {
    private static final int FILE_BUFFER_SIZE = 51200;
    private static final String TAG = "FileHelper";

    public static boolean fileIsExist(String filePath) {
        if (filePath == null || filePath.length() < 1) {
            Log.e(TAG, "param invalid, filePath: " + filePath);
            return false;
        } else if (new File(filePath).exists()) {
            return true;
        } else {
            return false;
        }
    }

    public static InputStream readFile(String filePath) {
        if (filePath == null) {
            Log.e(TAG, "Invalid param. filePath: " + filePath);
            return null;
        }
        try {
            if (fileIsExist(filePath)) {
                return new FileInputStream(new File(filePath));
            }
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "Exception, ex: " + ex.toString());
            return null;
        }
    }

    public static boolean createDirectory(String filePath) {
        if (filePath == null) {
            return false;
        }
        File file = new File(filePath);
        if (file.exists()) {
            return true;
        }
        return file.mkdirs();
    }

    public static boolean deleteDirectory(String filePath) {
        if (filePath == null) {
            Log.e(TAG, "Invalid param. filePath: " + filePath);
            return false;
        }
        File file = new File(filePath);
        if (file == null || !file.exists()) {
            return false;
        }
        if (file.isDirectory()) {
            File[] list = file.listFiles();
            for (int i = 0; i < list.length; i++) {
                Log.d(TAG, "delete filePath: " + list[i].getAbsolutePath());
                if (list[i].isDirectory()) {
                    deleteDirectory(list[i].getAbsolutePath());
                } else {
                    list[i].delete();
                }
            }
        }
        Log.d(TAG, "delete filePath: " + file.getAbsolutePath());
        file.delete();
        return true;
    }

    public static boolean writeFile(String filePath, InputStream inputStream) {
        if (filePath == null || filePath.length() < 1) {
            Log.e(TAG, "Invalid param. filePath: " + filePath);
            return false;
        }
        try {
            File file = new File(filePath);
            if (file.exists()) {
                deleteDirectory(filePath);
            }
            String pth = filePath.substring(0, filePath.lastIndexOf("/"));
            if (!createDirectory(pth)) {
                Log.e(TAG, "createDirectory fail path = " + pth);
                return false;
            } else if (file.createNewFile()) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                byte[] buf = new byte[1024];
                int c = inputStream.read(buf);
                while (-1 != c) {
                    fileOutputStream.write(buf, 0, c);
                    c = inputStream.read(buf);
                }
                fileOutputStream.flush();
                fileOutputStream.close();
                return true;
            } else {
                Log.e(TAG, "createNewFile fail filePath = " + filePath);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean writeFile(String filePath, String fileContent) {
        return writeFile(filePath, fileContent, false);
    }

    public static boolean writeFile(String filePath, String fileContent, boolean append) {
        if (filePath == null || fileContent == null || filePath.length() < 1 || fileContent.length() < 1) {
            Log.e(TAG, "Invalid param. filePath: " + filePath + ", fileContent: " + fileContent);
            return false;
        }
        try {
            File file = new File(filePath);
            if (!file.exists() && !file.createNewFile()) {
                return false;
            }
            BufferedWriter output = new BufferedWriter(new FileWriter(file, append));
            output.write(fileContent);
            output.flush();
            output.close();
            return true;
        } catch (IOException ioe) {
            Log.e(TAG, "writeFile ioe: " + ioe.toString());
            return false;
        }
    }

    public static long getFileSize(String filePath) {
        if (filePath == null) {
            Log.e(TAG, "Invalid param. filePath: " + filePath);
            return 0;
        }
        File file = new File(filePath);
        if (file == null || !file.exists()) {
            return 0;
        }
        return file.length();
    }

    public static long getFileModifyTime(String filePath) {
        if (filePath == null) {
            Log.e(TAG, "Invalid param. filePath: " + filePath);
            return 0;
        }
        File file = new File(filePath);
        if (file == null || !file.exists()) {
            return 0;
        }
        return file.lastModified();
    }

    public static boolean setFileModifyTime(String filePath, long modifyTime) {
        if (filePath == null) {
            Log.e(TAG, "Invalid param. filePath: " + filePath);
            return false;
        }
        File file = new File(filePath);
        if (file == null || !file.exists()) {
            return false;
        }
        return file.setLastModified(modifyTime);
    }

    public static boolean copyFile(ContentResolver cr, String fromPath, String destUri) {
        Exception ex;
        Throwable th;
        if (cr == null || fromPath == null || fromPath.length() < 1 || destUri == null || destUri.length() < 1) {
            Log.e(TAG, "copyFile Invalid param. cr=" + cr + ", fromPath=" + fromPath + ", destUri=" + destUri);
            return false;
        }
        InputStream is = null;
        OutputStream os = null;
        try {
            InputStream is2 = new FileInputStream(fromPath);
            String path = null;
            Uri uri = null;
            try {
                String lwUri = destUri.toLowerCase();
                if (lwUri.startsWith("content://")) {
                    uri = Uri.parse(destUri);
                } else if (lwUri.startsWith("file://")) {
                    uri = Uri.parse(destUri);
                    path = uri.getPath();
                } else {
                    path = destUri;
                }
                if (path != null) {
                    File fl = new File(path);
                    String pth = path.substring(0, path.lastIndexOf("/"));
                    File pf = new File(pth);
                    if (pf.exists() && !pf.isDirectory()) {
                        pf.delete();
                    }
                    pf = new File(new StringBuilder(String.valueOf(pth)).append(File.separator).toString());
                    if (!(pf.exists() || pf.mkdirs())) {
                        Log.e(TAG, "Can't make dirs, path=" + pth);
                    }
                    pf = new File(path);
                    if (pf.exists()) {
                        if (pf.isDirectory()) {
                            deleteDirectory(path);
                        } else {
                            pf.delete();
                        }
                    }
                    OutputStream os2 = new FileOutputStream(path);
                    try {
                        fl.setLastModified(System.currentTimeMillis());
                        os = os2;
                    } catch (Exception e) {
                        ex = e;
                        os = os2;
                        is = is2;
                        try {
                            Log.e(TAG, "Exception, ex: " + ex.toString());
                            if (is != null) {
                                try {
                                    is.close();
                                } catch (Exception e2) {
                                }
                            }
                            if (os != null) {
                                try {
                                    os.close();
                                } catch (Exception e3) {
                                }
                            }
                            return false;
                        } catch (Throwable th2) {
                            th = th2;
                            if (is != null) {
                                try {
                                    is.close();
                                } catch (Exception e4) {
                                }
                            }
                            if (os != null) {
                                try {
                                    os.close();
                                } catch (Exception e5) {
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        os = os2;
                        is = is2;
                        if (is != null) {
                            is.close();
                        }
                        if (os != null) {
                            os.close();
                        }
                        throw th;
                    }
                }
                os = new AutoCloseOutputStream(cr.openFileDescriptor(uri, "w"));
                byte[] dat = new byte[1024];
                for (int i = is2.read(dat); -1 != i; i = is2.read(dat)) {
                    os.write(dat, 0, i);
                }
                is2.close();
                is = null;
                os.flush();
                os.close();
                os = null;
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e6) {
                    }
                }
                if (os != null) {
                    try {
                        os.close();
                    } catch (Exception e7) {
                    }
                }
                return true;
            } catch (Exception e8) {
                ex = e8;
                is = is2;
                Log.e(TAG, "Exception, ex: " + ex.toString());
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
                return false;
            } catch (Throwable th4) {
                th = th4;
                is = is2;
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
                throw th;
            }
        } catch (Exception e9) {
            ex = e9;
            Log.e(TAG, "Exception, ex: " + ex.toString());
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
            return false;
        }
    }

    public static byte[] readAll(InputStream is) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        byte[] buf = new byte[1024];
        int c = is.read(buf);
        while (-1 != c) {
            baos.write(buf, 0, c);
            c = is.read(buf);
        }
        baos.flush();
        baos.close();
        return baos.toByteArray();
    }

    public static byte[] readFile(Context ctx, Uri uri) {
        if (ctx == null || uri == null) {
            Log.e(TAG, "Invalid param. ctx: " + ctx + ", uri: " + uri);
            return null;
        }
        InputStream is = null;
        if (uri.getScheme().toLowerCase().equals("file")) {
            is = readFile(uri.getPath());
        }
        try {
            is = ctx.getContentResolver().openInputStream(uri);
            if (is == null) {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e) {
                    }
                }
                return null;
            }
            byte[] bret = readAll(is);
            is.close();
            is = null;
            if (is == null) {
                return bret;
            }
            try {
                is.close();
                return bret;
            } catch (Exception e2) {
                return bret;
            }
        } catch (FileNotFoundException fne) {
            Log.e(TAG, "FilNotFoundException, ex: " + fne.toString());
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e3) {
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Exception, ex: " + ex.toString());
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e4) {
                }
            }
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e5) {
                }
            }
        }
        return null;
    }

    public static boolean writeFile(String filePath, byte[] content) {
        Exception ex;
        Throwable th;
        if (filePath == null || content == null) {
            Log.e(TAG, "Invalid param. filePath: " + filePath + ", content: " + content);
            return false;
        }
        FileOutputStream fileOutputStream = null;
        try {
            String pth = filePath.substring(0, filePath.lastIndexOf("/"));
            File pf = new File(pth);
            if (pf.exists() && !pf.isDirectory()) {
                pf.delete();
            }
            pf = new File(filePath);
            if (pf.exists()) {
                if (pf.isDirectory()) {
                    deleteDirectory(filePath);
                } else {
                    pf.delete();
                }
            }
            pf = new File(new StringBuilder(String.valueOf(pth)).append(File.separator).toString());
            if (!(pf.exists() || pf.mkdirs())) {
                Log.e(TAG, "Can't make dirs, path=" + pth);
            }
            FileOutputStream fos = new FileOutputStream(filePath);
            try {
                fos.write(content);
                fos.flush();
                fos.close();
                fileOutputStream = null;
                pf.setLastModified(System.currentTimeMillis());
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Exception e) {
                    }
                }
                return true;
            } catch (Exception e2) {
                ex = e2;
                fileOutputStream = fos;
                try {
                    Log.e(TAG, "Exception, ex: " + ex.toString());
                    if (fileOutputStream != null) {
                        return false;
                    }
                    try {
                        fileOutputStream.close();
                        return false;
                    } catch (Exception e3) {
                        return false;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Exception e4) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileOutputStream = fos;
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
        } catch (Exception e5) {
            ex = e5;
            Log.e(TAG, "Exception, ex: " + ex.toString());
            if (fileOutputStream != null) {
                return false;
            }
            fileOutputStream.close();
            return false;
        }
    }

    public static boolean readZipFile(String zipFileName, StringBuffer crc) {
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFileName));
            while (true) {
                ZipEntry entry = zis.getNextEntry();
                if (entry == null) {
                    zis.close();
                    return true;
                }
                crc.append(entry.getCrc() + ", size: " + entry.getSize());
            }
        } catch (Exception ex) {
            Log.e(TAG, "Exception: " + ex.toString());
            return false;
        }
    }

    public static byte[] readGZipFile(String zipFileName) {
        if (fileIsExist(zipFileName)) {
            Log.i(TAG, "zipFileName: " + zipFileName);
            try {
                FileInputStream fin = new FileInputStream(zipFileName);
                byte[] buffer = new byte[1024];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while (true) {
                    int size = fin.read(buffer, 0, buffer.length);
                    if (size == -1) {
                        return baos.toByteArray();
                    }
                    baos.write(buffer, 0, size);
                }
            } catch (Exception e) {
                Log.i(TAG, "read zipRecorder file error");
            }
        }
        return null;
    }

    public static boolean zipFile(String baseDirName, String fileName, String targerFileName) throws IOException {
        boolean z = false;
        if (!(baseDirName == null || "".equals(baseDirName))) {
            File baseDir = new File(baseDirName);
            if (baseDir.exists() && baseDir.isDirectory()) {
                String baseDirPath = baseDir.getAbsolutePath();
                ZipOutputStream out = new ZipOutputStream(new FileOutputStream(new File(targerFileName)));
                File file = new File(baseDir, fileName);
                if (file.isFile()) {
                    z = fileToZip(baseDirPath, file, out);
                } else {
                    z = dirToZip(baseDirPath, file, out);
                }
                out.close();
            }
        }
        return z;
    }

    public static boolean unZipFile(String fileName, String unZipDir) throws Exception {
        File f = new File(unZipDir);
        if (!f.exists()) {
            f.mkdirs();
        }
        ZipFile zipfile = new ZipFile(fileName);
        Enumeration<?> enumeration = zipfile.entries();
        byte[] data = new byte[FILE_BUFFER_SIZE];
        Log.i(TAG, "unZipDir: " + unZipDir);
        while (enumeration.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) enumeration.nextElement();
            if (entry.isDirectory()) {
                File f1 = new File(new StringBuilder(String.valueOf(unZipDir)).append("/").append(entry.getName()).toString());
                Log.i(TAG, "entry.isDirectory XXX " + f1.getPath());
                if (!f1.exists()) {
                    f1.mkdirs();
                }
            } else {
                BufferedInputStream is = new BufferedInputStream(zipfile.getInputStream(entry));
                File file = new File(new StringBuilder(String.valueOf(unZipDir)).append("/").append(entry.getName()).toString());
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                RandomAccessFile m_randFile = new RandomAccessFile(file, "rw");
                int begin = 0;
                while (true) {
                    int count = is.read(data, 0, FILE_BUFFER_SIZE);
                    if (count == -1) {
                        break;
                    }
                    try {
                        m_randFile.seek((long) begin);
                    } catch (Exception ex) {
                        Log.e(TAG, "exception, ex: " + ex.toString());
                    }
                    m_randFile.write(data, 0, count);
                    begin += count;
                }
                file.delete();
                m_randFile.close();
                is.close();
            }
        }
        return true;
    }

    private static boolean fileToZip(String baseDirPath, File file, ZipOutputStream out) throws IOException {
        IOException e;
        Throwable th;
        FileInputStream in = null;
        byte[] buffer = new byte[FILE_BUFFER_SIZE];
        try {
            FileInputStream in2 = new FileInputStream(file);
            try {
                ZipEntry entry = new ZipEntry(getEntryName(baseDirPath, file));
                ZipEntry zipEntry;
                try {
                    out.putNextEntry(entry);
                    while (true) {
                        int bytes_read = in2.read(buffer);
                        if (bytes_read == -1) {
                            break;
                        }
                        out.write(buffer, 0, bytes_read);
                    }
                    out.closeEntry();
                    in2.close();
                    if (out != null) {
                        out.closeEntry();
                    }
                    if (in2 != null) {
                        in2.close();
                    }
                    zipEntry = entry;
                    in = in2;
                    return true;
                } catch (IOException e2) {
                    e = e2;
                    zipEntry = entry;
                    in = in2;
                } catch (Throwable th2) {
                    th = th2;
                    zipEntry = entry;
                    in = in2;
                }
            } catch (IOException e3) {
                e = e3;
                in = in2;
                try {
                    Log.e(TAG, "Exception, ex: " + e.toString());
                    if (out != null) {
                        out.closeEntry();
                    }
                    if (in != null) {
                        return false;
                    }
                    in.close();
                    return false;
                } catch (Throwable th3) {
                    th = th3;
                    if (out != null) {
                        out.closeEntry();
                    }
                    if (in != null) {
                        in.close();
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                in = in2;
                if (out != null) {
                    out.closeEntry();
                }
                if (in != null) {
                    in.close();
                }
                throw th;
            }
        } catch (IOException e4) {
            e = e4;
            Log.e(TAG, "Exception, ex: " + e.toString());
            if (out != null) {
                out.closeEntry();
            }
            if (in != null) {
                return false;
            }
            in.close();
            return false;
        }
    }

    private static boolean dirToZip(String baseDirPath, File dir, ZipOutputStream out) throws IOException {
        if (!dir.isDirectory()) {
            return false;
        }
        File[] files = dir.listFiles();
        if (files.length == 0) {
            try {
                out.putNextEntry(new ZipEntry(getEntryName(baseDirPath, dir)));
                out.closeEntry();
            } catch (IOException e) {
                Log.e(TAG, "Exception, ex: " + e.toString());
            }
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                fileToZip(baseDirPath, files[i], out);
            } else {
                dirToZip(baseDirPath, files[i], out);
            }
        }
        return true;
    }

    private static String getEntryName(String baseDirPath, File file) {
        if (!baseDirPath.endsWith(File.separator)) {
            baseDirPath = new StringBuilder(String.valueOf(baseDirPath)).append(File.separator).toString();
        }
        String filePath = file.getAbsolutePath();
        if (file.isDirectory()) {
            filePath = new StringBuilder(String.valueOf(filePath)).append("/").toString();
        }
        return filePath.substring(baseDirPath.length() + filePath.indexOf(baseDirPath));
    }
}
