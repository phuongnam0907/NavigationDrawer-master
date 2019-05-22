package com.delaroystudios.navigationdrawer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.turkialkhateeb.materialcolorpicker.ColorChooserDialog;
import com.turkialkhateeb.materialcolorpicker.ColorListener;

import java.util.List;

/**
 * Created by delaroy on 3/21/17.
 */
public class SettingsActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences, app_preferences;
    SharedPreferences.Editor editor;
    Button button;
    Methods methods;

    Button wifiBtn;
    Button extBtn;

    int appTheme;
    int themeColor;
    int appColor;
    Constant constant;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //Settings Activity
        init();

        wifiBtn = (Button) findViewById(R.id.button_wifi);
        wifiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ssid = wifiInfo.getSSID();
                Log.d("SSID", ssid);
                if (ssid.equals("\"localhost2067\"")) Toast.makeText(getApplicationContext(),"Connected Station wifi!!!",Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(getApplicationContext(),"Pleasess connect to \"localhost2067\" wifi!!!",Toast.LENGTH_SHORT).show();
                    connectWifi();
                }

            }
        });

        extBtn = (Button) findViewById(R.id.button_cancel);
        extBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                //Toast.makeText(getApplicationContext(),"Please connect to \"localhost2067\" wifi!!!",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void init(){
        app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        appColor = app_preferences.getInt("color", 0);
        appTheme = app_preferences.getInt("theme", 0);
        themeColor = appColor;
        constant.color = appColor;

        if (themeColor == 0){
            setTheme(Constant.theme);
        }else if (appTheme == 0){
            setTheme(Constant.theme);
        }else{
            setTheme(appTheme);
        }



        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_setting);
        toolbar.setTitle("Settings");
        toolbar.setBackgroundColor(Constant.color);

        methods = new Methods();
        button = (Button) findViewById(R.id.button_color);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        colorize();

        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                ColorChooserDialog dialog = new ColorChooserDialog(SettingsActivity.this);
                dialog.setTitle("Select");
                dialog.setColorListener(new ColorListener() {
                    @Override
                    public void OnColorClick(View v, int color) {
                        colorize();
                        Constant.color = color;

                        methods.setColorTheme();
                        editor.putInt("color", color);
                        editor.putInt("theme",Constant.theme);
                        editor.commit();

                        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });

                dialog.show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void colorize(){
        ShapeDrawable d = new ShapeDrawable(new OvalShape());
        d.setBounds(58, 58, 58, 58);

        d.getPaint().setStyle(Paint.Style.FILL);
        d.getPaint().setColor(Constant.color);

        button.setBackground(d);
    }

    private void connectWifi(){
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        List<ScanResult> wifiScanList = wifi.getScanResults();
        String[] wifis = new String[wifiScanList.size()];

        for(int i = 0; i < wifiScanList.size(); i++){
            wifis[i] = ((wifiScanList.get(i)).SSID);

            if(wifis[i].equals("localhost2067")) {

                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = String.format("\"%s\"", wifis[i]);
                wifiConfig.preSharedKey = String.format("\"%s\"", "11223344");

                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                //remember id
                int netId = wifiManager.addNetwork(wifiConfig);
                wifiManager.disconnect();
                wifiManager.enableNetwork(netId, true);
                wifiManager.reconnect();
                Toast.makeText(getApplicationContext(),"Connected Station wifi!!!",Toast.LENGTH_SHORT).show();
            } else Toast.makeText(getApplicationContext(),"Cannot connect station wifi",Toast.LENGTH_SHORT).show();
        }
    }
}
