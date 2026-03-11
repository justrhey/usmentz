package com.example.usmentz.model;

import java.util.Calendar;

public class CalendarDay {
    private final int day;        // 0 = empty cell
    private final Calendar date;
    private boolean hasMoment;
    private boolean hasExpense;
    private boolean isSelected;
    private boolean isToday;

    public CalendarDay(int day, Calendar date) {
        this.day  = day;
        this.date = date;
    }

    public int getDay()            { return day; }
    public Calendar getDate()      { return date; }
    public boolean hasMoment()     { return hasMoment; }
    public boolean hasExpense()    { return hasExpense; }
    public boolean isSelected()    { return isSelected; }
    public boolean isToday()       { return isToday; }

    public void setHasMoment(boolean v)  { hasMoment  = v; }
    public void setHasExpense(boolean v) { hasExpense = v; }
    public void setSelected(boolean v)   { isSelected = v; }
    public void setToday(boolean v)      { isToday    = v; }
}