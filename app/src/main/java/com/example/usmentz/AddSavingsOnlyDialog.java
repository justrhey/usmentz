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

public class AddSavingsOnlyDialog extends DialogFragment {

    private ExpenseViewModel expenseVm;
    private int momentId;
    private Runnable onSaved;

    public static AddSavingsOnlyDialog newInstance(int momentId, Runnable onSaved) {
        AddSavingsOnlyDialog frag = new AddSavingsOnlyDialog();
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
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_savings_only, null);

        TextInputEditText etDesc = v.findViewById(R.id.etDescription);
        TextInputEditText etAmount = v.findViewById(R.id.etAmount);
        Button btnCancel = v.findViewById(R.id.btnCancel);
        Button btnSave = v.findViewById(R.id.btnSave);

        com.google.android.material.chip.ChipGroup chipGroupGoal = v.findViewById(R.id.chipGroupGoal);

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

            // Get selected goal
            int checkedGoalId = chipGroupGoal.getCheckedChipId();
            String goal = "General";
            if (checkedGoalId == R.id.chipTravel) goal = "Travel";
            else if (checkedGoalId == R.id.chipEmergency) goal = "Emergency";
            else if (checkedGoalId == R.id.chipHome) goal = "Home";
            else if (checkedGoalId == R.id.chipEducation) goal = "Education";
            else if (checkedGoalId == R.id.chipGadget) goal = "Gadget";

            String fullDesc = "Savings - " + goal + " - " + desc;
            Expense expense = new Expense(fullDesc, amount, momentId, Expense.TYPE_SAVINGS, "Savings");

            if (expenseVm != null) {
                expenseVm.insert(expense);
                Toast.makeText(getContext(), "Savings added", Toast.LENGTH_SHORT).show();
            }

            if (onSaved != null) onSaved.run();
            dismiss();
        });

        return dialog;
    }
}