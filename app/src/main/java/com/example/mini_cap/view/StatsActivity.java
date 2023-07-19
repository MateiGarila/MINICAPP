package com.example.mini_cap.view;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mini_cap.R;
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
import java.util.Arrays;

public class  StatsActivity extends AppCompatActivity {
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

    private TextView date_text_view;

    private TextView curr_time_text_view;

    public ArrayList<String> curr_week_list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        LocalDate current_date = LocalDate.now();
        previousSelectedPosition = -1;

        defaultColor = ContextCompat.getColor(this, R.color.button_grey);
        selectedColor = ContextCompat.getColor(this, R.color.item_selected_color);
        line_chart = (LineChart) findViewById(R.id.line_chart);

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

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMMM d' 'yyyy");
        date_text_view.setText(current_date.format(outputFormatter));
        curr_week_list = getWeekDays(current_date, curr_week);
        set_menu_text(menu_buttons, curr_week_list);

        ArrayList<String> x_axis_values = set_x_axis_values();

        ArrayList<Float> y_axis_values = new ArrayList<>(Arrays.asList(1.5f, 2.8f, 3.2f, 4.7f, 12.0f, 10.f, 5.3f, 8f, 3f, 4f));

        ArrayList<Entry> dataPoints = new ArrayList<>();
        for (int i = 0; i < y_axis_values.size(); i++) {
            dataPoints.add(new Entry(i, y_axis_values.get(i)));
        }
        System.out.println("current week: " + curr_week_list);

        // Create a LineDataSet and configure the appearance if needed
        LineDataSet dataSet = new LineDataSet(dataPoints, "");
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
        line_chart.invalidate();


        next_week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curr_week += 1;
                curr_week_list = getWeekDays(current_date, curr_week);
                set_menu_text(menu_buttons, curr_week_list);
                if (previousSelectedPosition != -1) {
                    date_text_view.setText(setDateTextView(curr_week_list.get(previousSelectedPosition)));
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
            buttons.get(i).setText(parts[0]);
        }
    }

    public static ArrayList<String> getWeekDays(LocalDate currentDate, int currentWeek) {
        ArrayList<String> weekDays = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yy");

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
        date_text_view.setText(setDateTextView(curr_week_list.get(selectedIndex)));

        // Handle item click here
        System.out.println("Selected button: " + clickedButton.getText());

        previousSelectedPosition = selectedIndex;
    }

    private ArrayList<String> set_x_axis_values(){
        ArrayList<String> hoursList = new ArrayList<>();
        for (int hour = 8; hour <= 18; hour++) {
            String hourStr = (hour < 12) ? hour + "AM" : (hour - 12) + "PM";
            hoursList.add(hourStr);
        }
        return hoursList;
    }
    public static String setDateTextView(String inputDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("d-M-yy");
        LocalDate date = LocalDate.parse(inputDate, inputFormatter);

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMMM d' 'yyyy");
        String formattedDate = date.format(outputFormatter);

        return formattedDate;
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







