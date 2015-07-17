package com.judgingmoloch.ftdiweb.tcl;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.judgingmoloch.ftdiweb.R;

public class TclFragment extends Fragment {

    // TODO: Rename and change types and number of parameters
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


        return view;
    }

}
