package com.judgingmoloch.ftdiweb;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.judgingmoloch.ftdiweb.connection.Instructions;
import com.judgingmoloch.ftdiweb.connection.ListAllFragment;
import com.judgingmoloch.ftdiweb.driver.AndroidDriver;
import com.judgingmoloch.ftdiweb.settings.Settings;
import com.judgingmoloch.ftdiweb.settings.SettingsFragment;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private CharSequence mTitle;
    public D2xxManager d2xxManager;

    private static Fragment currentFragment = null;

    public D2xxManager getDeviceManager() {
        return d2xxManager;
    }

    public Settings settings;
    public Instructions instructions;
    public AndroidDriver driver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //graph something
        double[] xvals = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] yvals = {2.4, 3.2, 4.4, 2.5, 3.5};

        GraphView graph = (GraphView) findViewById(R.id.graph);

        DataPoint[] dataPoints = new DataPoint[xvals.length];
        for (int i = 0; i < xvals.length; i++) {
            dataPoints[i] = new DataPoint(xvals[i], yvals[i]);
        }

        LineGraphSeries<DataPoint> myLineGraphSeries = new LineGraphSeries<>(dataPoints);

        graph.addSeries(myLineGraphSeries);

        // Turn off strict mode
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        settings = Settings.loadSettings(this);

        // Instantiate the D2xxManager for the whole project
        try {
            d2xxManager = D2xxManager.getInstance(this);
        } catch (D2xxManager.D2xxException e) {
            e.printStackTrace();
        }

        driver = new AndroidDriver(this);

        // These are the default instructions; new ones should be downloaded
        instructions = new Instructions("Nothing", "No instructions set", "", 0);

        // Instantiate navigation drawer fragment (for the sidebar) and get the project title
        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.setPriority(500);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        Fragment newFragment;

        switch (position) {
            case 0:
                newFragment = ItemFragment.newInstance();
                break;
            case 1:
                newFragment = ListAllFragment.newInstance();
                break;
            case 2:
                newFragment = RunCodeFragment.newInstance();
                break;
            case 3:
                newFragment = RunProgramFragment.newInstance();
                break;
            default:
                newFragment = ItemFragment.newInstance();
        }

        ft.replace(R.id.container, newFragment).commit();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    public void settings() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, SettingsFragment.newInstance()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            settings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();

        try {
            // Relevant callbacks to run when a USB device is connected or disconnected
            if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                if (currentFragment instanceof ItemFragment) {
                    ((ItemFragment) currentFragment).update();
                } else if (currentFragment instanceof RunCodeFragment) {
                    ((RunCodeFragment) currentFragment).createDeviceList();
                }
            } else if (action.equals(UsbManager.ACTION_USB_ACCESSORY_DETACHED)) {
                if (currentFragment instanceof RunCodeFragment) {
                    ((RunCodeFragment) currentFragment).disconnect();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Interface for dealing with JSON requests to and from a server
     * Used various times throughout the project
     */
    public static class Connection extends AsyncTask<String, String, Void> {
        public interface TaskListener {
            void onFinished(String result);
        }

        private final TaskListener taskListener;
        private ProgressDialog progressDialog;
        InputStream inputStream = null;
        String result = "";
        Context context;

        public Connection(Context ctx, TaskListener listener) {
            this.taskListener = listener;
            context = ctx;
            progressDialog = new ProgressDialog(ctx);
        }

        protected boolean isNetworkConnected() {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return (cm.getActiveNetworkInfo() != null);
        }

        protected void onPreExecute() {
            // Check if the device is connected to the internet. If not, alert the user
            if (!isNetworkConnected()) {
                Toast.makeText(context, "Cannot connect to internet", Toast.LENGTH_SHORT).show();
                Connection.this.cancel(true);
                return;
            } else {
                progressDialog.setMessage("Downloading data...");
            }
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Connection.this.cancel(true);
                }
            });
        }

        @Override
        protected Void doInBackground(String... params) {
            String url_select = params[0]; // parentContext.settings.connection_url;

            List<NameValuePair> param = new ArrayList<>();

            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url_select);
                httpPost.setEntity(new UrlEncodedFormEntity(param));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();

                inputStream = httpEntity.getContent();

                BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
                StringBuilder sBuilder = new StringBuilder();

                String line;
                while ((line = bReader.readLine()) != null) {
                    sBuilder.append(line);
                }

                inputStream.close();
                result = sBuilder.toString();
            } catch (Exception e) {
                Connection.this.cancel(true);
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(Void v) {
            taskListener.onFinished(result);
            this.progressDialog.dismiss();
        }
    }

}
