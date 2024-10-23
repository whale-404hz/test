package com.example.campusexpensemanager;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private List<Transaction_Notification.Transaction> transactionList;

    public TransactionAdapter(List<Transaction_Notification.Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction_Notification.Transaction transaction = transactionList.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewAmount, textViewSourceOrCategory, textViewType;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewAmount = itemView.findViewById(R.id.textViewAmount);
            textViewSourceOrCategory = itemView.findViewById(R.id.textViewSourceOrCategory);
            textViewType = itemView.findViewById(R.id.textViewType);
        }

        public void bind(Transaction_Notification.Transaction transaction) {
            if (transaction != null) {
                // Thiết lập số tiền, nguồn/danh mục và loại
                textViewAmount.setText(String.valueOf(transaction.getAmount()));
                textViewSourceOrCategory.setText(transaction.getSourceOrCategory() != null ? transaction.getSourceOrCategory() : "Không xác định");
                textViewType.setText(transaction.getType() != null ? transaction.getType() : "Không có");

                // Thay đổi màu sắc dựa trên loại giao dịch
                if ("income".equalsIgnoreCase(transaction.getType())) {
                    textViewAmount.setTextColor(Color.GREEN);
                } else if ("expense".equalsIgnoreCase(transaction.getType())) {
                    textViewAmount.setTextColor(Color.RED);
                } else {
                    textViewAmount.setTextColor(Color.BLACK); // Màu mặc định nếu loại không xác định
                }
            }
        }
    }
}
