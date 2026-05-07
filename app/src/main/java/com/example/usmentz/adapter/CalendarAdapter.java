package com.example.usmentz.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
            // Empty cell - hide content
            h.tvDay.setText("");
            h.viewTodayBorder.setVisibility(View.GONE);
            h.cellBackground.setVisibility(View.GONE);
            h.viewDimmed.setVisibility(View.VISIBLE);
            h.itemView.setOnClickListener(null);
            return;
        }

        // Show cell background
        h.cellBackground.setVisibility(View.VISIBLE);
        h.viewDimmed.setVisibility(View.GONE);

        // Day number
        h.tvDay.setText(String.valueOf(day.getDay()));

        // Today - show purple top border
        if (day.isToday()) {
            h.viewTodayBorder.setVisibility(View.VISIBLE);
            h.tvDay.setTypeface(null, Typeface.BOLD);
            h.tvDay.setTextColor(0xFF9B5CFF); // Purple
        } else {
            h.viewTodayBorder.setVisibility(View.GONE);
            h.tvDay.setTypeface(null, Typeface.NORMAL);
            h.tvDay.setTextColor(0xFF212121); // Dark
        }

        // Selected state
        if (day.isSelected()) {
            h.cellBackground.setBackgroundColor(0xFFF8F5FF); // Light purple bg
        } else {
            h.cellBackground.setBackgroundColor(0xFFFFFFFF); // White
        }

        // Event count badge
        int eventCount = (day.hasMoment() ? 1 : 0) + (day.hasExpense() ? 1 : 0);
        if (eventCount > 0) {
            h.tvEventCount.setVisibility(View.VISIBLE);
            h.tvEventCount.setText(eventCount > 9 ? "9+" : String.valueOf(eventCount));
        } else {
            h.tvEventCount.setVisibility(View.GONE);
        }

        // Hide event blocks (placeholder for future)
        if (h.tvEvent1 != null) h.tvEvent1.setVisibility(View.GONE);
        if (h.tvEvent2 != null) h.tvEvent2.setVisibility(View.GONE);
        if (h.tvEvent3 != null) h.tvEvent3.setVisibility(View.GONE);
        if (h.tvMoreEvents != null) h.tvMoreEvents.setVisibility(View.GONE);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDayClick(day);
        });
    }

    @Override
    public int getItemCount() { return days.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;
        TextView tvEventCount;
        TextView tvEvent1, tvEvent2, tvEvent3, tvMoreEvents;
        View viewTodayBorder;
        View viewDimmed;
        LinearLayout cellBackground;
        LinearLayout eventContainer;

        ViewHolder(@NonNull View v) {
            super(v);
            tvDay = v.findViewById(R.id.tvDay);
            tvEventCount = v.findViewById(R.id.tvEventCount);
            viewTodayBorder = v.findViewById(R.id.viewTodayBorder);
            cellBackground = v.findViewById(R.id.cellBackground);
            eventContainer = v.findViewById(R.id.eventContainer);
            viewDimmed = v.findViewById(R.id.viewDimmed);
            
            // Event blocks (may not exist in current layout)
            tvEvent1 = v.findViewById(R.id.tvEvent1);
            tvEvent2 = v.findViewById(R.id.tvEvent2);
            tvEvent3 = v.findViewById(R.id.tvEvent3);
            tvMoreEvents = v.findViewById(R.id.tvMoreEvents);
        }
    }
}