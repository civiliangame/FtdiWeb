package com.judgingmoloch.ftdiweb.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.judgingmoloch.ftdiweb.MainActivity;
import com.judgingmoloch.ftdiweb.R;

public class SettingsFragment extends Fragment {

    private Settings parentSettings;

    private EditText connectionURL;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    public SettingsFragment() { /* Required empty public constructor */ }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Get the parent settings
        parentSettings = ((MainActivity) getActivity()).settings;

        // Connection URL edit text
        connectionURL = (EditText) view.findViewById(R.id.connection_url);

        // Set to default value
        connectionURL.setText(parentSettings.connection_url);

        view.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentSettings.connection_url = connectionURL.getText().toString();
                Settings.writeSettings(getActivity(), parentSettings);
                Toast.makeText(getActivity(), "Saved settings", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

}
