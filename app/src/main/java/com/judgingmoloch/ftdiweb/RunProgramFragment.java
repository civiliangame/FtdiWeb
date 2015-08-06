package com.judgingmoloch.ftdiweb;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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
    private MainActivity parentContext;
    private AndroidDriver driver;
    private Instructions instructions;
    private ProgressBar progressBar;
    private Handler mHandler = new Handler();

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

    /*
    Oh my god, this is such a hack... I am so sorry to anyone who ever has to figure out what this
    stuff means. It is basically raw byte code from the Tcl scripts. I tried to work around it a few
    times, but to no avail. Anyways, there you are. Have fun.
     */
    private void run() {
        // Start lengthy operation in a background thread
        new Thread(new Runnable() {
            public void run() {
                byte[] data = Utils.stringToBytes(instructions.body);
                mHandler.post(new Runnable() {
                    public void run() {
                        progressBar.setProgress(0);
                    }
                });
                driver.write(0x80);
                try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
                driver.write(0xC0, 0x00);
                mHandler.post(new Runnable() {
                    public void run() {
                        progressBar.setProgress(5);
                    }
                });
                driver.write(0x00);
                driver.read(2);
                driver.write(0x01);
                driver.read(2);
                driver.write(0x18);
                driver.read(2);
                driver.write(0xc2);
                driver.write(0x18);
                driver.write(0x00);
                driver.read(2);
                driver.write(0x01);
                driver.read(2);
                driver.write(0x18);
                driver.read(2);
                driver.write(0x8a);
                driver.write(0x34);
                driver.write(0x12);
                driver.write(0x0a);
                driver.read(2);
                driver.write(0x8e);
                driver.write(0x34);
                driver.write(0x12);
                driver.write(0x0e);
                driver.read(2);
                driver.write(0x92);
                driver.write(0x34);
                driver.write(0x12);
                driver.write(0x12);
                driver.read(2);
                driver.write(0x96);
                driver.write(0x34);
                driver.write(0x12);
                driver.write(0x16);
                driver.read(2);
                driver.write(0x87);
                driver.write(0x00);
                driver.write(0x00);
                driver.write(0x85);
                driver.write(0x00);
                driver.write(0x00);
                driver.write(0xc4);
                driver.write(0x05);
                driver.write(0x06);
                driver.read(2);
                driver.write(0x42);
                driver.read(1);
                driver.write(0xc2);
                driver.write(0x78);
                driver.write(0xc2);
                driver.write(0x18);
                driver.write(0x43);
                driver.read(1);
                driver.write(0xc3);
                driver.write(0x04);
                driver.write(0x87);
                driver.write(0xff);
                driver.write(0x1f);
                driver.write(0x85);
                driver.write(0x00);
                driver.write(0xc0);
                driver.write(0xc4);
                driver.write(0x03);
                mHandler.post(new Runnable() {
                    public void run() {
                        progressBar.setProgress(25);
                    }
                });
                driver.write(data);
                try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
                mHandler.post(new Runnable() {
                    public void run() {
                        progressBar.setProgress(50);
                    }
                });
                driver.write(0x87);
                driver.write(0xff);
                driver.write(0x1f);
                driver.write(0x85);
                driver.write(0x00);
                driver.write(0xc0);
                driver.write(0xc4);
                driver.write(0x01);
                mHandler.post(new Runnable() {
                    public void run() {
                        progressBar.setProgress(60);
                    }
                });
                // Read data
                final byte[] read_data = driver.read(data.length);
                mHandler.post(new Runnable() {
                    public void run() {
                        progressBar.setProgress(75);
                    }
                });
                parentContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        output.setText(Utils.join(read_data));
                    }
                });
                driver.write(0x42);
                driver.read(1);
                driver.write(0xc2);
                driver.write(0x1a);
                try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
                driver.write(0x43);
                driver.read(1);
                mHandler.post(new Runnable() {
                    public void run() {
                        progressBar.setProgress(100);
                    }
                });
            }
        }).start();
    }
}
