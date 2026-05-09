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

public class AddFundsOnlyDialog extends DialogFragment {

    private ExpenseViewModel expenseVm;
    private int momentId;
    private Runnable onSaved;

    public static AddFundsOnlyDialog newInstance(int momentId, Runnable onSaved) {
        AddFundsOnlyDialog frag = new AddFundsOnlyDialog();
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
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_funds_only, null);

        TextInputEditText etDesc = v.findViewById(R.id.etDescription);
        TextInputEditText etAmount = v.findViewById(R.id.etAmount);
        Button btnCancel = v.findViewById(R.id.btnCancel);
        Button btnSave = v.findViewById(R.id.btnSave);

        com.google.android.material.chip.ChipGroup chipGroupSource = v.findViewById(R.id.chipGroupSource);
        com.google.android.material.chip.ChipGroup chipGroupAccount = v.findViewById(R.id.chipGroupAccount);

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

            // Get selected source
            int checkedSrcId = chipGroupSource.getCheckedChipId();
            String source = "Other";
            if (checkedSrcId == R.id.chipSalary) source = "Salary";
            else if (checkedSrcId == R.id.chipFreelance) source = "Freelance";
            else if (checkedSrcId == R.id.chipBusiness) source = "Business";
            else if (checkedSrcId == R.id.chipGift) source = "Gift";
            else if (checkedSrcId == R.id.chipRefund) source = "Refund";

            // Get selected account
            int checkedAccId = chipGroupAccount.getCheckedChipId();
            String account = "Bank";
            if (checkedAccId == R.id.chipGCashAccount) account = "GCash";
            else if (checkedAccId == R.id.chipPayMayaAccount) account = "PayMaya";
            else if (checkedAccId == R.id.chipCashAccount) account = "Cash";

            String fullDesc = source + " - " + desc;
            Expense expense = new Expense(fullDesc, amount, momentId, Expense.TYPE_FUNDS, account);

            if (expenseVm != null) {
                expenseVm.insert(expense);
                Toast.makeText(getContext(), "Income added", Toast.LENGTH_SHORT).show();
            }

            if (onSaved != null) onSaved.run();
            dismiss();
        });

        return dialog;
    }
}