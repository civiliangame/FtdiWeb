package com.judgingmoloch.ftdiweb;

import com.ftdi.j2xx.D2xxManager;

public class D2xxUtils {

    public static String getType(int type) {
        switch (type) {
            case D2xxManager.FT_DEVICE_232B:        return "FT232B device";
            case D2xxManager.FT_DEVICE_8U232AM:     return "FT8U232AM device";
            case D2xxManager.FT_DEVICE_UNKNOWN:     return "Unknown device";
            case D2xxManager.FT_DEVICE_2232:        return "FT2232 device";
            case D2xxManager.FT_DEVICE_232R:        return "FT232R device";
            case D2xxManager.FT_DEVICE_2232H:       return "FT2232H device";
            case D2xxManager.FT_DEVICE_4232H:       return "FT4232H device";
            case D2xxManager.FT_DEVICE_232H:        return "FT232H device";
            case D2xxManager.FT_DEVICE_X_SERIES:    return "FTDI X_SERIES";
            case D2xxManager.FT_DEVICE_4222_0:
            case D2xxManager.FT_DEVICE_4222_1_2:
            case D2xxManager.FT_DEVICE_4222_3:      return "FT4222 device";
            default:                                return "FT232B device";
                
        }
    }
}
