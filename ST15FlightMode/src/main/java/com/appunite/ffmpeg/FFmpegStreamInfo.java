package com.appunite.ffmpeg;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FFmpegStreamInfo {
    private static Map<String, Locale> sLocaleMap;
    private CodecType mMediaType;
    private Map<String, String> mMetadata;
    private int mStreamNumber;

    public enum CodecType {
        UNKNOWN,
        AUDIO,
        VIDEO,
        SUBTITLE,
        ATTACHMENT,
        NB,
        DATA
    }

    static {
        String[] languages = Locale.getISOLanguages();
        sLocaleMap = new HashMap(languages.length);
        for (String language : languages) {
            Locale locale = new Locale(language);
            sLocaleMap.put(locale.getISO3Language(), locale);
        }
    }

    public void setMetadata(Map<String, String> metadata) {
        this.mMetadata = metadata;
    }

    void setMediaTypeInternal(int mediaTypeInternal) {
        this.mMediaType = CodecType.values()[mediaTypeInternal];
    }

    void setStreamNumber(int streamNumber) {
        this.mStreamNumber = streamNumber;
    }

    public int getStreamNumber() {
        return this.mStreamNumber;
    }

    public Locale getLanguage() {
        if (this.mMetadata == null) {
            return null;
        }
        String iso3Langugae = (String) this.mMetadata.get("language");
        if (iso3Langugae != null) {
            return (Locale) sLocaleMap.get(iso3Langugae);
        }
        return null;
    }

    public CodecType getMediaType() {
        return this.mMediaType;
    }

    public Map<String, String> getMetadata() {
        return this.mMetadata;
    }

    public String toString() {
        Locale language = getLanguage();
        return "{\n" + "\tmediaType: " + this.mMediaType + "\n" + "\tlanguage: " + (language == null ? "unknown" : language.getDisplayName()) + "\n" + "\tmetadata " + this.mMetadata + "\n" + "}";
    }
}
