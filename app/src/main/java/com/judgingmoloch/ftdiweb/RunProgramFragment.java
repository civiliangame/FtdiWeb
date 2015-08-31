package com.judgingmoloch.ftdiweb;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.judgingmoloch.ftdiweb.connection.Instructions;
import com.judgingmoloch.ftdiweb.driver.AndroidDriver;
import com.judgingmoloch.ftdiweb.utils.Utils;

public class RunProgramFragment extends Fragment {
    private String TAG = "RunProgramFragment";

    private MainActivity parentContext;
    private AndroidDriver driver;
    private Instructions instructions;
    private ProgressBar progressBar;
    private Handler mHandler = new Handler();
    private TextView errorLog;

    TextView output;

    public static RunProgramFragment newInstance() {
        return new RunProgramFragment();
    }

    public RunProgramFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_run_program, container, false);

        // Get parent activity, driver and instruction set to run
        parentContext = (MainActivity) view.getContext();
        driver = parentContext.driver;
        instructions = parentContext.instructions;

        // Get output TextView
        output = (TextView) view.findViewById(R.id.data_output);
        errorLog = (TextView) view.findViewById(R.id.program_error_log);

        // Give user information about the instruction set
        ((TextView) view.findViewById(R.id.program_instruction_name)).setText(instructions.name);
        ((TextView) view.findViewById(R.id.program_instruction_description)).setText(instructions.description);
        ((TextView) view.findViewById(R.id.program_instruction_body)).setText(instructions.body);

        // Get progress bar
        progressBar = (ProgressBar) view.findViewById(R.id.programming_progress_bar);

        view.findViewById(R.id.run_code).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                run();
            }
        });

        return view;
    }

    private void run() {
        new Thread(new Runnable() {
            public void updateProgressBar(final int i) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(i);
                    }
                });
            }

            public void updateErrorLog(final String s) {
                Log.e(TAG, s);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        errorLog.setText(s);
                        progressBar.setProgress(100);
                    }
                });
            }

            public void updateOutput(final String s) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        output.setText(s);
                    }
                });
            }

            public void run() {
                // Get data from the instructions body
                byte[] data = Utils.stringToBytes(instructions.body);

                updateProgressBar(0);

                // Verify connection
                boolean r;

                r = driver.verifyCpuId();
                if (!r) {
                    updateErrorLog("Could not verify CPU ID");
                    return;
                }

                updateProgressBar(10);

                driver.getDevice();

                updateProgressBar(20);

                r = driver.verifyCpuId();
                if (!r) {
                    updateErrorLog("Could not verify CPU ID");
                    return;
                }

                updateProgressBar(30);

                driver.initBreakUnits();

                updateProgressBar(40);

                // Number of bytes
                int byte_size = data.length;

                driver.readSingle(0x0000, true);

                updateProgressBar(50);

                // POR and halt the CPU
                driver.executePorHalt();

                updateProgressBar(60);

                // Write the program to memory
                int startAddress = 0x10000 - byte_size;
                driver.writeBurst(startAddress, data);

                updateProgressBar(70);

                try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }

                // Verify that the data was written correctly
                // TODO Verification method

                updateProgressBar(80);

                // Run the CPU
                int cpuCtlOrg = Utils.toInt(driver.readRegister("CPU_CTL"));

                updateProgressBar(90);

                driver.writeRegister("CPU_CTL", cpuCtlOrg | 0x02);

                updateProgressBar(100);
            }
        }).start();
    }
}
