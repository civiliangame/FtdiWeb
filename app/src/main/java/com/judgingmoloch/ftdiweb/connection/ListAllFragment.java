package com.judgingmoloch.ftdiweb.connection;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.google.gson.Gson;
import com.judgingmoloch.ftdiweb.ItemListAdapter;
import com.judgingmoloch.ftdiweb.ItemListItem;
import com.judgingmoloch.ftdiweb.MainActivity;
import com.judgingmoloch.ftdiweb.R;

import java.util.ArrayList;
import java.util.List;

public class ListAllFragment extends Fragment {

    private MainActivity parentContext;

    private AbsListView mListView;
    private ListAdapter mAdapter;

    private List<ItemListItem> itemList;

    public static ListAllFragment newInstance() {
        return new ListAllFragment();
    }

    public ListAllFragment() { /* Mandatory empty constructor */ }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the parent context
        parentContext = (MainActivity) getActivity();

        // Set item list and adapter for the list view
        itemList = new ArrayList<>();
        mAdapter = new ItemListAdapter(getActivity(), itemList);

        // Default URL for downloading instruction list, as per Django
        String url = parentContext.settings.connection_url + "/list/";

        // Attempt to connect to server
        new MainActivity.Connection(parentContext, new MainActivity.Connection.TaskListener() {
            @Override
            public void onFinished(String result) {
                displayOutput(result);
            }
        }).execute(url);
    }

    public void displayOutput(String output) {
        // Clear the item list
        itemList.clear();

        try {
            // Parse the JSON instructions from the server
            Instructions.InstructionList il = new Gson().fromJson(output, Instructions.InstructionList.class);

            // Correct parsing will result in non-null object; otherwise, it will be null
            if (il != null) {
                for (Instructions.InstructionOverview o : il.objects) {
                    itemList.add(new ItemListItem(o));
                }
                ((BaseAdapter) mAdapter).notifyDataSetChanged();
            } else {
                Toast.makeText(parentContext, "Invalid JSON instructions", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(parentContext, "Could not parse JSON instructions", Toast.LENGTH_SHORT).show();
            if (output != null) Log.e("InstructionSet", output);
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);

        // I don't know how this works. It just does. Some android stuff.
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

        // When the button is pressed
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int pk = itemList.get(position).itemOverview.pk;
                String name = itemList.get(position).itemOverview.name;
                String url = parentContext.settings.connection_url + "/id" + pk + "/";

                new MainActivity.Connection(parentContext, new MainActivity.Connection.TaskListener() {
                    @Override
                    public void onFinished(String result) {
                        try {
                            Instructions instructions = new Gson().fromJson(result, Instructions.class);
                            if (instructions != null) {
                                parentContext.instructions = instructions;
                                Toast.makeText(parentContext, "Set \"" + instructions.name + "\" as instructions to run", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(parentContext, "Recieved bad instruction set", Toast.LENGTH_SHORT).show();
                                Log.e("InstructionSet", result);
                            }
                        } catch (Exception e) {
                            Toast.makeText(parentContext, "Could not parse JSON instructions", Toast.LENGTH_SHORT).show();
                            if (result != null) Log.e("InstructionSet", result);
                        }
                    }
                }).execute(url);
            }
        });

        return view;
    }


}
