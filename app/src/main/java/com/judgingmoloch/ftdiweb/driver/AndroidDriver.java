package com.judgingmoloch.ftdiweb.driver;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;
import com.judgingmoloch.ftdiweb.MainActivity;
import com.judgingmoloch.ftdiweb.connection.Instructions;
import com.judgingmoloch.ftdiweb.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AndroidDriver extends GenericDriver {

    /* Receive and hold data from the RX buffer */
    List<Byte> byteBuffer = new ArrayList<>();
    private byte[] readData;

    /* Connection settings */
    private int BAUD_RATE = 115200;
    private byte DATA_BITS = D2xxManager.FT_DATA_BITS_8;
    private byte STOP_BITS = D2xxManager.FT_STOP_BITS_1;
    private byte PARITY = D2xxManager.FT_PARITY_NONE;
    private short FLOW_CONTROL = D2xxManager.FT_FLOW_NONE;

    private long TIME_OUT = 2000; // Connection timeout, in milliseconds

    private boolean connected = false;

    /* Some variables for representing the context */
    public MainActivity parentContext;
    public Instructions instructions;
    public D2xxManager d2xxManager;
    public FT_Device ftDev;

    public void set(String s, Object v) {
        switch (s.toUpperCase()) {
            case "BAUD_RATE": BAUD_RATE = (int) v; break;
            case "DATA_BITS": DATA_BITS = (byte) v; break;
            case "STOP_BITS": STOP_BITS = (byte) v; break;
            case "PARITY": PARITY = (byte) v; break;
            case "FLOW_CONTROL": FLOW_CONTROL = (short) v; break;
        }
    }

    public void log(String msg) {
        String TAG = "AndroidDriver";
        Log.d(TAG, ">==< " + msg + " >==<");
    }

    public boolean connect() throws DeviceConnectionError {
        // Make sure the device is connected
        int devCount = d2xxManager.createDeviceInfoList(parentContext);
        log("Number of devices: " + devCount);

        if (devCount < 2) {
            log("Insufficient number of devices");
            return false;
        }

        // Open the device: Connect to the second port
        if (ftDev == null || !ftDev.isOpen()) {
            log("Device is not open: Opening device...");
            ftDev = d2xxManager.openByIndex(parentContext, 1);
        }

        // Make sure the device is open, then start reading from it
        if (ftDev == null) {
            log("Device could not be opened.");
            return false;
        }

        if (ftDev.isOpen()) {
            if (!connected) {
                connected = true;
                log("Opened device connection.");
            }
        } else {
            log("Device is not open.");
            connected = false;
            ftDev = null;
            return false;
        }

        // Configure device
        ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);
        ftDev.setBaudRate(BAUD_RATE);
        ftDev.setDataCharacteristics(DATA_BITS, STOP_BITS, PARITY);
        ftDev.setFlowControl(FLOW_CONTROL, (byte) 0x0b, (byte) 0x0d);

        log("Successfully configured device.");

        // Return true, because nothing went wrong
        return true;
    }

    public boolean disconnect() {
        // Sleep for 50 milliseconds
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        // Close the FT device, if it is open
        if (ftDev != null) {
            synchronized (ftDev) {
                if (ftDev.isOpen()) {
                    ftDev.close();
                }
            }
        }

        connected = false;
        return true;
    }

    public AndroidDriver(MainActivity context) {
        parentContext = context;
        instructions = context.instructions;
        d2xxManager = context.d2xxManager;

        try {
            connected = connect();
        } catch (DeviceConnectionError e) {
            e.printStackTrace();
        }
    }

    private boolean checkConnection() {
        if (!connected) {
            log("Not connected for writing. Connecting now...");
            try {
                connected = connect();
                if (!connected) {
                    log("Device could not be connected for writing.");
                    return false;
                }
            } catch (DeviceConnectionError e) {
                e.printStackTrace();
                log("A connection error occured: " + e.getMessage());
                return false;
            }
            purge();
        }
        return true;
    }

    public void purge() {
        // Purge TX and RX buffers
        ftDev.stopInTask();
        ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
        ftDev.restartInTask();
    }

    /* -----------------------------------------------------------
     * Override these methods to implement the GenericDriver class
     * ----------------------------------------------------------- */

    @Override
    protected boolean write(byte... b) {
        // Make sure the device is connected
        if (!checkConnection()) return false;

        int written = -1;

        if (ftDev != null && ftDev.isOpen()) {
            synchronized (ftDev) {
                // Set the latency timer
                ftDev.setLatencyTimer((byte) 16);

                // Write the data: Returns the number of bytes written
                written = ftDev.write(b, b.length, true);

                log("Wrote " + written + " bytes: " + Utils.join(b));
            }
        } else {
            if (ftDev == null) {
                log("Device is NULL");
            } else {
                log("Device is not open");
            }
            connected = false;
        }

        return written > 0;
    }

    @Override
    protected byte[] read(int length) {
        // Make sure device is connected
        if (!checkConnection()) return new byte[length];

        long startTime = System.currentTimeMillis();
        byte[] b = new byte[length];

        // Read from the device
        while (System.currentTimeMillis() - startTime < TIME_OUT) {
            synchronized (ftDev) {
                int b_available = ftDev.getQueueStatus();
                if (b_available > 0) {
                    ftDev.read(b, length, TIME_OUT);
                } else {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return new byte[length];
                    }
                }
            }
        }

        if (System.currentTimeMillis() - startTime > TIME_OUT) {
            log("Failed to read from device");
        } else {
            log("Read: " + Utils.join(b));
        }

        return b;
    }
}
