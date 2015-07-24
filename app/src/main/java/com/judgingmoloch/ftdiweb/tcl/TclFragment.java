package com.judgingmoloch.ftdiweb.tcl;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.judgingmoloch.ftdiweb.MainActivity;
import com.judgingmoloch.ftdiweb.R;
import com.judgingmoloch.ftdiweb.driver.AndroidDriver;
import com.judgingmoloch.ftdiweb.utils.Utils;

public class TclFragment extends Fragment {
    private TextView output;

    // Relevant cross-view variables
    private MainActivity parentContext;
    private AndroidDriver driver;

    public static TclFragment newInstance() {
        return new TclFragment();
    }

    public TclFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tcl, container, false);

        // Add button listener
        view.findViewById(R.id.run_code).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                run();
            }
        });

        // Add output for read data
        output = (TextView) view.findViewById(R.id.data_output);

        // Get parent context and driver
        parentContext = (MainActivity) getActivity();
        driver = parentContext.driver;

        return view;
    }

    public void run() {
        // Test out the driver
        boolean b = driver.writeSingle(0x5000, 0xFACE);
        byte[] r = driver.readRegister("CPU_ID_HI");
        output.setText("Wrote: " + b + " Read: " + Utils.join(r));
//        byte[] r_data = a_driver.readBurst(0x7000, 8);
//        name.setText(Utils.join(r_data));
    }

}
