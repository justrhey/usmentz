package com.example.usmentz.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarDay {
    private final int day;        // 0 = empty cell
    private final Calendar date;
    private boolean hasExpense;
    private boolean isSelected;
    private boolean isToday;
    private int momentCount;
    private List<String> momentLabels = new ArrayList<>();

    public CalendarDay(int day, Calendar date) {
        this.day  = day;
        this.date = date;
    }

    public int getDay()            { return day; }
    public Calendar getDate()      { return date; }
    public boolean hasExpense()    { return hasExpense; }
    public boolean isSelected()    { return isSelected; }
    public boolean isToday()       { return isToday; }
    public int getMomentCount()   { return momentCount; }
    public List<String> getMomentLabels() { return momentLabels; }

    public boolean hasMoment()     { return momentCount > 0; }

    public void setHasExpense(boolean v)  { hasExpense = v; }
    public void setSelected(boolean v)    { isSelected = v; }
    public void setToday(boolean v)       { isToday    = v; }
    public void setMomentCount(int v)     { momentCount = v; }

    public void setMomentLabels(List<String> labels) {
        this.momentLabels = labels != null ? new ArrayList<>(labels) : new ArrayList<>();
        this.momentCount = this.momentLabels.size();
    }
}