package com.example.mini_cap.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.widget.Button;
import android.widget.Switch;
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
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
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
    private static final int HOURS_START = 0;
    private static final int HOURS_END = 23;

    // System
    private Handler handler;

    // Activity state - chart control
    private LocalDate today;
    private boolean viewingMinutes;
    private String selectedDate;
    private int selectedHour;
    private int selectedWeek;
    private String[] dayOfWeekStrings;
    private int selectedDayOfWeekButtonIndex;
    private boolean viewingALS;

    // UI components - chart displays
    private LineChart lineChart;
    private Button[] dayOfWeekButtons;
    private TextView selectedDateTextView;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch dataContentSwitch;

    // UI components - RT data displays
    private TextView currentUVTextView;
    private TextView currentALSTextView;
    private TextView currentSeverityTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_stats);

        if(this.handler == null)
            this.handler = new Handler(Looper.getMainLooper());

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


        /* -------- Initialize UI components - content display switch -------- */
        this.dataContentSwitch = this.findViewById(R.id.stat_display_switch);
        this.dataContentSwitch.setChecked(false);
        this.dataContentSwitch.setOnClickListener(e -> this.onDataContentSwitchToggle());
        this.updateDataContentSwitch();


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
        if(weekOffset == 0) return;
        this.selectedWeek += weekOffset;
        this.updateDayOfWeekButtons();
        if(weekOffset > 0) this.onDayOfWeekButtonClick(0);
        else this.onDayOfWeekButtonClick(this.dayOfWeekButtons.length - 1);
    }

    // Event handler
    private void onDayOfWeekButtonClick(int selectedIndex) {
        if(this.selectedDayOfWeekButtonIndex == selectedIndex) return;
        // Restore old button color
        if(this.selectedDayOfWeekButtonIndex != -1)
            this.dayOfWeekButtons[this.selectedDayOfWeekButtonIndex].setBackgroundColor(ContextCompat.getColor(this, R.color.button_grey));
        // Set new button color
        this.dayOfWeekButtons[selectedIndex].setBackgroundColor(ContextCompat.getColor(this, R.color.item_selected_color));
        // Update internal state
        this.selectedDayOfWeekButtonIndex = selectedIndex;
        // UI updates
        this.selectedDate = this.dayOfWeekStrings[selectedIndex];
        this.refreshLineChartDelayed(false);
    }

    // Rendering
    private void updateDayOfWeekButtons() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        // Find the Sunday of the current week
        LocalDate sunday = this.today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

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


    /* -------- Content display switch methods -------- */

    // Event handler
    private void onDataContentSwitchToggle() {
        this.updateDataContentSwitch();
        this.refreshLineChartDelayed();
    }

    // Helper
    private void updateDataContentSwitch() {
        this.viewingALS = this.dataContentSwitch.isChecked();
        this.dataContentSwitch.setText(this.viewingALS ? "Light Intensity": "UV Index");
    }


    /* -------- Line chart methods -------- */

    // Event handler
    public void onLineChartValueSelected(Entry e, Highlight ignoredH) {
        if(this.viewingMinutes) return;
        if(e.getData() == null || !(e.getData() instanceof Integer)) return;
        this.selectedHour = (Integer)e.getData();
        this.refreshLineChartDelayed(true);
    }

    // Event handler
    public void onLineChartValueDeselected() {
        if(!this.viewingMinutes) return;
        this.refreshLineChartNow(false);
    }

    // Event handler
    @Override
    public void onBackPressed() {
        if(!this.viewingMinutes) super.onBackPressed();
        this.refreshLineChartNow(false);
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
                            int h24 = HOURS_START + i;
                            float val = dbHelper.getHourlyAvg(day, h24, this.viewingALS);
                            if(Float.isNaN(val)) return null;
                            return new Entry(i, val, h24);
                        })
                        .collect(Collectors.toList());

        // Generate datasets
        LineData lineData = this.prepareDataSets(yAxisValues, dataSet -> {
            dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER); // Use different modes for the line appearance
            dataSet.setDrawValues(false); // Set to false to hide the values on data points
            dataSet.setLineWidth(4f); // Set line width to 4
            dataSet.setCircleRadius(8f);
        });

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
        this.lineChart.setData(lineData);
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
                    float val = dbHelper.getMinuteAvg(day, i, hour, this.viewingALS);
                    if(Float.isNaN(val)) return null;
                    return new Entry(i, val);
                })
                .collect(Collectors.toList());


        // Generate datasets
        LineData lineData = this.prepareDataSets(yAxisValues, dataSet -> {
            dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER); // Use different modes for the line appearance
            dataSet.setDrawValues(false); // Set to false to hide the values on data points
            dataSet.setLineWidth(6f); // Set line width to 6
            dataSet.setDrawCircles(false); // Set to false to hide circles at data points
            dataSet.setColor(ContextCompat.getColor(this, R.color.stats_color));
        });

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
        this.lineChart.setData(lineData);
        this.lineChart.invalidate();
        this.selectedDateTextView.setText(generateDateText(date) + ", " + generateHourText(hour));
    }

    // Helper
    public LineData prepareDataSets(List<Entry> values, Consumer<LineDataSet> formatter) {
        List<LineDataSet> dataSets = new ArrayList<>(2);

        int length = values.size();
        int recordedIndex = 0;
        boolean recording = values.get(0) != null;
        for(int index = 0; index < length; index++) {
            Entry entry = values.get(index);
            if(!recording) {
                if(entry == null) continue;
                recording = true;
                recordedIndex = index;
            }
            if(entry != null && index != length - 1) continue;
            if(index == length - 1) index++;
            recording = false;
            List<Entry> subset = values;
            if(recordedIndex != 0 || index != length)
                subset = values.subList(recordedIndex, index);
            LineDataSet dataSet = new LineDataSet(subset, "");
            formatter.accept(dataSet);
            dataSets.add(dataSet);
        }

        List<Entry> placeholderValues = new ArrayList<>(2);
        placeholderValues.add(new Entry(0, 0));
        placeholderValues.add(new Entry(values.size() - 1, 0));

        LineDataSet dataSetPlaceholder = new LineDataSet(placeholderValues, "");
        dataSetPlaceholder.setVisible(false);

        dataSets.add(dataSetPlaceholder);

        return new LineData(dataSets.toArray(new LineDataSet[0]));
    }

    // Helper
    public void refreshLineChartDelayed() {
        this.refreshLineChartDelayed(this.viewingMinutes);
    }

    public void refreshLineChartDelayed(boolean displayMinuteView) {
        this.handler.postDelayed(() -> this.refreshLineChartNow(displayMinuteView), 15);
    }

    public void refreshLineChartNow() {
        this.refreshLineChartNow(this.viewingMinutes);
    }

    public void refreshLineChartNow(boolean displayMinuteView) {
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
            new Handler(Looper.getMainLooper()).post(this::refreshLineChartDelayed);
    }

    @EventHandler
    protected void onSyncDataReceived(@NonNull SyncDataReceivedEvent event) {
        if(PROGRESSIVE_REFRESH)
            new Handler(Looper.getMainLooper()).post(this::refreshLineChartDelayed);
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
