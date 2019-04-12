package com.delaroystudios.navigationdrawer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.netopen.hotbitmapgg.library.view.RingProgressBar;

/**
 * Created by delaroy on 3/18/17.
 */
public class Gallery extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    Methods methods;
    Constant constant;
    SharedPreferences.Editor editor;
    SharedPreferences app_preferences;
    int deftheme;
    int themeColor;
    int color;

    public String TAG = "YOUR CLASS NAME";
    final static String url = "http://192.168.97.1/rpi3/backend/getgw.php";

    private ViewPager mSlideViewPager;
    private LinearLayout mDotLayout;

    private TextView[] mDots;

    private SliderAdapter sliderAdapter;

    private int mCurrentPage;

    boolean isRunning = false;
    Handler mHandler = new Handler();

    String data;

    JSONArray jsonArray1;

    String currentGateway;
    String currentNode;

    int progress = 0;
    Spinner spinner;
    private ArrayList<String> stringArray;
    private ArrayList<String> stringGateway;
    private ArrayList<String> stringNode;
    TextView ipaddr;

    RingProgressBar ringProgressBar;
    ArrayList<Entry> entryArrayList;

    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        //Data show Chart Activity
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        data="";
        init();

        stringArray = new ArrayList<String>();
        stringGateway = new ArrayList<String>();
        stringNode = new ArrayList<String>();

        jsonArray1 = new JSONArray();

        spinner = findViewById(R.id.spinnerL);
        ipaddr = findViewById(R.id.ip_add);
        ringProgressBar = findViewById(R.id.progBar);
        loadSpinnerData(url);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String country = spinner.getItemAtPosition(spinner.getSelectedItemPosition()).toString();
                waiting(country);
//                Toast.makeText(getApplicationContext(),"Wait ...",Toast.LENGTH_LONG).show();
//                Toast.makeText(getApplicationContext(),country,Toast.LENGTH_LONG).show();
                currentGateway = stringGateway.get(spinner.getSelectedItemPosition());
                currentNode = stringNode.get(spinner.getSelectedItemPosition());
//                ipaddr.setText(country);
                ringProgressBar.setVisibility(View.VISIBLE);

                //setJSON(urlS);
                //Log.d("json", String.valueOf(jsonArray1.length()));
                //Log.d("data",data);

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        mSlideViewPager = (ViewPager) findViewById(R.id.slideViewPager);
        mDotLayout = (LinearLayout) findViewById(R.id.dotsLayout);

        sliderAdapter = new SliderAdapter(Gallery.this);

        mSlideViewPager.setAdapter(sliderAdapter);

        addDotsIndicator(0);

        mSlideViewPager.addOnPageChangeListener(viewListener);
    }

    void waiting(final String c){
        new Thread(new Runnable() {
            @Override
            public void run() {
                setJSON();
                for (int i = 0; i < 100; i++){
                    try {
                        Thread.sleep(15);
                        handler.sendEmptyMessage(0);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),c,Toast.LENGTH_SHORT).show();
                ringProgressBar.setVisibility(View.GONE);
                progress = 0;
                ipaddr.setText(c);
            }

        }, 1500);
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                if (progress < 100){
                    progress++;
                    ringProgressBar.setProgress(progress);
                }
            }
        }
    };

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private void loadSpinnerData(String url) {
//        RequestQueue requestQueue=Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray=new JSONArray(response);
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        stringArray.add("Gateway " + jsonObject1.getString("Gateway") + " - Node " + jsonObject1.getString("Node"));
                        stringGateway.add(jsonObject1.getString("Gateway"));
                        stringNode.add(jsonObject1.getString("Node"));
                    }
                    spinner.setAdapter(new ArrayAdapter<String>(Gallery.this, android.R.layout.simple_spinner_dropdown_item, stringArray));
                }catch (JSONException e){e.printStackTrace();}
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        requestQueue.add(stringRequest);
    }

    private void setJSON(){
//        RequestQueue requestQueue=Volley.newRequestQueue(getApplicationContext());
        String urlS = "http://192.168.97.1/rpi3/backend/getdata.php?gw=" + currentGateway + "&id=" + currentNode;
        StringRequest stringRequest=new StringRequest(Request.Method.GET, urlS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    jsonArray1 = jsonArray;
//                    Intent intent = new Intent(Gallery.this,SliderAdapter.class);
//                    intent.putExtra("json",String.valueOf(jsonArray1.length()));
//                    startActivity(intent);
//                    Log.d("jsonFi", String.valueOf(jsonArray1.length()));
                }catch (JSONException e){e.printStackTrace();}
                Log.d("jsonSe", String.valueOf(jsonArray1.length()));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        requestQueue.add(stringRequest);
    }

    private void init(){
        app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        color = app_preferences.getInt("color", 0);
        deftheme = app_preferences.getInt("theme", 0);
        themeColor = color;
        constant.color = color;

        if (themeColor == 0){
            setTheme(Constant.theme);
        }else if (deftheme == 0){
            setTheme(Constant.theme);
        }else{
            setTheme(deftheme);
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Constant.color);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.nav_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == R.id.action_settings){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onNavigationItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == R.id.gallery){
            Intent searchIntent = new Intent(Gallery.this, MainActivity.class);
            startActivity(searchIntent);
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        }else if(id == R.id.fileimport){
            Intent searchIntent = new Intent(Gallery.this, FileImport.class);
            startActivity(searchIntent);
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        }else if(id == R.id.slideshow){
            Intent searchIntent = new Intent(Gallery.this, SlideShow.class);
            startActivity(searchIntent);
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        }else if(id == R.id.settings) {
            Intent searchIntent = new Intent(Gallery.this, SettingsActivity.class);
            startActivity(searchIntent);
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void addDotsIndicator(int position){
        mDots = new TextView[7];
        mDotLayout.removeAllViews();

        for (int i = 0; i < mDots.length; i++){
            mDots[i] = new TextView(this);
            mDots[i].setText(Html.fromHtml("&#8226 "));
            mDots[i].setTextSize(28);
            mDots[i].setTextColor(getResources().getColor(R.color.colorGrey));

            mDotLayout.addView(mDots[i]);
        }

        if (mDots.length > 0){
            mDots[position].setTextSize(33);
            mDots[position].setTextColor(getResources().getColor(R.color.colorWhite));
        }
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            addDotsIndicator(i);

            mCurrentPage = i;

            Log.d("Current Page",String.valueOf(i));
            Log.d("SizePage",String.valueOf(jsonArray1.length()));
            switch (i) {
                case 0:
                    setJSON();
                    break;
                case 1:
                    drawChart("phValue");
                    break;
                case 2:
                    drawChart("tempValue");
                    break;
                case 3:
                    drawChart("liqValue");
                    break;
                case 4:
                    drawChart("doValue");
                    break;
                case 5:
                    drawChart("tdsValue");
                    break;
                case 6:
                    drawChart("orpValue");
                    break;
                default:
                    setJSON();
                    break;
            }

        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    private Runnable loopR = new Runnable() {
        public void run() {
            try {
                isRunning = true;
                setJSON();
                mHandler.postDelayed(loopR, 5000);
            } catch (Exception e) {
                e.printStackTrace();
                isRunning = false;
            }
        }
    };

    @Override
    protected void onStart() {
        if (isRunning == false){
            loopR.run();
        }
        super.onStart();
    }


    @Override
    protected void onStop() {
        mHandler.removeCallbacks(loopR);
        mHandler.removeCallbacksAndMessages(null);
        isRunning = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void drawChart(String valueS) {
        entryArrayList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray1.length(); i++) {
                JSONObject jsonObject1 = jsonArray1.getJSONObject(i);


                Float value = Float.valueOf(jsonObject1.getString(valueS));
                int date = Integer.parseInt(jsonObject1.getString("time"));
                Log.d(valueS, String.valueOf(value));
                entryArrayList.add(new Entry(value,date));

//                Log.d(valueS,jsonObject.getString(valueS));

            }
            Log.d("Size array", String.valueOf(entryArrayList.size()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
