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

    public ExpenseAdapter() {
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return expenses.get(position).getId();
    }

    public interface OnExpenseDeleteListener {
        void onDelete(Expense expense);
    }

    public void setOnExpenseDeleteListener(OnExpenseDeleteListener listener) {
        this.deleteListener = listener;
    }

    public interface SortOption {
        int NEWEST_FIRST = 0;
        int OLDEST_FIRST = 1;
        int HIGHEST_AMOUNT = 2;
        int LOWEST_AMOUNT = 3;
    }

    private int currentSort = SortOption.NEWEST_FIRST;

    public void setSortOption(int sortOption) {
        this.currentSort = sortOption;
        sortExpenses();
        notifyDataSetChanged();
    }

    public int getSortOption() {
        return currentSort;
    }

    private void sortExpenses() {
        if (expenses == null || expenses.isEmpty()) return;

        java.util.Collections.sort(expenses, (e1, e2) -> {
            switch (currentSort) {
                case SortOption.OLDEST_FIRST:
                    return Long.compare(e1.getCreatedAt(), e2.getCreatedAt());
                case SortOption.HIGHEST_AMOUNT:
                    return Double.compare(e2.getAmount(), e1.getAmount());
                case SortOption.LOWEST_AMOUNT:
                    return Double.compare(e1.getAmount(), e2.getAmount());
                case SortOption.NEWEST_FIRST:
                default:
                    return Long.compare(e2.getCreatedAt(), e1.getCreatedAt());
            }
        });
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses != null ? expenses : new ArrayList<>();
        sortExpenses();
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
        holder.tvExpenseType.setText(expense.getType());
        
        // Set payment method icon
        String paymentMethod = expense.getPaymentMethod();
        int iconRes;
        if (paymentMethod != null) {
            switch (paymentMethod) {
                case "GCash":
                    iconRes = R.drawable.gcash_logo;
                    break;
                case "BPI":
                    iconRes = R.drawable.bpi_logo;
                    break;
                case "PayMaya":
                    iconRes = R.drawable.paymaya_logo;
                    break;
                case "Cash":
                default:
                    iconRes = R.drawable.cash_logo;
            }
        } else {
            iconRes = R.drawable.cash_logo;
        }
        try {
            holder.ivPaymentIcon.setImageResource(iconRes);
        } catch (Exception e) {
            holder.ivPaymentIcon.setImageResource(R.drawable.cash_logo);
        }
        
        // Format amount with sign based on type
        String amountStr;
        int color;
        
        if (expense.getType() != null && expense.getType().equals(Expense.TYPE_FUNDS)) {
            amountStr = "+₱" + String.format("%.0f", expense.getAmount());
            color = 0xFF4CAF50; // Green
        } else if (expense.getType() != null && expense.getType().equals(Expense.TYPE_SAVINGS)) {
            amountStr = "+₱" + String.format("%.0f", expense.getAmount());
            color = 0xFF00BFA5; // Teal
        } else {
            amountStr = "-₱" + String.format("%.0f", expense.getAmount());
            color = 0xFFF44336; // Red
        }
        
        holder.tvExpenseAmount.setText(amountStr);
        holder.tvExpenseAmount.setTextColor(color);
        
        // Click listener
        holder.itemView.setOnLongClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(expense);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExpenseName, tvExpenseAmount, tvExpenseType;
        ImageView ivPaymentIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpenseName = itemView.findViewById(R.id.tvExpenseName);
            tvExpenseAmount = itemView.findViewById(R.id.tvExpenseAmount);
            tvExpenseType = itemView.findViewById(R.id.tvExpenseType);
            ivPaymentIcon = itemView.findViewById(R.id.ivPaymentIcon);
        }
    }
}