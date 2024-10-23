package com.example.campusexpensemanager;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.campusexpensemanager.helper.DatabaseHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SearchFragment extends Fragment {
    private DatabaseHelper databaseHelper;
    private EditText etAmount, etDescription, etNewCategory;  // Thêm trường nhập cho danh mục mới
    private Spinner spinnerCategory, spinnerType, spinnerSource;
    private TextView tvDate;
    private Button btnAddTransaction;
    private int selectedYear, selectedMonth, selectedDay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        databaseHelper = new DatabaseHelper(getContext());
        initializeViews(view);
        setupEventListeners();

        return view;
    }

    private void initializeViews(View view) {
        etAmount = view.findViewById(R.id.etAmount);
        spinnerType = view.findViewById(R.id.spinnerType);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        spinnerSource = view.findViewById(R.id.spinnerSource);
        etDescription = view.findViewById(R.id.etDescription);
        etNewCategory = view.findViewById(R.id.etNewCategory);  // Khởi tạo trường nhập danh mục mới
        tvDate = view.findViewById(R.id.tvDate);
        btnAddTransaction = view.findViewById(R.id.btnSaveTransaction);

        loadCategoriesIntoSpinner(); // Load categories into spinner
        setupTypeSpinner(); // Setup transaction type spinner
    }

    private void setupEventListeners() {
        // Handle date selection
        tvDate.setOnClickListener(v -> showDatePickerDialog());

        // Handle transaction addition
        btnAddTransaction.setOnClickListener(v -> addTransaction());

        // Handle type selection (Income or Expense)
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString().toLowerCase();
                handleTransactionTypeSelection(selectedType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Handle category selection to show/hide new category input
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = parent.getItemAtPosition(position).toString();
                if (selectedCategory.equals("Other (Add New)")) {
                    etNewCategory.setVisibility(View.VISIBLE);  // Show input field for new category
                } else {
                    etNewCategory.setVisibility(View.GONE);  // Hide input field if a predefined category is selected
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                etNewCategory.setVisibility(View.GONE);
            }
        });
    }

    private void handleTransactionTypeSelection(String selectedType) {
        if (selectedType.equals("income")) {
            setupSourceSpinner();
            spinnerSource.setVisibility(View.VISIBLE);
            etDescription.setVisibility(View.GONE);
        } else if (selectedType.equals("expense")) {
            spinnerSource.setVisibility(View.GONE);
            etDescription.setVisibility(View.VISIBLE);
        }
    }

    private void setupTypeSpinner() {
        String[] types = {"income", "expense"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);
    }

    private void setupSourceSpinner() {
        String[] sources = {"Salary", "Freelance"};
        ArrayAdapter<String> sourceAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, sources);
        sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSource.setAdapter(sourceAdapter);
    }

    private void addTransaction() {
        String amountStr = etAmount.getText().toString();
        String category = spinnerCategory.getSelectedItem().toString();
        String date = tvDate.getText().toString();
        String type = spinnerType.getSelectedItem().toString().toLowerCase();

        if (validateInputs(amountStr, category, date)) {
            double amount = Double.parseDouble(amountStr);
            int userId = getUserIdFromSession();
            int categoryId = -1;
            if (category.equals("Other (Add New)")) {
                String newCategory = etNewCategory.getText().toString();
                categoryId = databaseHelper.addNewCategory(newCategory);
            } else {
                categoryId = databaseHelper.getCategoryIdByName(category);
            }

            String source = null;
            String description = null;

            if (type.equals("income")) {
                source = spinnerSource.getSelectedItem().toString();
            } else if (type.equals("expense")) {
                description = etDescription.getText().toString();
            }

            boolean isInserted = databaseHelper.addTransaction(userId, amount, date, type, categoryId, source, description);

            if (isInserted) {
                Toast.makeText(getContext(), "Transaction added successfully!", Toast.LENGTH_SHORT).show();
                clearFields();
            } else {
                Toast.makeText(getContext(), "Failed to add transaction", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateInputs(String amountStr, String category, String date) {
        if (amountStr.isEmpty() || category.isEmpty() || date.equals("Select Date")) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (category.equals("Other (Add New)") && etNewCategory.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please enter the new category", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private int getUserIdFromSession() {
        SharedPreferences prefs = getContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, monthOfYear, dayOfMonth) -> {
            selectedYear = year1;
            selectedMonth = monthOfYear;
            selectedDay = dayOfMonth;
            tvDate.setText(String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay));
        }, year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void loadCategoriesIntoSpinner() {
        List<String> categories = new ArrayList<>(databaseHelper.getAllCategoryNames());
        categories.add("Other (Add New)");  // Add option for creating new category
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }
    private void clearFields() {
        etAmount.setText("");
        etDescription.setText("");
        etNewCategory.setText("");
        tvDate.setText("Select Date");
        spinnerCategory.setSelection(0);
        spinnerType.setSelection(0);
        etNewCategory.setVisibility(View.GONE);
        spinnerSource.setVisibility(View.GONE);
        etDescription.setVisibility(View.GONE);
    }
}
