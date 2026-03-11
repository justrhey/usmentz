package com.example.usmentz.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usmentz.R;
import com.example.usmentz.model.CalendarDay;

import java.util.ArrayList;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    private List<CalendarDay> days = new ArrayList<>();
    private OnDayClickListener listener;

    public interface OnDayClickListener {
        void onDayClick(CalendarDay day);
    }

    public void setOnDayClickListener(OnDayClickListener l) { this.listener = l; }

    public void setDays(List<CalendarDay> days) {
        this.days = days != null ? days : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        CalendarDay day = days.get(position);

        if (day.getDay() == 0) {
            // Empty cell
            h.tvDay.setText("");
            h.viewBg.setVisibility(View.INVISIBLE);
            h.dotMoment.setVisibility(View.INVISIBLE);
            h.dotExpense.setVisibility(View.INVISIBLE);
            h.itemView.setOnClickListener(null);
            return;
        }

        h.tvDay.setText(String.valueOf(day.getDay()));

        // Today — bold
        h.tvDay.setTypeface(null, day.isToday() ? Typeface.BOLD : Typeface.NORMAL);

        // Selected — show purple circle, white text
        if (day.isSelected()) {
            h.viewBg.setVisibility(View.VISIBLE);
            h.tvDay.setTextColor(0xFFFFFFFF);
        } else if (day.isToday()) {
            h.viewBg.setVisibility(View.INVISIBLE);
            h.tvDay.setTextColor(0xFF9C27B0); // purple for today
        } else {
            h.viewBg.setVisibility(View.INVISIBLE);
            h.tvDay.setTextColor(0xFFFFFFFF);
        }

        // Dots
        h.dotMoment.setVisibility(day.hasMoment()  ? View.VISIBLE : View.INVISIBLE);
        h.dotExpense.setVisibility(day.hasExpense() ? View.VISIBLE : View.INVISIBLE);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDayClick(day);
        });
    }

    @Override
    public int getItemCount() { return days.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;
        View viewBg, dotMoment, dotExpense;

        ViewHolder(@NonNull View v) {
            super(v);
            tvDay      = v.findViewById(R.id.tvDay);
            viewBg     = v.findViewById(R.id.viewDayBg);
            dotMoment  = v.findViewById(R.id.dotMoment);
            dotExpense = v.findViewById(R.id.dotExpense);
        }
    }
}