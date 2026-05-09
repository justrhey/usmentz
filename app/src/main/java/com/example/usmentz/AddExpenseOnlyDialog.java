package com.example.usmentz;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.usmentz.fina.Expense;
import com.example.usmentz.viewmodel.ExpenseViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class AddExpenseOnlyDialog extends DialogFragment {

    private ExpenseViewModel expenseVm;
    private int momentId;
    private Runnable onSaved;

    public static AddExpenseOnlyDialog newInstance(int momentId, Runnable onSaved) {
        AddExpenseOnlyDialog frag = new AddExpenseOnlyDialog();
        frag.momentId = momentId;
        frag.onSaved = onSaved;
        return frag;
    }

    public void setViewModel(ExpenseViewModel vm) {
        this.expenseVm = vm;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_expense_only, null);

        TextInputEditText etDesc = v.findViewById(R.id.etDescription);
        TextInputEditText etAmount = v.findViewById(R.id.etAmount);
        Button btnCancel = v.findViewById(R.id.btnCancel);
        Button btnSave = v.findViewById(R.id.btnSave);

        // Category chips
        com.google.android.material.chip.ChipGroup chipGroupCategory = v.findViewById(R.id.chipGroupCategory);

        // Payment chips
        com.google.android.material.chip.ChipGroup chipGroupPayment = v.findViewById(R.id.chipGroupPayment);

        AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(v).create();

        btnCancel.setOnClickListener(v1 -> dismiss());

        btnSave.setOnClickListener(v1 -> {
            String desc = etDesc.getText().toString().trim();
            String amt = etAmount.getText().toString().trim();

            if (desc.isEmpty()) {
                etDesc.setError("Required");
                return;
            }
            if (amt.isEmpty()) {
                etAmount.setError("Required");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amt);
            } catch (NumberFormatException e) {
                etAmount.setError("Invalid number");
                return;
            }

            // Get selected category
            int checkedCatId = chipGroupCategory.getCheckedChipId();
            String category = "Others";
            if (checkedCatId == R.id.chipFood) category = "Food";
            else if (checkedCatId == R.id.chipTransport) category = "Transport";
            else if (checkedCatId == R.id.chipShopping) category = "Shopping";
            else if (checkedCatId == R.id.chipBills) category = "Bills";
            else if (checkedCatId == R.id.chipEntertainment) category = "Entertainment";

            // Get selected payment method
            int checkedPayId = chipGroupPayment.getCheckedChipId();
            String paymentMethod = "Cash";
            if (checkedPayId == R.id.chipGCash) paymentMethod = "GCash";
            else if (checkedPayId == R.id.chipBPI) paymentMethod = "BPI";
            else if (checkedPayId == R.id.chipPayMaya) paymentMethod = "PayMaya";

            // Create expense with description including category
            String fullDesc = category + " - " + desc;
            Expense expense = new Expense(fullDesc, amount, momentId, Expense.TYPE_EXPENSES, paymentMethod);

            if (expenseVm != null) {
                expenseVm.insert(expense);
                Toast.makeText(getContext(), "Expense added", Toast.LENGTH_SHORT).show();
            }

            if (onSaved != null) onSaved.run();
            dismiss();
        });

        return dialog;
    }
}