package com.example.usmentz;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.usmentz.category.Category;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.viewmodel.CategoryViewModel;
import com.example.usmentz.viewmodel.DateViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddMomentDialog extends DialogFragment {

    private DateViewModel dateViewModel;
    private CategoryViewModel categoryViewModel;
    private Category selectedCategory;
    private OnMomentAddedListener listener;

    public interface OnMomentAddedListener {
        void onMomentAdded();
    }

    public static AddMomentDialog newInstance(OnMomentAddedListener listener) {
        AddMomentDialog dialog = new AddMomentDialog();
        dialog.listener = listener;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dateViewModel = new ViewModelProvider(requireActivity()).get(DateViewModel.class);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_moment, null);

        AutoCompleteTextView actvCategory = view.findViewById(R.id.actvCategory);
        EditText etName = view.findViewById(R.id.etName);
        EditText etAddress = view.findViewById(R.id.etAddress);
        EditText etDescription = view.findViewById(R.id.etDescription);
        TextView tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        Button btnSelectDate = view.findViewById(R.id.btnSelectDate);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnSave = view.findViewById(R.id.btnSave);

        // Load categories
        categoryViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && !categories.isEmpty()) {
                String[] categoryNames = new String[categories.size()];
                for (int i = 0; i < categories.size(); i++) {
                    categoryNames[i] = categories.get(i).getName();
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line, categoryNames);
                actvCategory.setAdapter(adapter);
                actvCategory.setOnItemClickListener((parent, v, position, id) -> {
                    selectedCategory = categories.get(position);
                });
            }
        });

        // Set default date
        SimpleDateFormat fmt = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        tvSelectedDate.setText(fmt.format(cal.getTime()));

        // Date picker
        btnSelectDate.setOnClickListener(v -> {
            new DatePickerDialog(requireContext(),
                    (d, m, day, year) -> {
                        cal.set(year, m, day);
                        tvSelectedDate.setText(fmt.format(cal.getTime()));
                    },
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setView(view);
        Dialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (selectedCategory == null) {
                Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(name)) {
                etName.setError("Required");
                return;
            }
            if (TextUtils.isEmpty(address)) {
                etAddress.setError("Required");
                return;
            }

            DateLocation moment = new DateLocation(name, address, description, cal.getTime());
            moment.setCategoryId(selectedCategory.getId());
            dateViewModel.insert(moment);

            Toast.makeText(getContext(), "Moment added to " + selectedCategory.getName(), Toast.LENGTH_SHORT).show();

            if (listener != null) {
                listener.onMomentAdded();
            }
            dialog.dismiss();
        });

        return dialog;
    }
}