package com.example.usmentz.adapter;

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
            h.itemView.setClickable(false);
            return;
        }

        // Day number
        h.tvDay.setText(String.valueOf(day.getDay()));

        // Selected state - show/hide selected circle
        if (day.isSelected()) {
            h.viewSelectedCircle.setVisibility(View.VISIBLE);
        } else {
            h.viewSelectedCircle.setVisibility(View.GONE);
        }

        // Today state - show/hide today circle
        if (day.isToday()) {
            h.viewTodayCircle.setVisibility(View.VISIBLE);
        } else {
            h.viewTodayCircle.setVisibility(View.GONE);
        }

        // Event dot indicator
        h.viewEventDot.setVisibility(day.hasMoment() ? View.VISIBLE : View.GONE);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDayClick(day);
        });
    }

    @Override
    public int getItemCount() { return days.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;
        View viewEventDot;
        View viewTodayCircle;
        View viewSelectedCircle;

        ViewHolder(@NonNull View v) {
            super(v);
            tvDay = v.findViewById(R.id.tvDay);
            viewEventDot = v.findViewById(R.id.viewEventDot);
            viewTodayCircle = v.findViewById(R.id.viewTodayCircle);
            viewSelectedCircle = v.findViewById(R.id.viewSelectedCircle);
        }
    }
}