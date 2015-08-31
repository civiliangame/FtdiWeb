package com.judgingmoloch.ftdiweb;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;
import com.judgingmoloch.ftdiweb.connection.Instructions;
import com.judgingmoloch.ftdiweb.utils.Utils;

public class RunCodeFragment extends Fragment {
    private final String TAG = "RunCodeFragment";

    /* Original */
    D2xxManager d2xxManager;
    FT_Device ftDev = null;
    int devCount = -1;
    int currentIndex = -1;
    int openIndex;

    /* Local variables */
    int baudRate;
    byte stopBit;
    byte dataBit;
    byte parity;
    byte flowControl;

    /* Parameters and more local variables */
    public static final int readLength = 512;
    public static int iEnableReadFlag = 1;
    public int readCount = 0;
    public int iavailable = 0;
    byte[] readData;
    char[] readDataToText;
    boolean bReadThreadGoing = false;
    ReadThread readThread;
    boolean uartConfigured = false;

    /* Message handler */
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (iavailable > 0) {
                output.append(Utils.bytesToString(readData, true) + "\n");
            }
        }
    };

    private MainActivity parentContext;
    private Instructions instructions;

    /* Text Views */
    TextView name;
    TextView description;
    TextView body;
    TextView connectionStatus;
    TextView statusText;

    /* Spinners */
    Spinner baudRateSpinner;
    Spinner stopBitSpinner;
    Spinner dataBitSpinner;
    Spinner parityBitSpinner;
    Spinner flowSpinner;
    Spinner portSpinner;

    TextView output;

    public static RunCodeFragment newInstance() {
        return new RunCodeFragment();
    }

    public RunCodeFragment() { /* Required empty public constructor */ }

    public int getShownIndex() { return getArguments().getInt("index", 5); }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get parent context and instructions to execute
        parentContext = (MainActivity) getActivity();
        instructions = parentContext.instructions;

        // Get device manager
        d2xxManager = parentContext.d2xxManager;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) return null;
        super.onCreate(savedInstanceState);

        // Inflate the view XML file
        final View view = inflater.inflate(R.layout.fragment_run_code, container, false);

        // Get the name, description, body and output text views
        name = (TextView) view.findViewById(R.id.instruction_name);
        description = (TextView) view.findViewById(R.id.instruction_description);
        body = (TextView) view.findViewById(R.id.instruction_body);
        connectionStatus = (TextView) view.findViewById(R.id.connection_status);
        output = (TextView) view.findViewById(R.id.output);
        statusText = (TextView) view.findViewById(R.id.status_text);

        // Baud rate spinner and default
        baudRateSpinner = (Spinner) view.findViewById(R.id.baud_rate_spinner);
        ArrayAdapter<CharSequence> baudRateAdapter = ArrayAdapter.createFromResource(parentContext, R.array.baud_rate, R.layout.default_spinner_textview);
        baudRateSpinner.setAdapter(baudRateAdapter);
        baudRateSpinner.setSelection(8);
        baudRateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                baudRate = Integer.parseInt(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        baudRate = 115200;

        // Stop bit spinner and default
        stopBitSpinner = (Spinner) view.findViewById(R.id.stop_bit_spinner);
        ArrayAdapter<CharSequence> stopBitAdapter = ArrayAdapter.createFromResource(parentContext, R.array.stop_bits, R.layout.default_spinner_textview);
        stopBitSpinner.setAdapter(stopBitAdapter);
        stopBitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                stopBit = Byte.parseByte(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        stopBit = 1;

        // Data bit spinner and default
        dataBitSpinner = (Spinner) view.findViewById(R.id.data_bit_spinner);
        ArrayAdapter<CharSequence> dataBitAdapter = ArrayAdapter.createFromResource(parentContext, R.array.data_bits, R.layout.default_spinner_textview);
        dataBitSpinner.setAdapter(dataBitAdapter);
        dataBitSpinner.setSelection(1);
        dataBitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dataBit = Byte.parseByte(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        dataBit = 8;

        // Parity bit spinner and default
        parityBitSpinner = (Spinner) view.findViewById(R.id.parity_bit_spinner);
        ArrayAdapter<CharSequence> parityBitAdapter = ArrayAdapter.createFromResource(parentContext, R.array.parity, R.layout.default_spinner_textview);
        parityBitSpinner.setAdapter(parityBitAdapter);
        parityBitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                parity = parseParityBit(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        parity = 0;

        // Flow spinner and default
        flowSpinner = (Spinner) view.findViewById(R.id.flow_spinner);
        ArrayAdapter<CharSequence> flowAdapter = ArrayAdapter.createFromResource(parentContext, R.array.flow_control, R.layout.default_spinner_textview);
        flowSpinner.setAdapter(flowAdapter);
        flowSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                flowControl = parseFlowControl(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        flowControl = 0;

        // Port spinner and default
        portSpinner = (Spinner) view.findViewById(R.id.port_spinner);
        ArrayAdapter<CharSequence> portAdapter = ArrayAdapter.createFromResource(parentContext, R.array.port_control, R.layout.default_spinner_textview);
        portSpinner.setAdapter(portAdapter);
        portSpinner.setSelection(1);
        portSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                openIndex = Integer.parseInt(parent.getItemAtPosition(position).toString()) - 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        openIndex = 1;

        // Initialize some local variables
        readData = new byte[readLength];
        readDataToText = new char[readLength];

        // Set text for name, description, and instruction body
        name.setText(instructions.name);
        description.setText(instructions.description);
        body.setText(instructions.body);
        connectionStatus.setText("Disconnected");

        // Runs when the "run" button is clicked
        view.findViewById(R.id.run_instructions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (devCount <= 0 || ftDev == null) {
                    Toast.makeText(parentContext, "Device is not connected", Toast.LENGTH_SHORT).show();
                } else {
                    if (!uartConfigured) {
                        setConfig(baudRate, dataBit, stopBit, parity, flowControl);
                    }

                    if (uartConfigured) {
                        sendMessage();
                    }
                }
            }
        });

        // Runs when the "connect" button is clicked
        view.findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (devCount <= 0) {
                    createDeviceList();
                }

                if (devCount > 0) {
                    connect();
                }
            }
        });

        return view;
    }

    public byte parseFlowControl(String s) {
        if (s.compareTo("CTS/RTS") == 0) {
            return 1;
        } else if (s.compareTo("DTR/DSR") == 0) {
            return 2;
        } else if (s.compareTo("XOFF/XON") == 0) {
            return 3;
        } else {
            return 0;
        }
    }

    public byte parseParityBit(String s) {
        if (s.compareTo("Odd") == 0) {
            return 1;
        } else if (s.compareTo("Even") == 0) {
            return 2;
        } else if (s.compareTo("Mark") == 0) {
            return 3;
        } else if (s.compareTo("Space") == 0) {
            return 4;
        } else {
            return 0; // Default, also for "None" argument
        }
    }

    public void setConfig(int baud, byte dataBits, byte stopBits, byte parity, byte flowControl) {
        if (!ftDev.isOpen()) {
            Toast.makeText(parentContext, "FT device is not open", Toast.LENGTH_SHORT).show();
            return;
        }

        // Configure to our port
        ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);
        ftDev.setBaudRate(baud);

        // Configure data bits
        switch (dataBits) {
            case 7:
                dataBits = D2xxManager.FT_DATA_BITS_7;
                break;
            case 8:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
            default:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
        }

        // Configure stop bits
        switch (stopBits) {
            case 1:
                stopBit = D2xxManager.FT_STOP_BITS_1;
                break;
            case 2:
                stopBits = D2xxManager.FT_STOP_BITS_2;
                break;
            default:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
        }

        // Configure parity
        switch (parity) {
            case 0:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
            case 1:
                parity = D2xxManager.FT_PARITY_ODD;
                break;
            case 2:
                parity = D2xxManager.FT_PARITY_EVEN;
                break;
            case 3:
                parity = D2xxManager.FT_PARITY_MARK;
                break;
            default:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
        }

        // Set data characteristics
        ftDev.setDataCharacteristics(dataBits, stopBits, parity);

        short flowControlSetting;
        switch (flowControl) {
            case 0:
                flowControlSetting = D2xxManager.FT_FLOW_NONE;
                break;
            case 1:
                flowControlSetting = D2xxManager.FT_FLOW_RTS_CTS;
                break;
            case 2:
                flowControlSetting = D2xxManager.FT_FLOW_DTR_DSR;
                break;
            case 3:
                flowControlSetting = D2xxManager.FT_FLOW_XON_XOFF;
                break;
            default:
                flowControlSetting = D2xxManager.FT_FLOW_NONE;
                break;
        }

        // Shouldn't be hard coded, but I don't know the correct way
        ftDev.setFlowControl(flowControlSetting, (byte) 0x0b, (byte) 0x0d);

        uartConfigured = true;
    }

    public void sendMessage() {
        if (!ftDev.isOpen()) {
            Toast.makeText(parentContext, "Failed to send message (device not found)", Toast.LENGTH_SHORT).show();
            return;
        }

        ftDev.setLatencyTimer((byte) 16);

        // Purge TX and RX buffers
        ftDev.stopInTask();
        boolean purged = ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
        ftDev.restartInTask();

        String writeData = instructions.body;
        byte[] outData = Utils.stringToBytes(writeData);

        // Check to make sure we have at least something to write
        boolean found = false;
        for (byte b : outData) {
            if (b != 0) {
                found = true;
            }
        }
        if (!found) {
            Log.e(TAG, "Data to write as all 0's!");
            return;
        }

        // Write bytes one at a time
        for (byte b : outData) {
            byte[] v = { b };
            int result = ftDev.write(v);
            if (result != 1) {
                Log.e(TAG, "Attempt to write \"" + String.format("%02x", b) + "\" to device yielded " + result + ", not 1");
            }
        }

//        int result = ftDev.write(outData);

        String s = "";
        for (byte b : outData) {
            s += String.format("0x%02x ", b);
        }

        if (s.length() > 0) {
            s = s.substring(0, s.length() - 1);
        }

        Log.d(TAG, ">==< Wrote " + s + " to device >==<");

        Toast.makeText(parentContext, "Wrote \"" + s + "\" to device", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        createDeviceList();
    }

    @Override
    public void onStop() {
        disconnect();
        super.onStop();
    }

    public void createDeviceList() {
        int tempDevCount = d2xxManager.createDeviceInfoList(parentContext);
        if (tempDevCount > 0) {
            if (devCount != tempDevCount) {
                devCount = tempDevCount;
            }
        } else {
            devCount = -1;
            currentIndex = -1;
        }
        updatePortNumberSelector();
    }

    public void updatePortNumberSelector() {
        if (devCount <= 0) {
            connectionStatus.setText("No device attached");
            Toast.makeText(parentContext, "No device attached", Toast.LENGTH_SHORT).show();
        } else {
            connectionStatus.setText(devCount + " port device attached");
            Toast.makeText(parentContext, devCount + " port device attached", Toast.LENGTH_SHORT).show();
        }
    }

    public void connect() {
        int tmpPortNumber = openIndex + 1;

        if (currentIndex != openIndex) {
            if (ftDev == null) {
                ftDev = d2xxManager.openByIndex(parentContext, openIndex);
            } else {
                synchronized (ftDev) {
                    ftDev = d2xxManager.openByIndex(parentContext, openIndex);
                }
            }
            uartConfigured = false;
        } else {
            Toast.makeText(parentContext, "Device port " + tmpPortNumber + " is already open", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ftDev == null) {
            Toast.makeText(parentContext, "Open device port (" + tmpPortNumber + ") no good, FT device is not available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ftDev.isOpen()) {
            currentIndex = openIndex;

            if (!bReadThreadGoing) {
                readThread = new ReadThread(handler);
                readThread.start();
                bReadThreadGoing = true;
            }

            Toast.makeText(parentContext, "Open device port (" + tmpPortNumber + ") OK, device is being read", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(parentContext, "Open device port (" + tmpPortNumber + ") no good, FT device is not open", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateDeviceInformation() {
        if (ftDev == null | !ftDev.isOpen()) {
            statusText.setText("");
        } else {
            statusText.setText(
                    String.format("Line Status: 0x%02x\n", ftDev.getLineStatus()) +
                            String.format("Modem Status: 0x%02x\n", ftDev.getModemStatus()) +
                            String.format("Event Status: 0x%02x\n", ftDev.getEventStatus()) +
                            String.format("Queue Status: %d\n", ftDev.getQueueStatus()));
        }
    }

    public void disconnect() {
        devCount = -1;
        currentIndex = -1;
        bReadThreadGoing = false;

        // Sleep for 50 milliseconds
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Close the FT device, if it is open
        if (ftDev != null) {
            synchronized (ftDev) {
                if (ftDev.isOpen()) {
                    ftDev.close();
                }
            }
        }
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
                while (bReadThreadGoing) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    synchronized (ftDev) {
                        iavailable = ftDev.getQueueStatus();
                        if (iavailable > 0) {
                            if (iavailable > readLength) {
                                iavailable = readLength;
                            }

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
