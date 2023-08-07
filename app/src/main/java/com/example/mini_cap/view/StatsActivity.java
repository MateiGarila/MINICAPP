package com.example.mini_cap.view;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mini_cap.R;
import com.example.mini_cap.controller.DBHelper;
import com.example.mini_cap.controller.SensorController;
import com.example.mini_cap.model.Day;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import app.uvtracker.data.optical.OpticalRecord;
import app.uvtracker.sensor.pii.connection.application.event.NewSampleReceivedEvent;
import app.uvtracker.sensor.pii.connection.application.event.SyncDataReceivedEvent;
import app.uvtracker.sensor.pii.connection.application.event.SyncProgressChangedEvent;
import app.uvtracker.sensor.pii.event.EventHandler;
import app.uvtracker.sensor.pii.event.IEventListener;

public class  StatsActivity extends AppCompatActivity implements IEventListener {

    // If set to true, the graph will progressively update everytime it receives sync data.
    // If set to false, the graph will only update once the entire sync session is completed.
    // This only impacts initial synchronization (when the user connects to a sensor with a full database)
    private static final boolean PROGRESSIVE_REFRESH = false;

    // These configures the hour range to display (inclusive)
    private static final int HOURS_START = 6;
    private static final int HOURS_END = 23;

    // Activity state - chart control
    private LocalDate today;
    private boolean viewingMinutes;
    private String selectedDate;
    private int selectedHour;
    private int selectedWeek;
    private String[] dayOfWeekStrings;
    private int selectedDayOfWeekButtonIndex;

    // UI components - chart displays
    private LineChart lineChart;
    private Button[] dayOfWeekButtons;
    private TextView selectedDateTextView;

    // UI components - RT data displays
    private TextView currentUVTextView;
    private TextView currentALSTextView;
    private TextView currentSeverityTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_stats);

        // Register event handlers
        SensorController.get(this).registerListenerClass(this);

        // Initialize common constants
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMMM d' 'yyyy");
        DateTimeFormatter outputFormatterForDB = DateTimeFormatter.ofPattern("dd-MM-yyyy");


        /* -------- Initialize activity state -------- */
        this.today = LocalDate.now();
        this.viewingMinutes = false;
        this.selectedDate = this.today.format(outputFormatterForDB);
        this.selectedHour = 0;
        this.selectedWeek = 0;
        this.selectedDayOfWeekButtonIndex = -1;


        /* -------- Initialize UI components - day of week menu -------- */

        // Day of week menu - prev/next buttons
        this.findViewById(R.id.prev_week_button).setOnClickListener(v -> this.onWeekSelectionButtonClick(-1));
        this.findViewById(R.id.next_week_button).setOnClickListener(v -> this.onWeekSelectionButtonClick(+1));

        // Day of week menu - day of week buttons
        int[] buttonID = {
                R.id.day_1, R.id.day_2, R.id.day_3, R.id.day_4, R.id.day_5, R.id.day_6, R.id.day_7
        };
        this.dayOfWeekButtons =
                IntStream.range(0, buttonID.length)
                        .mapToObj(i -> new Pair<>(i, (Button)findViewById(buttonID[i])))
                        .map(p -> {
                            p.second.setOnClickListener(e ->
                                    this.onDayOfWeekButtonClick(p.first));
                            return p.second;
                        })
                        .toArray(Button[]::new);
        this.updateDayOfWeekButtons();

        // Current selected date text
        this.selectedDateTextView = this.findViewById(R.id.date_text_view);
        this.selectedDateTextView.setText(this.today.format(outputFormatter));


        /* -------- Initialize UI components - chart displays -------- */

        // Line chart
        this.lineChart = this.findViewById(R.id.line_chart);
        this.lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                StatsActivity.this.onLineChartValueSelected(e, h);
            }
            @Override
            public void onNothingSelected() {
                // Now that we have the back button, we probably no longer need this
                // StatsActivity.this.onLineChartValueDeselected();
            }
        });
        this.lineChart.setPinchZoom(false); // Disable pinch zoom
        this.lineChart.setDoubleTapToZoomEnabled(false); // Disable double-tap zoom
        this.onDayOfWeekButtonClick(this.today.getDayOfWeek().getValue() % 7); // Emulate a button click + refresh


        /* -------- Initialize UI components - real-time data displays -------- */
        this.currentUVTextView = this.findViewById(R.id.uvindex_id);
        this.currentALSTextView = this.findViewById(R.id.lightIntensityID);
        this.currentSeverityTextView = this.findViewById(R.id.severity_index);
        float defaultUVValue = 4.0F;
        float defaultIlluminance = 1100.0F;
        this.setCurrentUVIndex(defaultUVValue);
        this.setLightIntensity(defaultIlluminance);
        this.setSeverity(defaultUVValue);
    }


    /* -------- Day of week menu methods -------- */

    // Event handler
    public void onWeekSelectionButtonClick(int weekOffset) {
        this.selectedWeek += weekOffset;
        this.updateDayOfWeekButtons();
        if(this.selectedDayOfWeekButtonIndex != -1) {
            String selectedDate = this.dayOfWeekStrings[this.selectedDayOfWeekButtonIndex];
            this.renderDailyDetailChart(selectedDate);
        }
    }

    // Event handler
    private void onDayOfWeekButtonClick(int selectedIndex) {
        // Restore old button color
        if(this.selectedDayOfWeekButtonIndex != -1)
            this.dayOfWeekButtons[this.selectedDayOfWeekButtonIndex].setBackgroundColor(ContextCompat.getColor(this, R.color.button_grey));
        // Set new button color
        this.dayOfWeekButtons[selectedIndex].setBackgroundColor(ContextCompat.getColor(this, R.color.item_selected_color));
        // Update internal state
        this.selectedDayOfWeekButtonIndex = selectedIndex;
        // UI updates
        this.selectedDate = this.dayOfWeekStrings[selectedIndex];
        this.renderDailyDetailChart(this.selectedDate);
    }

    // Rendering
    private void updateDayOfWeekButtons() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        // Find the Sunday of the current week
        LocalDate sunday = this.today.with(DayOfWeek.SUNDAY);

        // Add the number of weeks based on the currentWeek value
        LocalDate firstDayOfWeek = sunday.plusWeeks(this.selectedWeek);

        // Add the dates of the week to the ArrayList
        this.dayOfWeekStrings =
                IntStream.range(0, 7)
                        .mapToObj(i -> firstDayOfWeek.plusDays(i).format(formatter))
                        .toArray(String[]::new);

        // Set the text of each button to the corresponding day
        for (int i = 0; i < this.dayOfWeekButtons.length; i++) {
            String[] parts = this.dayOfWeekStrings[i].split("-");
            int dayOfMonth = Integer.parseInt(parts[0]);
            this.dayOfWeekButtons[i].setText(String.valueOf(dayOfMonth));
        }
    }


    /* -------- Line chart methods -------- */

    // Event handler
    public void onLineChartValueSelected(Entry e, Highlight ignoredH) {
        if(this.viewingMinutes) return;
        if(e.getData() != null && e.getData().equals(Boolean.TRUE)) return;
        this.selectedHour = Math.round(e.getX()) + HOURS_START;
        this.refreshLineChart(true);
    }

    // Event handler
    public void onLineChartValueDeselected() {
        if(!this.viewingMinutes) return;
        this.refreshLineChart(false);
    }


    // Event handler
    @Override
    public void onBackPressed() {
        if(!this.viewingMinutes) super.onBackPressed();
        this.refreshLineChart(false);
    }

    // Rendering
    public void renderDailyDetailChart(String selectedDate){
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate date = LocalDate.parse(selectedDate, inputFormatter);
        Day day = new Day(date);

        DBHelper dbHelper = DBHelper.get(this);

        // Generate x-axis values
        List<String> xAxisValues =
                IntStream.range(0, HOURS_END - HOURS_START + 1)
                        .mapToObj(i -> generateHourText(HOURS_START + i))
                        .collect(Collectors.toList());

        // Generate y-axis values
        List<Entry> yAxisValues =
                IntStream.range(0, HOURS_END - HOURS_START + 1)
                        .mapToObj(i -> {
                            float val = dbHelper.getHourlyAvg(day, HOURS_START + i);
                            if(Float.isNaN(val)) return null;
                            return new Entry(i, val);
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        List<Entry> yAxisValuePlaceholder = new ArrayList<>(2);
        yAxisValuePlaceholder.add(new Entry(0, 0, true));
        yAxisValuePlaceholder.add(new Entry(HOURS_END - HOURS_START, 0, true));

        // Generate datasets
        LineDataSet dataSet = new LineDataSet(yAxisValues, "");
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER); // Use different modes for the line appearance
        dataSet.setDrawValues(false); // Set to false to hide the values on data points
        dataSet.setLineWidth(4f); // Set line width to 4
        dataSet.setCircleRadius(8f);
        LineDataSet dataSetPlaceholder = new LineDataSet(yAxisValuePlaceholder, "");
        dataSetPlaceholder.setVisible(false);

        // Set line chart style
        this.lineChart.setDrawGridBackground(true);
        this.lineChart.setGridBackgroundColor(Color.TRANSPARENT); // Transparent background color
        this.lineChart.getDescription().setEnabled(false); // Hide the description label
        this.lineChart.getLegend().setEnabled(false); // Hide the dataset label in the legend

        // Set x-axis style and label
        XAxis xAxis = this.lineChart.getXAxis();
        xAxis.setGridColor(Color.TRANSPARENT); // Transparent grid lines
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisValues));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Move X-axis to the bottom
        xAxis.setTextSize(12f);
        xAxis.setDrawAxisLine(false);

        // Set y-axis style (left)
        /*
        YAxis leftYAxis = this.lineChart.getAxisLeft();
        leftYAxis.setAxisMinimum(0f); // Set the minimum value of the y-axis to 0
        leftYAxis.setAxisMaximum(12f); // Set the maximum value of the y-axis to 12
        leftYAxis.setGranularityEnabled(true);
        leftYAxis.setDrawLabels(false); // Do not draw Y-axis values on the left side
        leftYAxis.setGridColor(Color.TRANSPARENT); // Transparent grid lines on the left side
        */

        // Set y-axis style (right)
        YAxis rightYAxis = this.lineChart.getAxisRight();
        rightYAxis.setDrawLabels(false); // Do not draw Y-axis values on the left side
        rightYAxis.setGridColor(Color.TRANSPARENT);

        // Refresh
        this.viewingMinutes = false;
        this.lineChart.setData(new LineData(dataSet, dataSetPlaceholder));
        this.lineChart.invalidate();
        this.selectedDateTextView.setText(generateDateText(date));
    }

    // Rendering
    public void renderHourlyDetailChart(String selectedDate, int hour){
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate date = LocalDate.parse(selectedDate, inputFormatter);
        Day day = new Day(date);

        DBHelper dbHelper = DBHelper.get(this);

        // Generate x-axis values
        List<String> xAxisValues = IntStream.range(0, 61)
                .mapToObj(i ->
                        i < 60 ? String.format(Locale.getDefault(), "%d:%02d", hour, i)
                                : String.format(Locale.getDefault(), "%d:00", hour + 1)
                )
                .collect(Collectors.toList());

        // Generate y-axis values
        List<Entry> yAxisValues = IntStream.range(0, 61)
                .mapToObj(i -> {
                    float val = dbHelper.getMinuteAvg(day, i, hour);
                    if(Float.isNaN(val)) return null;
                    return new Entry(i, val);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Entry> yAxisValuePlaceholder = new ArrayList<>(2);
        yAxisValuePlaceholder.add(new Entry(0, 0, true));
        yAxisValuePlaceholder.add(new Entry(60, 0, true));

        // Generate datasets
        LineDataSet dataSet = new LineDataSet(yAxisValues, "");
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER); // Use different modes for the line appearance
        dataSet.setDrawValues(false); // Set to false to hide the values on data points
        dataSet.setLineWidth(6f); // Set line width to 6
        dataSet.setDrawCircles(false); // Set to false to hide circles at data points
        dataSet.setColor(ContextCompat.getColor(this, R.color.stats_color));
        LineDataSet dataSetPlaceholder = new LineDataSet(yAxisValuePlaceholder, "");
        dataSetPlaceholder.setVisible(false);

        // Set line chart style
        lineChart.setDrawGridBackground(true);
        lineChart.setGridBackgroundColor(Color.TRANSPARENT); // Transparent background color
        this.lineChart.getDescription().setEnabled(false);  // Hide the description label
        this.lineChart.getLegend().setEnabled(false); // Hide the dataset label in the legend

        // Set x-axis style and label
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGridColor(Color.TRANSPARENT); // Transparent grid lines
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisValues));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Move X-axis to the bottom
        xAxis.setTextSize(12f);
        xAxis.setDrawAxisLine(false);

        // Set y-axis style (left)
        /*
        YAxis leftYAxis = this.lineChart.getAxisLeft();
        leftYAxis.setAxisMinimum(0f); // Set the minimum value of the y-axis to 0
        leftYAxis.setAxisMaximum(12f); // Set the maximum value of the y-axis to 12
        */

        // Set y-axis style (right)
        YAxis rightYAxis = this.lineChart.getAxisRight();
        rightYAxis.setDrawLabels(false); // Do not draw Y-axis values on the left side
        rightYAxis.setGridColor(Color.TRANSPARENT);

        // Refresh
        this.viewingMinutes = true;
        this.lineChart.setData(new LineData(dataSet, dataSetPlaceholder));
        this.lineChart.invalidate();
        this.selectedDateTextView.setText(generateDateText(date) + ", " + generateHourText(hour));
    }

    // Helper
    public void refreshLineChart() {
        this.refreshLineChart(this.viewingMinutes);
    }

    public void refreshLineChart(boolean displayMinuteView) {
        if(displayMinuteView) {
            this.renderHourlyDetailChart(this.selectedDate, this.selectedHour);
        }
        else {
            this.renderDailyDetailChart(this.selectedDate);
        }
    }


    /* -------- Text field methods -------- */

    // Rendering
    public void setCurrentUVIndex(float uvValue){
        String str= "Current UV Index: " + uvValue;
        this.currentUVTextView.setText(str);
    }

    // Rendering
    public void setLightIntensity(float lightIntensity){
        String str= "Light Intensity: " + lightIntensity + " lux";
        this.currentALSTextView.setText(str);
    }

    // Rendering
    public void setSeverity(float uvIndex){
        String uvIndexText;
        int textColor;

        if (uvIndex >= 0 && uvIndex <= 2) {
            uvIndexText = "Low";
            textColor = Color.GREEN;
        } else if (uvIndex >= 3 && uvIndex <= 7) {
            uvIndexText = "Moderate";
            textColor = Color.parseColor("#FFA500");
        } else {
            uvIndexText = "High";
            textColor = Color.RED;
        }

        this.currentSeverityTextView.setText(uvIndexText);
        this.currentSeverityTextView.setTextColor(textColor);
    }


    /* -------- External events -------- */

    @EventHandler
    protected void onNewSample(@NonNull NewSampleReceivedEvent event) {
        OpticalRecord record = event.getRecord();
        this.setCurrentUVIndex(record.uvIndex);
        this.setLightIntensity(record.illuminance);
        this.setSeverity(record.uvIndex);
    }

    @EventHandler
    protected void onSyncStateChange(@NonNull SyncProgressChangedEvent event) {
        if (event.getStage() != SyncProgressChangedEvent.Stage.DONE) return;
        if(!PROGRESSIVE_REFRESH)
            new Handler(Looper.getMainLooper()).post(this::refreshLineChart);
    }

    @EventHandler
    protected void onSyncDataReceived(@NonNull SyncDataReceivedEvent event) {
        if(PROGRESSIVE_REFRESH)
            new Handler(Looper.getMainLooper()).post(this::refreshLineChart);
    }


    /* -------- Static helper methods -------- */

    public static String generateDateText(LocalDate date) {
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMMM d' 'yyyy");
        return date.format(outputFormatter);
    }

    public static String generateHourText(int h24) {
        String disp12 = h24 >= 12 ? "PM" : "AM";
        int h12 = h24 > 12 ? h24 - 12 : h24;
        disp12 = h12 + disp12;
        return disp12;
    }

}
