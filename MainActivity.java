package com.plaiydmn.wifirouterdistace;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TEST";
    private WifiManager wifiManager;
    private TextView tview;
    private TextView tViewTime;
    private ProgressBar progressBar;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private ListView listView;
    private final List<String> dataList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tview = findViewById(R.id.tview);
        tViewTime = findViewById(R.id.tViewTime);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        tViewTime.setText(sdf.format(new Date()));
        listView = findViewById(R.id.listView);

        dataList.add("test");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);

        listView.setAdapter(adapter);

        tview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                measure();
                listView.invalidate();
            }
        });

        listView.invalidate();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            measure();
        }else{
            tview.setText("No permission!");
        }
    }

    private void measure() {
        wifiManager = (WifiManager)
                getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        progressBar.setVisibility(View.VISIBLE);

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                } else {
                    scanFailure();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);

        boolean success = wifiManager.startScan();
        if (!success) {
            scanFailure();
        }
    }

    private void scanSuccess() {
        List<ScanResult> results = wifiManager.getScanResults();
        WifiInfo info = wifiManager.getConnectionInfo ();
        String ssid  = info.getSSID();
        progressBar.setVisibility(View.VISIBLE);
        dataList.clear();

        for (ScanResult s : results){
            DecimalFormat df = new DecimalFormat("#.##");
            String data = s.SSID+ "\n" + s.BSSID + ": " + s.level + ", Distance: " +
                    df.format(calculateDistance((double)s.level, s.frequency)) + "m";
            if(ssid.substring(1,ssid.length()-1).equals(s.SSID))
            tview.setText(data);

            dataList.add(data);
            Log.d(TAG, data);
            adapter.notifyDataSetChanged();
        }
        tViewTime.setText(sdf.format(new Date()));
        listView.invalidate();
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void scanFailure() {
        tview.setText("WiFi Manager Scan Failure!");
    }

    private double calculateDistance(double levelInDb, double freqInMHz)    {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(levelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }
}
