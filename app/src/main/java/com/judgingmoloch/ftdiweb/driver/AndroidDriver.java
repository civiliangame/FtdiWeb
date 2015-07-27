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

    // For logging
    private String TAG = "AndroidDriver";

    /* Receive and hold data from the RX buffer */
    List<Byte> byteBuffer = new ArrayList<>();
    private byte[] readData;

    /* Connection settings */
    private int BAUD_RATE = 115200;
    private byte DATA_BITS = D2xxManager.FT_DATA_BITS_8;
    private byte STOP_BITS = D2xxManager.FT_STOP_BITS_1;
    private byte PARITY = D2xxManager.FT_PARITY_NONE;
    private short FLOW_CONTROL = D2xxManager.FT_FLOW_NONE;

    private boolean connected = false;

    /* Some variables for representing the context */
    public MainActivity parentContext;
    public Instructions instructions;
    public D2xxManager d2xxManager;
    public FT_Device ftDev;
    public ReadThread readThread;

    /* When data is read, add it to the byte buffer */
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            for (byte b : readData) {
                byteBuffer.add(b);
            }
        }
    };

    public void log(String msg) {
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
                readThread = new ReadThread(handler);
                readThread.run();
                connected = true;
                log("Connected to device");
            }
        } else {
            log("Device is not open.");
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

    /* -----------------------------------------------------------
     * Override these methods to implement the GenericDriver class
     * ----------------------------------------------------------- */

    @Override
    protected boolean write(byte... b) {
        // Make sure the device is connected
        if (!connected) {
            try {
                connected = connect();
                if (!connected) return false;
            } catch (DeviceConnectionError e) {
                e.printStackTrace();
                return false;
            }
        }

        int written = -1;

        if (ftDev != null && ftDev.isOpen()) {
            synchronized (ftDev) {
                // Set the latency timer
                ftDev.setLatencyTimer((byte) 16);

                // Purge TX and RX buffers
                ftDev.stopInTask();
                ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
                ftDev.restartInTask();

                // Write the data
                written = ftDev.write(b, b.length, true);
            }
        } else {
            connected = false;
        }

        return written == 0;
    }

    @Override
    protected byte[] read(int length) {
        /* Basically, return everything in the RX buffer */
        byte[] b = new byte[byteBuffer.size()];
        int i = 0;
        for (Byte bb : byteBuffer) {
            b[i++] = bb;
        }
        return b;
    }

    private class ReadThread extends Thread {
        Handler mHandler;

        ReadThread(Handler h) {
            mHandler = h;
            this.setPriority(Thread.MIN_PRIORITY);
        }

        @Override
        public void run() {
            try {
                while (connected) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    synchronized (ftDev) {
                        int iavailable = ftDev.getQueueStatus();
                        if (iavailable > 0) {
                            readData = new byte[iavailable];
                            ftDev.read(readData, iavailable);
                            Message msg = mHandler.obtainMessage();
                            mHandler.sendMessage(msg);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
