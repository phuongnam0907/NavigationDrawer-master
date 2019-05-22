package com.delaroystudios.navigationdrawer;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SliderAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;

    private LineChart lineChart;
    private LineData lineData;
    ArrayList<Entry> entryArrayList;

    TextView slideData;

    String currentState;

    JSONArray jsonArrayA = new JSONArray();

    String currentData;

    public SliderAdapter(Context context){
        this.context = context;
    }
    public void setDataOnGraph(JSONArray jsonArray){
        Log.d("rep", String.valueOf(jsonArray.length()));
        this.jsonArrayA = jsonArray;
        Log.d("repA", String.valueOf(jsonArrayA.length()));
    }

    public int[] slide_images = {
            R.drawable.ph,
            R.drawable.temperature,
            R.drawable.li,
            R.drawable.dos,
            R.drawable.tds,
            R.drawable.orp
    };

    public String[] slide_headings = {
            "pH Data",
            "Temperature Data",
            "Liquid Water Level Data",
            "Dissolved Oxygen Data",
            "Total Dissolved Solids Data",
            "Oxidation Reduction Potential Data"
    };

    public String[] currency = {
            "",
            String.valueOf(Html.fromHtml("&#8451")),
            "%",
            "%",
            " ppm",
            ""
    };

    @Override
    public int getCount() {
        return slide_headings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == (RelativeLayout) o;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slide_layout, container, false);

        ImageView slideImageView = view.findViewById(R.id.slide_image);
        TextView slideHeading = view.findViewById(R.id.slide_heading);
        slideData = view.findViewById(R.id.slide_data);

        lineChart = view.findViewById(R.id.chart);
        lineData = new LineData(getLineDataValues(position));
        lineData.setValueTextColor(Color.WHITE);
        lineData.setValueTextSize(9f);
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragDecelerationFrictionCoef(0.9f);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setHighlightPerDragEnabled(true);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setLabelRotationAngle(300);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                Date d = new Date(Float.valueOf(value*1000).longValue());
                String date = new SimpleDateFormat("HH:mm - dd/MM").format(d);
                return date;
            }
        });
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true);
        xAxis.setCenterAxisLabels(true);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setSpaceTop(30);
        leftAxis.setSpaceBottom(30);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setTextColor(Color.WHITE);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        lineChart.setData(lineData);

        slideImageView.setImageResource(slide_images[position]);
        slideHeading.setText(slide_headings[position]);
        slideData.setText(currentData + currency[position]);
        //currentState = slide_headings[position];

        container.addView(view);

        return view;
    };

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout)object);
    }

    private List<ILineDataSet> getLineDataValues(int position) {

        switch (position){
            case 0:
                currentState = "phValue";
                break;
            case 1:
                currentState = "tempValue";
                break;
            case 2:
                currentState = "liqValue";
                break;
            case 3:
                currentState = "doValue";
                break;
            case 4:
                currentState = "tdsValue";
                break;
            case 5:
                currentState = "orpValue";
                break;
            default:
                break;
        }

        ArrayList<ILineDataSet> lineDataSets = null;

        ArrayList<Entry> entryArrayList = new ArrayList<>();

        try {
            if (jsonArrayA.length() == 0) entryArrayList.add(new Entry(0,0f));
            else {
                for (int i = 0; i < jsonArrayA.length(); i++) {
                    JSONObject jsonObject = jsonArrayA.getJSONObject(i);
                    entryArrayList.add(new Entry(Float.valueOf(jsonObject.getString("time")), Float.valueOf(jsonObject.getString(currentState))));
                    if (i == jsonArrayA.length()-1) currentData = jsonObject.getString(currentState);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        LineDataSet lineDataSet = new LineDataSet(entryArrayList,currentState);

        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setColor(ColorTemplate.getHoloBlue());
        lineDataSet.setValueTextColor(ColorTemplate.getHoloBlue());
        lineDataSet.setLineWidth(2f);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawValues(true);
        lineDataSet.setFillColor(ColorTemplate.getHoloBlue());
        lineDataSet.setHighLightColor(Color.rgb(244, 117, 117));
        lineDataSet.setDrawCircleHole(false);

        lineDataSets = new ArrayList<>();
        lineDataSets.add(lineDataSet);

        return lineDataSets;
    }


}
