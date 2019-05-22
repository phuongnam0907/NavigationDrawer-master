package com.delaroystudios.navigationdrawer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.blox.treeview.BaseTreeAdapter;
import de.blox.treeview.TreeNode;
import de.blox.treeview.TreeView;

/**
 * Created by delaroy on 3/18/17.
 */
public class FileImport extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Constant constant;
    SharedPreferences app_preferences;
    int appTheme;
    int themeColor;
    int appColor;

    boolean isRunning = false;
    Handler mHandler = new Handler();

    TreeNode rootNode;
    BaseTreeAdapter adapter;

    private TextView mTextViewResult;

    private ArrayList<String> stringArrayList;
    private ArrayList<TreeNode> treeNodeArrayList;

    final String urlmap = "http://192.168.97.1/rpi3/backend/getmap.php";
    final String urlgw = "http://192.168.97.1/rpi3/backend/getgwnode.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fileimport);
        //Node Mapping Activity
        init();

        //mTextViewResult = (TextView) findViewById(R.id.node);



        TreeView treeView = findViewById(R.id.tree);

        adapter = new BaseTreeAdapter<ViewHolder>(this, R.layout.node) {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(View view) {
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(ViewHolder viewHolder, Object data, int position) {
                viewHolder.mTextView.setText(data.toString());
            }
        };
        treeView.setAdapter(adapter);

        // example tree
        rootNode = new TreeNode("  Server  ");
//        rootNode.addChild(new TreeNode("  Node 1  "));
//        final TreeNode child3 = new TreeNode("  Node 2  ");
//        child3.addChild(new TreeNode("  Node 3  "));
//        final TreeNode child6 = new TreeNode("  Node 4  ");
//        child6.addChild(new TreeNode("  Node 5  "));
//        child6.addChild(new TreeNode("  Node 6  "));
//        child3.addChild(child6);
//        rootNode.addChild(child3);
//        final TreeNode child4 = new TreeNode("  Node 7  ");
//        child4.addChild(new TreeNode("  Node 8  "));
//        child4.addChild(new TreeNode("  Node 9  "));
//        rootNode.addChild(child4);

        adapter.setRootNode(rootNode);


    }

    private Runnable loopR = new Runnable() {
        public void run() {
            try {
                isRunning = true;
                getGateway();
                //jsonParse();
                Log.d("Times","0");
                mHandler.postDelayed(loopR, 60000);
            } catch (Exception e) {
                e.printStackTrace();
                isRunning = false;
            }
        }
    };

    private void getGateway() {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlgw,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonarray = new JSONArray(response);
                            stringArrayList = new ArrayList<String>();
                            rootNode = new TreeNode("  Server  ");
                            for (int i = 0; i < jsonarray.length(); i++) {

                                JSONObject jsonobj = jsonarray.getJSONObject(i);

                                TreeNode child = new TreeNode("  Gateway " + jsonobj.getString("gateway") + "  ");
                                if (jsonobj.getString("son").equals("1")) jsonParse(child, jsonobj.getString("gateway"));
                                rootNode.addChild(child);
                                stringArrayList.add(jsonobj.getString("gateway"));
                            }
                            Log.d("length", String.valueOf(stringArrayList.size()));
                            adapter.setRootNode(rootNode);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(stringRequest);
    }


    private void init() {
        app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        appColor = app_preferences.getInt("color", 0);
        appTheme = app_preferences.getInt("theme", 0);
        themeColor = appColor;
        constant.color = appColor;

        if (themeColor == 0) {
            setTheme(Constant.theme);
        } else if (appTheme == 0) {
            setTheme(Constant.theme);
        } else {
            setTheme(appTheme);
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Constant.color);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

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
    public void onBackPressed() {

        //onStop();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.gallery) {
            Intent searchIntent = new Intent(FileImport.this, Gallery.class);
            startActivity(searchIntent);
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        } else if (id == R.id.fileimport) {
            Intent searchIntent = new Intent(FileImport.this, FileImport.class);
            startActivity(searchIntent);
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        } else if (id == R.id.slideshow) {
            Intent searchIntent = new Intent(FileImport.this, SlideShow.class);
            startActivity(searchIntent);
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        } else if (id == R.id.settings) {
            Intent searchIntent = new Intent(FileImport.this, SettingsActivity.class);
            startActivity(searchIntent);
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void jsonParse(final TreeNode treeNode1, final String gateway1) {

        RequestQueue queue = Volley.newRequestQueue(this);

        final StringBuilder s = new StringBuilder();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlmap,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonarray = new JSONArray(response);
                            loopFor(jsonarray,0,gateway1,treeNode1);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(stringRequest);

    }

    private class ViewHolder {
        TextView mTextView;
        ViewHolder(View view) {
            mTextView = view.findViewById(R.id.textView);
        }
    }

    private void loopFor(JSONArray jsonArray, int position, String gateway, TreeNode treeNode){
        try {

            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                //TreeNode child = new TreeNode();
                if ((gateway.equals(jsonObject.getString("dad"))) && (jsonObject.getString("son").equals("1"))) {
                    TreeNode child = new TreeNode("  Node " + jsonObject.getString("ip") + "  ");
                    loopFor(jsonArray, position + 1, jsonObject.getString("ip"), child);
//                    treeNode.addChild(loopFor(jsonArray, position + 1, jsonObject.getString("ip")));
                    treeNode.addChild(child);

                } else if ((gateway.equals(jsonObject.getString("dad"))) && (jsonObject.getString("son").equals("0"))){
//                    child = new TreeNode("  Node " + jsonObject.getString("ip") + "  ");
                    treeNode.addChild(new TreeNode("  Node " + jsonObject.getString("ip") + "  "));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
