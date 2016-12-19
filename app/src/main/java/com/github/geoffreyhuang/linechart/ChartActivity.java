package com.github.geoffreyhuang.linechart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.gistool.library.obj.GeoPoint;
import com.gistool.library.obj.GeoSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class ChartActivity extends AppCompatActivity {

    public static final String TAG = ChartActivity.class.getSimpleName();

    private LineGraphicView mLineGraphicView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        mLineGraphicView = (LineGraphicView) findViewById(R.id.line_chart);
        View rootView = findViewById(R.id.rl_root);
        if (rootView != null) {
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        organizeData(dummyGeoSection());
    }


    private GeoSection dummyGeoSection() {
        GeoSection geoSection = new GeoSection();
        for (int i = 0; i < 10; i++) {
            GeoPoint geoPoint = new GeoPoint();
            geoPoint.lat = i;
            geoPoint.lon = i;
            geoSection.appendSectionValue(geoPoint, Math.random() * 10 * i);
        }
        return geoSection;
    }

    private void organizeData(GeoSection geoSection) {
        ArrayList<Double> yData = new ArrayList<>();
        ArrayList<String> xData = new ArrayList<>();
        int maxValue;
        int pointsCount = geoSection.getPointsCount();
        for (int i = 0; i < pointsCount; i++) {
            yData.add(geoSection.getHeight(i));
            if (i == 0 || i == pointsCount - 1) {
                xData.add(String.format(Locale.CHINA, "(%.4f, %.4f)",
                        geoSection.getPoint(i).lat, geoSection.getPoint(i).lon));
            } else {
                xData.add("");
            }
            Log.d(TAG, "Height value ==> " + geoSection.getHeight(i));
        }
        maxValue = Collections.max(yData).intValue();
        mLineGraphicView.setData(yData, xData, maxValue, 6);
    }
}
