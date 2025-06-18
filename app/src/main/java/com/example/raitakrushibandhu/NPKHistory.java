package com.example.raitakrushibandhu;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.raitakrushibandhu.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class NPKHistory extends AppCompatActivity {

    private Spinner spinnerTimeRange;
    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_npkhistory);

        spinnerTimeRange = findViewById(R.id.spinnerTimeRange);
        lineChart = findViewById(R.id.lineChart);

        String[] ranges = {"Today", "Last Week", "Last Month", "3 Months", "1 Year"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ranges);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeRange.setAdapter(adapter);

        spinnerTimeRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedRange = parent.getItemAtPosition(pos).toString();
                loadDataForRange(selectedRange);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadDataForRange(String range) {
        long currentTime = System.currentTimeMillis();
        long startTime = getStartTime(range, currentTime);

        FirebaseDatabase.getInstance().getReference("NPK_Values")
                .orderByKey()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<Entry> nitrogenEntries = new ArrayList<>();
                        ArrayList<Entry> phosphorusEntries = new ArrayList<>();
                        ArrayList<Entry> potassiumEntries = new ArrayList<>();

                        for (DataSnapshot data : snapshot.getChildren()) {
                            try {
                                long timestamp = Long.parseLong(data.getKey());
                                if (timestamp >= startTime) {
                                    float timeIndex = (timestamp - startTime) / 100000; // Normalize time axis

                                    float n = data.child("nitrogen").getValue(Float.class);
                                    float p = data.child("phosphorus").getValue(Float.class);
                                    float k = data.child("potassium").getValue(Float.class);

                                    nitrogenEntries.add(new Entry(timeIndex, n));
                                    phosphorusEntries.add(new Entry(timeIndex, p));
                                    potassiumEntries.add(new Entry(timeIndex, k));
                                }
                            } catch (Exception e) {
                                e.printStackTrace(); // Skip malformed data
                            }
                        }

                        showGraph(nitrogenEntries, phosphorusEntries, potassiumEntries);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }

    private void showGraph(ArrayList<Entry> nitrogen, ArrayList<Entry> phosphorus, ArrayList<Entry> potassium) {
        LineDataSet nitrogenSet = new LineDataSet(nitrogen, "Nitrogen");
        nitrogenSet.setColor(getResources().getColor(android.R.color.holo_green_dark));
        nitrogenSet.setCircleColor(getResources().getColor(android.R.color.holo_green_dark));

        LineDataSet phosphorusSet = new LineDataSet(phosphorus, "Phosphorus");
        phosphorusSet.setColor(getResources().getColor(android.R.color.holo_blue_light));
        phosphorusSet.setCircleColor(getResources().getColor(android.R.color.holo_blue_light));

        LineDataSet potassiumSet = new LineDataSet(potassium, "Potassium");
        potassiumSet.setColor(getResources().getColor(android.R.color.holo_orange_dark));
        potassiumSet.setCircleColor(getResources().getColor(android.R.color.holo_orange_dark));

        LineData lineData = new LineData(nitrogenSet, phosphorusSet, potassiumSet);

        lineChart.setData(lineData);
        lineChart.getDescription().setText("NPK Levels Over Time");
        lineChart.animateX(1000);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawLabels(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);

        lineChart.invalidate(); // refresh
    }

    private long getStartTime(String range, long currentTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTime);

        switch (range) {
            case "Today":
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                break;
            case "Last Week":
                cal.add(Calendar.DAY_OF_YEAR, -7);
                break;
            case "Last Month":
                cal.add(Calendar.MONTH, -1);
                break;
            case "3 Months":
                cal.add(Calendar.MONTH, -3);
                break;
            case "1 Year":
                cal.add(Calendar.YEAR, -1);
                break;
        }

        return cal.getTimeInMillis();
    }
}
