package com.judgingmoloch.ftdiweb.settings;

import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Settings {
    public String connection_url = "";

    public Settings() { /* Empty constructor */ }

    // Below this line are methods to read and write the settings file

    private static final String DEFAULT_FILENAME = "my_settings.txt";

    public static Settings loadSettings(Context context) {
        Settings s;
        try {
            FileInputStream inputStream = context.openFileInput(DEFAULT_FILENAME);
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            String json = sb.toString();
            s = new Gson().fromJson(json, Settings.class);
        } catch (FileNotFoundException e) {
            s = getDefaultSettings();
            writeSettings(context, s);
            e.printStackTrace();
        } catch (IOException e) {
            s = getDefaultSettings();
            e.printStackTrace();
        }
        return s;
    }

    public static void writeSettings(Context context, Settings s) {
        FileOutputStream outputStream = null;
        try {
            outputStream = context.openFileOutput(DEFAULT_FILENAME, Context.MODE_PRIVATE);
            outputStream.write(new Gson().toJson(s).getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Settings getDefaultSettings() {
        // Could do something here if it needed to be different
        return new Settings();
    }
}
