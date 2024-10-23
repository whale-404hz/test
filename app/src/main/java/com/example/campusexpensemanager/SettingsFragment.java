package com.example.campusexpensemanager;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.campusexpensemanager.helper.DatabaseHelper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SettingsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private Spinner spinnerCategory;
    private EditText etBudgetAmount, etStartDate, etEndDate;
    private Button btnAddBudget, btnSaveBudget;
    private LineChart lineChart;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize views
        lineChart = view.findViewById(R.id.lineChart);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        etBudgetAmount = view.findViewById(R.id.etBudgetAmount);
        etStartDate = view.findViewById(R.id.etStartDate);
        etEndDate = view.findViewById(R.id.etEndDate);
        btnAddBudget = view.findViewById(R.id.btnAddBudget);
        btnSaveBudget = view.findViewById(R.id.btnSaveBudget);

        // Get user ID from SharedPreferences (assuming you're saving it there)
        userId = getUserIdFromPreferences();
        dbHelper = new DatabaseHelper(getActivity());

        // Set up date pickers for the start and end date fields
        setupDatePickers();

        // Load categories from the database and set up the spinner
        loadCategories();

        // Load existing data and set up chart
        loadBudgetData();
        setupAddBudgetButton();

        return view;
    }

    private int getUserIdFromPreferences() {
        // Retrieve user ID from SharedPreferences
        return getActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
                .getInt("user_id", -1);
    }

    private void setupDatePickers() {
        // Set up DatePickerDialog for start date
        etStartDate.setOnClickListener(v -> showDatePickerDialog((date) -> etStartDate.setText(date)));

        // Set up DatePickerDialog for end date
        etEndDate.setOnClickListener(v -> showDatePickerDialog((date) -> etEndDate.setText(date)));
    }

    // Method to show a DatePickerDialog and return the selected date
    private void showDatePickerDialog(OnDateSetListener listener) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                (view, year1, month1, dayOfMonth) -> {
                    // Format the date and return it to the listener
                    String selectedDate = year1 + "-" + (month1 + 1) + "-" + dayOfMonth;
                    listener.onDateSet(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    // Interface for handling the selected date
    private interface OnDateSetListener {
        void onDateSet(String date);
    }

    private void loadCategories() {
        // Fetch categories from the database
        List<Category> categories = dbHelper.getAllCategories();

        // Create an ArrayAdapter to populate the Spinner
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupAddBudgetButton() {
        btnAddBudget.setOnClickListener(v -> {
            // Make fields visible when "Add Budget" is clicked
            spinnerCategory.setVisibility(View.VISIBLE);
            etBudgetAmount.setVisibility(View.VISIBLE);
            etStartDate.setVisibility(View.VISIBLE);
            etEndDate.setVisibility(View.VISIBLE);
            btnSaveBudget.setVisibility(View.VISIBLE);
        });

        btnSaveBudget.setOnClickListener(v -> {
            // Save the new budget
            Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
            int categoryId = selectedCategory.getId(); // Get category_id
            String amountStr = etBudgetAmount.getText().toString();
            String startDate = etStartDate.getText().toString();
            String endDate = etEndDate.getText().toString();

            if (!amountStr.isEmpty() && !startDate.isEmpty() && !endDate.isEmpty()) {
                float amount = Float.parseFloat(amountStr);

                // Save to the database with category_id
                boolean isAdded = dbHelper.addBudget(userId, categoryId, amount, startDate, endDate);
                if (isAdded) {
                    Toast.makeText(getActivity(), "Budget added successfully", Toast.LENGTH_SHORT).show();

                    // Clear the fields and hide them
                    clearAndHideFields();

                    // Reload the budget data and refresh the chart
                    loadBudgetData();
                } else {
                    Toast.makeText(getActivity(), "Failed to add budget", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearAndHideFields() {
        // Clear all input fields
        etBudgetAmount.setText("");
        etStartDate.setText("");
        etEndDate.setText("");
        spinnerCategory.setSelection(0); // Reset spinner to default value

        // Hide the fields
        spinnerCategory.setVisibility(View.GONE);
        etBudgetAmount.setVisibility(View.GONE);
        etStartDate.setVisibility(View.GONE);
        etEndDate.setVisibility(View.GONE);
        btnSaveBudget.setVisibility(View.GONE);
    }

    private void loadBudgetData() {
        // Fetch budget data from the database and update the chart
        List<Budget> budgets = dbHelper.getBudgetsByUserId(userId);

        // Update chart with the latest budget data
        updateLineChart(budgets);
    }

    private void updateLineChart(List<Budget> budgets) {
        List<Entry> entries = new ArrayList<>();

        for (Budget budget : budgets) {
            // Convert start date to a float value (example: YYYYMMDD)
            entries.add(new Entry(Float.parseFloat(budget.getStartDate().replace("-", "")), budget.getAmount()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Budget");
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate();  // Refresh the chart
    }
}
