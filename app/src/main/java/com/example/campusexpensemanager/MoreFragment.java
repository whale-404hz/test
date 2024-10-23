package com.example.campusexpensemanager;

import android.content.Context; // Add this import for Context
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.campusexpensemanager.helper.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class MoreFragment extends Fragment {
    private RecyclerView recyclerView;
    private TransactionAdapter transactionAdapter;
    private DatabaseHelper databaseHelper;
    private List<Transaction_Notification.Transaction> transactionList;
    private int userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);

        // Initialize views and database helper
        recyclerView = view.findViewById(R.id.recyclerViewNotifications);
        databaseHelper = new DatabaseHelper(getContext());
        transactionList = new ArrayList<>();

        // Fetch logged-in user_id from session
        userId = getUserIdFromSession();

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        transactionAdapter = new TransactionAdapter(transactionList);
        recyclerView.setAdapter(transactionAdapter);

        // Fetch transactions for the logged-in user
        fetchTransactions(userId);

        return view;
    }

    private int getUserIdFromSession() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("user_id", -1); // Returns -1 if no user is logged in
    }

    private void fetchTransactions(int userId) {
        transactionList.clear();

        // Fetch Income Transactions for the logged-in user
        Cursor incomeCursor = databaseHelper.getAllIncomeTransactions(userId);
        if (incomeCursor.moveToFirst()) {
            do {
                int transactionId = incomeCursor.getInt(incomeCursor.getColumnIndexOrThrow("income_id"));
                double amount = incomeCursor.getDouble(incomeCursor.getColumnIndexOrThrow("amount"));
                String source = incomeCursor.getString(incomeCursor.getColumnIndexOrThrow("source"));
                String date = incomeCursor.getString(incomeCursor.getColumnIndexOrThrow("income_date"));

                Transaction_Notification.Transaction incomeTransaction = new Transaction_Notification.Transaction(
                        transactionId, userId, -1, amount, date, "income", source);
                transactionList.add(incomeTransaction);
            } while (incomeCursor.moveToNext());
        }
        incomeCursor.close();

        // Fetch Expense Transactions for the logged-in user
        Cursor expenseCursor = databaseHelper.getAllExpenseTransactions(userId);
        if (expenseCursor.moveToFirst()) {
            do {
                int transactionId = expenseCursor.getInt(expenseCursor.getColumnIndexOrThrow("expense_id"));
                double amount = expenseCursor.getDouble(expenseCursor.getColumnIndexOrThrow("amount"));
                String description = expenseCursor.getString(expenseCursor.getColumnIndexOrThrow("description"));
                String date = expenseCursor.getString(expenseCursor.getColumnIndexOrThrow("expense_date"));

                Transaction_Notification.Transaction expenseTransaction = new Transaction_Notification.Transaction(
                        transactionId, userId, -1, amount, date, "expense", description);
                transactionList.add(expenseTransaction);
            } while (expenseCursor.moveToNext());
        }
        expenseCursor.close();

        // Notify adapter about data changes
        transactionAdapter.notifyDataSetChanged();
    }
}
