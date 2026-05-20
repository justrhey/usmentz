package com.example.usmentz.adapter;

import com.example.usmentz.date.DateLocation;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Unit tests for DateAdapter crash-prone areas.
 */
public class DateAdapterTest {

    private DateAdapter adapter;

    @Before
    public void setUp() {
        adapter = new DateAdapter();
    }

    @Test
    public void testEmptyListDoesNotCrash() {
        adapter.setDates(new ArrayList<>());
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testNullListDoesNotCrash() {
        adapter.setDates(null);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testSetDatesWithValidList() {
        List<DateLocation> dates = new ArrayList<>();
        dates.add(new DateLocation("Test", "", "", new Date()));
        adapter.setDates(dates);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testGetItemIdWithValidData() {
        List<DateLocation> dates = new ArrayList<>();
        DateLocation dl = new DateLocation("Test", "", "", new Date());
        dl.setId(42);
        dates.add(dl);
        adapter.setDates(dates);
        assertEquals(42, adapter.getItemId(0));
    }

    @Test
    public void testCompletionToggleDoesNotCrashOnNull() {
        adapter.setOnItemClickListener(null);
        adapter.setOnItemDeleteListener(null);
        adapter.setOnItemCompleteListener(null);
        // No crash expected
    }

    @Test
    public void testPositionBounds() {
        List<DateLocation> dates = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            DateLocation dl = new DateLocation("Item " + i, "", "", new Date());
            dl.setId(i + 1);
            dates.add(dl);
        }
        adapter.setDates(dates);
        assertEquals(5, adapter.getItemCount());
        for (int i = 0; i < 5; i++) {
            assertTrue(adapter.getItemId(i) > 0);
        }
    }

    @Test
    public void testDateLocationNullFields() {
        DateLocation dl = new DateLocation();
        assertNull(dl.getName());
        assertNull(dl.getAddress());
        assertNull(dl.getDescription());
        assertNull(dl.getDate());
        assertFalse(dl.isCompleted());
        assertEquals(0f, dl.getRating(), 0.01f);
    }

    @Test
    public void testDateLocationConstructorDefaults() {
        Date now = new Date();
        DateLocation dl = new DateLocation("Test", "Address", "Desc", now);
        assertEquals("Test", dl.getName());
        assertEquals("Address", dl.getAddress());
        assertEquals("Desc", dl.getDescription());
        assertEquals(now, dl.getDate());
        assertFalse(dl.isCompleted());
        assertEquals(1, dl.getCategoryId());
    }
}
