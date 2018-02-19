package com.yuneec.flight_settings;

import com.yuneec.IPCameraManager.Cameras;
import java.io.InputStream;
import java.util.List;

public interface CameraParser {
    List<Cameras> parse(InputStream inputStream) throws Exception;

    String serialize(List<Cameras> list) throws Exception;
}
