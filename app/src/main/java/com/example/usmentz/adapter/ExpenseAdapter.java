package com.example.usmentz.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usmentz.R;
import com.example.usmentz.fina.Expense;

import java.util.ArrayList;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private List<Expense> expenses = new ArrayList<>();
    private OnExpenseDeleteListener deleteListener;

    public interface OnExpenseDeleteListener {
        void onDelete(Expense expense);
    }

    public void setOnExpenseDeleteListener(OnExpenseDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses != null ? expenses : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.tvExpenseName.setText(expense.getDescription());
        holder.tvExpenseAmount.setText(String.format("₱%.2f", expense.getAmount()));
        holder.btnDeleteExpense.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(expense);
        });
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExpenseName, tvExpenseAmount;
        ImageView btnDeleteExpense;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpenseName    = itemView.findViewById(R.id.tvExpenseName);
            tvExpenseAmount  = itemView.findViewById(R.id.tvExpenseAmount);
            btnDeleteExpense = itemView.findViewById(R.id.btnDeleteExpense);
        }
    }
}