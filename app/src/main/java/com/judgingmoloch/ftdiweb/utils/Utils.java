package com.judgingmoloch.ftdiweb.utils;

public class Utils {
    public static String toHex(byte[] b) {
        String s = "";
        for (byte bb : b) {
            s += String.format("0x%02x ", bb);
        }
        if (s.length() > 0) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    public static String join(char... c) {
        String s = "";
        for (int i = 0; i < c.length; i++) {
            s += String.format("%04x ", (int) c[i]);
        }
        if (s.length() > 0) {
            return s.substring(0, s.length() - 1);
        }
        return s;
    }

    public static String join(byte... b) {
        String s = "";
        for (int i = 0; i < b.length; i++) {
            s += String.format("%02x ", (byte) b[i]);
        }
        if (s.length() > 0) {
            return s.substring(0,  s.length() - 1);
        }
        return s;
    }

    public static int toInt(byte... data) {
        int i = 0;
        for (byte b : data) {
            i = i << 8;
            i += b;
        }
        return i;
    }

    public static char[] toChar(byte... data) {
        int n = (data.length + 1) / 2;
        char[] cdata = new char[n];
        int j = 0;
        for (int i = 0; i < n; i++) {
            cdata[i] |= data[j++] << 8;
            if (j < data.length) {
                cdata[i] |= data[j++];
            }
        }
        return cdata;
    }

    public static String dataToString(byte... data) {
        String s = "";
        for (int i = 0; i < data.length / 2; i++) {
            s += String.format("0x%02x%02x ", data[data.length-2*i-2], data[data.length-2*i-1]);
        }
        return s;
    }

    public static byte[] swapBytes(byte... data) {
        for (int i = 0; i < data.length / 2; i++) {
            byte temp = data[2*i];
            data[2*i] = data[2*i+1];
            data[2*i+1] = temp;
        }
        return data;
    }

    // In-place reversal
    public static byte[] reverse(byte... data) {
        for (int i = 0; i < data.length / 2; i++) {
            byte temp = data[data.length - i - 1];
            data[data.length - i - 1] = data[i];
            data[i] = temp;
        }
        return data;
    }

    public static byte[] toByte(char... data) {
        int[] idata = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            idata[i] = data[i];
        }
        return toByte(idata);
    }

    // Sort of convoluted way of converting some ints into some bytes
    public static byte[] toByte(int... data) {
        int n = data.length;
        for (int c : data) {
            int d = c >> 8;
            while (d != 0) {
                d >>= 8;
                n++;
            }
        }
        byte[] b = new byte[n];
        int i = 0;
        for (int c : data) {
            b[i++] = (byte) (c);
            int d = c >> 8;
            while (d != 0) {
                b[i++] = (byte) d;
                d >>= 8;
            }
        }
        return b;
    }


    // Converts byte to string the coorect way
    public static String bytesToString(byte[] b) {
        String s = "";

        for (int i = 0; i < b.length; i++) {
            s += String.format("%02x ", b[i]);
        }

        return s;
    }

    // Converts string to byte the correct way
    public static byte[] stringToBytes(String x) {
        String[] s = x.split(" |\n|,");
        byte[] r = new byte[s.length];

        for (int i = 0; i < s.length; i++) {
            try {
                r[i] = (byte) Integer.parseInt(s[i].toLowerCase(), 16);
            } catch (Exception e) {
                r[i] = (byte) 0x00;
            }
        }

        return r;
    }
}
