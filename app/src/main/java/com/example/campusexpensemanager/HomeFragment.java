package com.example.campusexpensemanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.campusexpensemanager.helper.DatabaseHelper;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private DatabaseHelper databaseHelper;
    private TextView totalIncomeView, totalExpenseView, balanceView;
    private PieChart pieChart;
    private int userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo DatabaseHelper
        databaseHelper = new DatabaseHelper(getContext());

        // Ánh xạ các view từ XML
        totalIncomeView = view.findViewById(R.id.textViewTotalIncome);
        totalExpenseView = view.findViewById(R.id.textViewTotalExpense);
        balanceView = view.findViewById(R.id.textViewBalance);
        pieChart = view.findViewById(R.id.pieChart);


        // Lấy ID người dùng từ phiên (SharedPreferences)
        userId = getUserIdFromSession();

        if (userId != -1) {

            // Cập nhật số liệu thống kê và biểu đồ
            updateStatistics(userId);
        } else {
            // Không có người dùng đăng nhập
            Toast.makeText(getContext(), "User ID not found!", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Cập nhật số liệu thống kê và biểu đồ khi người dùng quay lại trang
        if (userId != -1) {
            updateStatistics(userId);
        }
    }

    // Cập nhật tổng thu nhập, chi tiêu và số dư
    private void updateStatistics(int userId) {
        // Lấy tổng thu nhập và chi tiêu từ cơ sở dữ liệu
        double totalIncome = databaseHelper.getTotalIncome(userId);
        double totalExpense = databaseHelper.getTotalExpense(userId);
        double balance = totalIncome - totalExpense;

        // Hiển thị dữ liệu đã được format cho người dùng
        totalIncomeView.setText(String.format("Total Income: %.2f", totalIncome));
        totalExpenseView.setText(String.format("Total Expense: %.2f", totalExpense));
        balanceView.setText(String.format("Balance: %.2f", balance));

        // Cập nhật biểu đồ nếu có dữ liệu hợp lệ
        if (totalIncome > 0 || totalExpense > 0) {
            updatePieChart(totalIncome, totalExpense);
        } else {
            pieChart.clear();  // Xóa biểu đồ nếu không có dữ liệu
        }
    }

    // Cập nhật biểu đồ tròn cho thu nhập và chi tiêu
    private void updatePieChart(double income, double expense) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float) income, "Income"));
        entries.add(new PieEntry((float) expense, "Expense"));

        PieDataSet dataSet = new PieDataSet(entries, "Financial Overview");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);  // Sử dụng màu mặc định của thư viện

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();  // Làm mới biểu đồ
    }

    // Lấy user_id từ phiên hiện tại (SharedPreferences)
    private int getUserIdFromSession() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("user_id", -1);  // Trả về -1 nếu không tìm thấy user_id
    }
}