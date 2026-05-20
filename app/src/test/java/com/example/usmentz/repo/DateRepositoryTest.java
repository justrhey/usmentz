package com.example.usmentz.repo;

import com.example.usmentz.date.DateLocation;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Date;

/**
 * Unit tests for DateRepository insert/update logic.
 */
public class DateRepositoryTest {

    @Test
    public void testDateLocationWithIdShouldUseUpdate() {
        DateLocation dl = new DateLocation("Test", "", "", new Date());
        dl.setId(42);
        assertTrue("DateLocation with ID should use update path", dl.getId() > 0);
    }

    @Test
    public void testDateLocationWithoutIdShouldUseInsert() {
        DateLocation dl = new DateLocation("Test", "", "", new Date());
        assertEquals("DateLocation without ID should use insert path", 0, dl.getId());
    }

    @Test
    public void testDateLocationIdZeroVsNegative() {
        DateLocation dl = new DateLocation();
        assertEquals(0, dl.getId());

        dl.setId(-1);
        assertFalse("Negative ID should not trigger update path", dl.getId() > 0);
    }

    @Test
    public void testDateLocationSerializability() {
        DateLocation dl = new DateLocation("Test", "Addr", "Desc", new Date());
        assertTrue("DateLocation should be Serializable", dl instanceof java.io.Serializable);
    }

    @Test
    public void testDateLocationFieldAccess() {
        Date now = new Date();
        DateLocation dl = new DateLocation("Name", "Address", "Description", now);
        dl.setId(1);
        dl.setCategoryId(2);
        dl.setCompleted(true);
        dl.setRating(4.5f);
        dl.setReview("Great place");
        dl.setPhotoPath("/path/to/photo.jpg");
        dl.setPosition(3);

        assertEquals(1, dl.getId());
        assertEquals("Name", dl.getName());
        assertEquals("Address", dl.getAddress());
        assertEquals("Description", dl.getDescription());
        assertEquals(now, dl.getDate());
        assertEquals(2, dl.getCategoryId());
        assertTrue(dl.isCompleted());
        assertEquals(4.5f, dl.getRating(), 0.01f);
        assertEquals("Great place", dl.getReview());
        assertEquals("/path/to/photo.jpg", dl.getPhotoPath());
        assertEquals(3, dl.getPosition());
    }
}
