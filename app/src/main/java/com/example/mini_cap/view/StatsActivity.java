package com.example.mini_cap.view;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mini_cap.R;
import com.example.mini_cap.controller.DBHelper;
import com.example.mini_cap.controller.SensorController;
import com.example.mini_cap.model.Day;
import com.example.mini_cap.model.Stats;
import com.example.mini_cap.view.helper.IntentDataHelper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import app.uvtracker.sensor.pii.event.EventHandler;
import app.uvtracker.sensor.pii.event.IEventListener;

public class  StatsActivity extends AppCompatActivity implements IEventListener {
    private LineChart line_chart;
    private Button prev_week;
    private Button day1;
    private Button day2;
    private Button day3;
    private Button day4;
    private Button day5;
    private Button day6;
    private Button day7;
    private Button next_week;
    public int curr_week = 0;


    public int defaultColor;
    public int selectedColor;
    private int previousSelectedPosition;
    private Button previousSelectedButton;
    private DBHelper dbHelper;
    private Day date;
    private TextView date_text_view;

    private TextView curr_time_text_view;

    private  TextView currentUVIndex;

    //private String currentSelectedDate;
    private  LineDataSet dataSet;

    public ArrayList<String> curr_week_list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        // Get sensor and register event handler
        SensorController sensorController = IntentDataHelper.readSensorController();
        if(sensorController == null) {
            // Sensor is not connected!
            Toast.makeText(this, "Please first connect to a sensor.", Toast.LENGTH_SHORT).show();
        }
        else {
            sensorController.registerListener(this);
        }

        this.dbHelper = new DBHelper(this);

/*
        //"dd/MM/yyyy HH:mm:ss"
//        dbHelper = new DBHelper(this);
//        Stats stats1 = new Stats(1, 3.0F, "19/07/2023 08:00:00");
//        Stats stats2 = new Stats(2, 4.0F, "19/07/2023 09:00:00");
//        Stats stats3 = new Stats(3, 4.5F, "19/07/2023 10:00:00");
//        Stats stats4 = new Stats(4, 5.3F, "19/07/2023 11:00:00");
//        Stats stats5 = new Stats(5, 3.4F, "19/07/2023 12:00:00");
//        Stats stats6 = new Stats(6, 6.0F, "19/07/2023 13:00:00");
//        Stats stats7 = new Stats(7, 7.5F, "19/07/2023 14:00:00");
//        Stats stats8 = new Stats(8, 11.2F, "19/07/2023 15:00:00");
//        Stats stats9 = new Stats(9, 8.0F, "19/07/2023 16:00:00");
//        Stats stats10 = new Stats(10, 5.0F, "19/07/2023 17:00:00");
//        Stats stats11 = new Stats(11, 6.2F, "19/07/2023 18:00:00");
//        Stats stats12 = new Stats(12, 3.0F, "19/07/2023 19:00:00");
//        Stats stats13 = new Stats(13, 3.2F, "19/07/2023 20:00:00");
//
//        dbHelper.insertStats(stats1);
//        dbHelper.insertStats(stats2);
//        dbHelper.insertStats(stats3);
//        dbHelper.insertStats(stats4);
//        dbHelper.insertStats(stats5);
//        dbHelper.insertStats(stats6);
//        dbHelper.insertStats(stats7);
//        dbHelper.insertStats(stats8);
//        dbHelper.insertStats(stats9);
//        dbHelper.insertStats(stats10);
//        dbHelper.insertStats(stats11);
//        dbHelper.insertStats(stats12);
//        dbHelper.insertStats(stats13);
*/

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMMM d' 'yyyy");
        DateTimeFormatter outputFormatterForDB = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        // Getting date as LocalDate object and creating a Date class object with it
        LocalDate current_date = LocalDate.now();
        Day currentDate = new Day(current_date);

        previousSelectedPosition = -1;

        defaultColor = ContextCompat.getColor(this, R.color.button_grey);
        selectedColor = ContextCompat.getColor(this, R.color.item_selected_color);
        line_chart = findViewById(R.id.line_chart);

        date_text_view = findViewById(R.id.date_text_view);
        //initialize datetime menu
        prev_week = findViewById(R.id.prev_week_button);
        day1 = findViewById(R.id.day_1);
        day2 = findViewById(R.id.day_2);
        day3 = findViewById(R.id.day_3);
        day4 = findViewById(R.id.day_4);
        day5 = findViewById(R.id.day_5);
        day6 = findViewById(R.id.day_6);
        day7 = findViewById(R.id.day_7);
        next_week = findViewById(R.id.next_week_button);

        ArrayList<Button> menu_buttons = new ArrayList<>();
        menu_buttons.add(day1);
        menu_buttons.add(day2);
        menu_buttons.add(day3);
        menu_buttons.add(day4);
        menu_buttons.add(day5);
        menu_buttons.add(day6);
        menu_buttons.add(day7);

        //set UV index realtime text for this textview
        currentUVIndex = findViewById(R.id.uvindex_id);


        date_text_view.setText(current_date.format(outputFormatter));
        curr_week_list = getWeekDays(current_date, curr_week);
        set_menu_text(menu_buttons, curr_week_list);

        ArrayList<String> x_axis_values = set_x_axis_values();

        ArrayList<Float> y_axis_values = new ArrayList<>();

        float[] uv_values_float = dbHelper.getExposureForDay(currentDate);
        for (float value : uv_values_float) {
            y_axis_values.add(value);
        }




        ArrayList<Entry> dataPoints = new ArrayList<>();
        for (int i = 0; i < y_axis_values.size(); i++) {
            dataPoints.add(new Entry(i, y_axis_values.get(i)));
        }


        // Create a LineDataSet and configure the appearance if needed
        dataSet = new LineDataSet(dataPoints, "");
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER); // Use different modes for the line appearance
        dataSet.setDrawValues(false); // Set to false to hide the values on data points
        dataSet.setLineWidth(4f); // Set line width to 4
        dataSet.setCircleRadius(8f);
        line_chart.setDrawGridBackground(true);
        line_chart.setGridBackgroundColor(Color.TRANSPARENT); // Transparent background color

        XAxis xAxis = line_chart.getXAxis();
        xAxis.setGridColor(Color.TRANSPARENT); // Transparent grid lines
        xAxis.setValueFormatter(new IndexAxisValueFormatter(x_axis_values));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Move X-axis to the bottom
        xAxis.setTextSize(12f);
        xAxis.setDrawAxisLine(false);


        YAxis leftYAxis = line_chart.getAxisLeft();
        leftYAxis.setDrawLabels(false); // Do not draw Y-axis values on the left side
        leftYAxis.setGridColor(Color.TRANSPARENT); // Transparent grid lines on the left side

        YAxis rightYAxis = line_chart.getAxisRight();
        rightYAxis.setDrawLabels(false); // Do not draw Y-axis values on the left side
        rightYAxis.setGridColor(Color.TRANSPARENT);


        // Hide the description label
        Description description = line_chart.getDescription();
        description.setEnabled(false);

        // Hide the dataset label in the legend
        Legend legend = line_chart.getLegend();
        legend.setEnabled(false);



        // Add the LineDataSet to LineData and set it to your LineChart
        LineData lineData = new LineData(dataSet);
        line_chart.setData(lineData);

        line_chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int originalColor = Color.BLUE;
                float originalRadius = 8f;
                dataSet.setCircleColors(new int[] { originalColor }); // Set the circle color for all data points
                dataSet.setCircleRadius(originalRadius); // Set the circle radius for all data points

                // Create a new DataSet for the clicked data point
                LineDataSet clickedDataSet = new LineDataSet(null, "Clicked DataSet");
                clickedDataSet.setDrawValues(true);
                clickedDataSet.setValueTextSize(16f);

                clickedDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                clickedDataSet.setColor(Color.parseColor("#88B0F6")); // Darker blue color for the clicked data point
                clickedDataSet.setCircleColor(Color.parseColor("#88B0F6")); // Darker blue color for the clicked data point
                clickedDataSet.setCircleHoleColor(Color.WHITE); // Customize the circle hole color
                clickedDataSet.setCircleRadius(12f); // Enlarged circle radius for the clicked data point

                // Add the selected data point to the clicked DataSet
                ArrayList<Entry> clickedDataPoints = new ArrayList<>();
                clickedDataPoints.add(e);
                clickedDataSet.setValues(clickedDataPoints);

                // Combine the original DataSet and the clicked DataSet
                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(dataSet); // Add the original DataSet
                dataSets.add(clickedDataSet); // Add the clicked DataSet

                // Create a new LineData object with the combined DataSets
                LineData combinedLineData = new LineData(dataSets);
                line_chart.setData(combinedLineData);

                // Refresh the chart
                line_chart.invalidate();
                float yValue = e.getY();

                // Do something with the Y value, e.g., display it in a TextView
                // textView.setText("Selected Y Value: " + yValue);
            }

            @Override
            public void onNothingSelected() {
                // Do something when nothing is selected (optional)
            }
        });
        // Refresh the chart



        next_week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curr_week += 1;
                curr_week_list = getWeekDays(current_date, curr_week);
                set_menu_text(menu_buttons, curr_week_list);
                if (previousSelectedPosition != -1) {
                    String selectedDate = curr_week_list.get(previousSelectedPosition);
                    date_text_view.setText(setDateTextView(selectedDate));
                    createDataSet(selectedDate);
                    LineData lineData = new LineData(dataSet);
                    line_chart.setData(lineData);

                }

            }
        });



        prev_week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curr_week -= 1;
                curr_week_list = getWeekDays(current_date, curr_week);
                set_menu_text(menu_buttons, curr_week_list);
                if (previousSelectedPosition != -1) {
                    date_text_view.setText(setDateTextView(curr_week_list.get(previousSelectedPosition)));
                }
            }
        });

        for (int i = 0; i < menu_buttons.size(); i++) {
            Button button = menu_buttons.get(i);
            int finalIndex = i; // Create a final variable for use in lambda expression
            button.setOnClickListener(event -> handleButtonClick(button, finalIndex));


        }


    }

    public static void set_menu_text(ArrayList<Button> buttons, ArrayList<String> curr_week) {

        // Set the text of each button to the corresponding day
        for (int i = 0; i < buttons.size(); i++) {
            String[] parts = curr_week.get(i).split("-");
            int dayOfMonth = Integer.parseInt(parts[0]);
            buttons.get(i).setText(String.valueOf(dayOfMonth));
        }
    }

    public static ArrayList<String> getWeekDays(LocalDate currentDate, int currentWeek) {
        ArrayList<String> weekDays = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        // Find the Sunday of the current week
        LocalDate sunday = currentDate.with(DayOfWeek.SUNDAY);

        // Add the number of weeks based on the currentWeek value
        LocalDate firstDayOfWeek = sunday.plusWeeks(currentWeek -1);

        // Add the dates of the week to the ArrayList
        for (int i = 0; i < 7; i++) {
            LocalDate day = firstDayOfWeek.plusDays(i);
            String formattedDate = day.format(formatter);
            weekDays.add(formattedDate);
        }

        return weekDays;
    }

    private void handleButtonClick(Button clickedButton, int selectedIndex) {
        if (previousSelectedButton != null) {
            previousSelectedButton.setBackgroundColor(defaultColor);
        }

        clickedButton.setBackgroundColor(selectedColor);
        previousSelectedButton = clickedButton;

        // Handle item click here
        System.out.println("Selected button: " + clickedButton.getText());

        previousSelectedPosition = selectedIndex;
        line_chart.invalidate();

        date_text_view.setText(setDateTextView(curr_week_list.get(selectedIndex)));
        String selectedDate = curr_week_list.get(previousSelectedPosition);
        ArrayList<Float> y_axis_values = new ArrayList<>();

        String[] conversion1 = selectedDate.split("-");
        int month = Integer.parseInt(conversion1[1]);
        int day = Integer.parseInt(conversion1[0]);
        int year = Integer.parseInt(conversion1[2]);
        Day selectedDate2 = new Day(day, month, year);

        float[] uv_values_float = dbHelper.getExposureForDay(selectedDate2);
        //System.out.println("uv values:" + uv_values_float);
        for (float value : uv_values_float) {
            System.out.println("uv value" + value);
            y_axis_values.add(value);
        }

        date_text_view.setText(setDateTextView(selectedDate));
        createDataSet(selectedDate);
        LineData lineData = new LineData(dataSet);
        line_chart.setData(lineData);
    }

    private ArrayList<String> set_x_axis_values(){
        ArrayList<String> hoursList = new ArrayList<>();
        for (int hour = 8; hour <= 20; hour++) {
            String hourStr ="";
            if (hour!= 12) {
                hourStr = (hour < 12) ? hour + "AM" : (hour - 12) + "PM";
            } else {
                hourStr = "12PM";
            }
            hoursList.add(hourStr);
        }
        return hoursList;
    }
    public static String setDateTextView(String inputDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate date = LocalDate.parse(inputDate, inputFormatter);

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMMM d' 'yyyy");
        String formattedDate = date.format(outputFormatter);

        return formattedDate;
    }

    public void createDataSet(String selectedDate){
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        LocalDate date = LocalDate.parse(selectedDate, inputFormatter);
        ArrayList<Float> y_axis_values = new ArrayList<>();
        Day outputDate = new Day(date);
        float[] uv_values_float = dbHelper.getExposureForDay(outputDate);


        //System.out.println("uv values:" + uv_values_float);
        for (float value : uv_values_float) {
            System.out.println("uv value" + value);
            y_axis_values.add(value);
        }


        ArrayList<Entry> dataPoints = new ArrayList<>();
        for (int i = 0; i < y_axis_values.size(); i++) {
            dataPoints.add(new Entry(i, y_axis_values.get(i)));
        }

        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER); // Use different modes for the line appearance
        dataSet.setDrawValues(false); // Set to false to hide the values on data points
        dataSet.setLineWidth(4f); // Set line width to 4
        dataSet.setCircleRadius(8f);
        dataSet = new LineDataSet(dataPoints, "");
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER); // Use different modes for the line appearance
        dataSet.setDrawValues(false); // Set to false to hide the values on data points
        dataSet.setLineWidth(4f); // Set line width to 4
        dataSet.setCircleRadius(8f);
        line_chart.setDrawGridBackground(true);
        line_chart.setGridBackgroundColor(Color.TRANSPARENT); // Transparent background color

        XAxis xAxis = line_chart.getXAxis();
        xAxis.setGridColor(Color.TRANSPARENT); // Transparent grid lines

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Move X-axis to the bottom
        xAxis.setTextSize(12f);
        xAxis.setDrawAxisLine(false);


        YAxis leftYAxis = line_chart.getAxisLeft();
        leftYAxis.setDrawLabels(false); // Do not draw Y-axis values on the left side
        leftYAxis.setGridColor(Color.TRANSPARENT); // Transparent grid lines on the left side

        YAxis rightYAxis = line_chart.getAxisRight();
        rightYAxis.setDrawLabels(false); // Do not draw Y-axis values on the left side
        rightYAxis.setGridColor(Color.TRANSPARENT);


        // Hide the description label
        Description description = line_chart.getDescription();
        description.setEnabled(false);

        // Hide the dataset label in the legend
        Legend legend = line_chart.getLegend();
        legend.setEnabled(false);
//        line_chart.setDrawGridBackground(true);
//        line_chart.setGridBackgroundColor(Color.TRANSPARENT); // Transparent background color
//
//        XAxis xAxis = line_chart.getXAxis();
//        xAxis.setGridColor(Color.TRANSPARENT); // Transparent grid lines
//        xAxis.setValueFormatter(new IndexAxisValueFormatter(x_axis_values));
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Move X-axis to the bottom
//        xAxis.setTextSize(12f);
//        xAxis.setDrawAxisLine(false);
//
//
//        YAxis leftYAxis = line_chart.getAxisLeft();
//        leftYAxis.setDrawLabels(false); // Do not draw Y-axis values on the left side
//        leftYAxis.setGridColor(Color.TRANSPARENT); // Transparent grid lines on the left side
//
//        YAxis rightYAxis = line_chart.getAxisRight();
//        rightYAxis.setDrawLabels(false); // Do not draw Y-axis values on the left side
//        rightYAxis.setGridColor(Color.TRANSPARENT);


//        // Hide the description label
//        Description description = line_chart.getDescription();
//        description.setEnabled(false);

        // Hide the dataset label in the legend
//        Legend legend = line_chart.getLegend();
//        legend.setEnabled(false);
//
//
//
//        // Add the LineDataSet to LineData and set it to your LineChart
//        LineData lineData = new LineData(dataSet);
//        line_chart.setData(lineData);

    }

    @EventHandler
    protected void onRealTimeData(@NonNull SensorController.RealTimeDataEvent event) {
        this.setCurrentUVIndex(event.toString());
    }

    public void setCurrentUVIndex(@NonNull String message){
        currentUVIndex.setText(message);
    }

}



//        public static ArrayList<String> getWeekDays(LocalDate currentDate) {
//            ArrayList<String> weekDays = new ArrayList<>();
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yy");
//
//            // Find the Sunday of the current week
//            LocalDate sunday = currentDate.with(DayOfWeek.SUNDAY);
//
//            LocalDate firstDayOfWeek = sunday.minusDays(sunday.getDayOfWeek().getValue() - 1);
//
//            // Add the dates of the week to the ArrayList
//            for (int i = 0; i < 7; i++) {
//                LocalDate day = firstDayOfWeek.plusDays(i);
//                String formattedDate = day.format(formatter);
//                weekDays.add(formattedDate);
//            }
//
//            return weekDays;
//        }







