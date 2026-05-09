package com.example.usmentz.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
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

    public void setDays(List<CalendarDay> newDays) {
        if (newDays == null) newDays = new ArrayList<>();
        final List<CalendarDay> finalNewDays = newDays;

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return days.size(); }

            @Override
            public int getNewListSize() { return finalNewDays.size(); }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                CalendarDay oldDay = days.get(oldPos);
                CalendarDay newDay = finalNewDays.get(newPos);
                return oldDay.getDay() == newDay.getDay()
                        && oldDay.getDate() == newDay.getDate();
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                CalendarDay oldDay = days.get(oldPos);
                CalendarDay newDay = finalNewDays.get(newPos);
                return oldDay.isSelected() == newDay.isSelected()
                        && oldDay.isToday() == newDay.isToday()
                        && oldDay.hasMoment() == newDay.hasMoment();
            }
        });

        days = new ArrayList<>(newDays);
        diff.dispatchUpdatesTo(this);
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
            h.tvDay.setText("");
            h.viewTodayCircle.setVisibility(View.GONE);
            h.viewSelectedCircle.setVisibility(View.GONE);
            h.strip1.setVisibility(View.GONE);
            h.itemView.setClickable(false);
            h.itemView.setOnClickListener(null);
            return;
        }

        h.tvDay.setText(String.valueOf(day.getDay()));
        h.viewTodayCircle.setVisibility(day.isToday() ? View.VISIBLE : View.GONE);
        h.viewSelectedCircle.setVisibility(day.isSelected() ? View.VISIBLE : View.GONE);
        h.strip1.setVisibility(day.hasMoment() ? View.VISIBLE : View.GONE);
        if (day.hasMoment()) {
            String label = day.getMomentLabel();
            h.strip1.setText(label != null && !label.isEmpty() ? label : "");
        }

        h.itemView.setClickable(true);
        h.itemView.setTag(day);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder h) {
        super.onViewAttachedToWindow(h);
        h.itemView.setOnClickListener(v -> {
            if (listener != null) {
                Object tag = v.getTag();
                if (tag instanceof CalendarDay) {
                    listener.onDayClick((CalendarDay) tag);
                }
            }
        });
    }

    @Override
    public int getItemCount() { return days.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;
        View viewTodayCircle;
        View viewSelectedCircle;
        TextView strip1;

        ViewHolder(@NonNull View v) {
            super(v);
            tvDay = v.findViewById(R.id.tvDay);
            viewTodayCircle = v.findViewById(R.id.viewTodayCircle);
            viewSelectedCircle = v.findViewById(R.id.viewSelectedCircle);
            strip1 = v.findViewById(R.id.strip1);
        }
    }
}