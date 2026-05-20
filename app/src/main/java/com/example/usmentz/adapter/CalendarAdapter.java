package com.example.usmentz.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.example.usmentz.R;
import com.example.usmentz.model.CalendarDay;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    private List<CalendarDay> days = new ArrayList<>();
    private OnDayClickListener listener;

    public CalendarAdapter() {
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public interface OnDayClickListener {
        void onDayClick(CalendarDay day);
    }

    public void setOnDayClickListener(OnDayClickListener l) { this.listener = l; }

    public void setDays(List<CalendarDay> newDays) {
        if (newDays == null) newDays = new ArrayList<>();
        final List<CalendarDay> finalNewDays = new ArrayList<>(newDays);

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return days.size(); }
            @Override
            public int getNewListSize() { return finalNewDays.size(); }
            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                CalendarDay oldDay = days.get(oldPos);
                CalendarDay newDay = finalNewDays.get(newPos);
                if (oldDay.getDay() != newDay.getDay()) return false;
                Calendar oldDate = oldDay.getDate();
                Calendar newDate = newDay.getDate();
                if (oldDate == null || newDate == null) return false;
                return oldDate.get(Calendar.YEAR) == newDate.get(Calendar.YEAR)
                        && oldDate.get(Calendar.MONTH) == newDate.get(Calendar.MONTH);
            }
            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                CalendarDay oldDay = days.get(oldPos);
                CalendarDay newDay = finalNewDays.get(newPos);
                return oldDay.isSelected() == newDay.isSelected()
                        && oldDay.isToday() == newDay.isToday()
                        && oldDay.getMomentCount() == newDay.getMomentCount();
            }
        }, true);

        days = finalNewDays;
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
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        CalendarDay day = days.get(pos);

        if (day.getDay() == 0) {
            h.tvDay.setText("");
            h.viewTodayCircle.setVisibility(View.GONE);
            h.viewSelectedCircle.setVisibility(View.GONE);
            h.capsuleContainer.setVisibility(View.GONE);
            h.itemView.setClickable(false);
            h.itemView.setOnClickListener(null);
            return;
        }

        h.tvDay.setText(String.valueOf(day.getDay()));
        h.viewTodayCircle.setVisibility(day.isToday() ? View.VISIBLE : View.GONE);
        h.viewSelectedCircle.setVisibility(day.isSelected() ? View.VISIBLE : View.GONE);

        List<String> labels = day.getMomentLabels();
        int count = day.getMomentCount();

        // Capsule: show if day has moments and is selected or today
        if (count > 0 && labels != null && !labels.isEmpty() && (day.isSelected() || day.isToday())) {
            h.capsuleContainer.setVisibility(View.VISIBLE);
            h.tvDay.setVisibility(View.GONE);
            h.viewTodayCircle.setVisibility(View.GONE);
            h.viewSelectedCircle.setVisibility(View.GONE);
            h.tvCapsuleDay.setText(String.valueOf(day.getDay()));
            h.tvCapsuleLabel.setText(labels.get(0));
        } else {
            h.capsuleContainer.setVisibility(View.GONE);
            h.tvDay.setVisibility(View.VISIBLE);

            // Low-emphasis for boundary cells (first/last row)
            if (pos < 7 || pos > days.size() - 7) {
                h.tvDay.setTextColor(0xFFBDBDBD);
            } else {
                h.tvDay.setTextColor(0xFF2D2640);
            }
        }

        h.itemView.setClickable(true);
        h.itemView.setTag(day);
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
        LinearLayout capsuleContainer;
        TextView tvCapsuleDay, tvCapsuleLabel;

        ViewHolder(@NonNull View v) {
            super(v);
            tvDay = v.findViewById(R.id.tvDay);
            viewTodayCircle = v.findViewById(R.id.viewTodayCircle);
            viewSelectedCircle = v.findViewById(R.id.viewSelectedCircle);
            capsuleContainer = v.findViewById(R.id.capsuleContainer);
            tvCapsuleDay = v.findViewById(R.id.tvCapsuleDay);
            tvCapsuleLabel = v.findViewById(R.id.tvCapsuleLabel);
        }
    }
}
