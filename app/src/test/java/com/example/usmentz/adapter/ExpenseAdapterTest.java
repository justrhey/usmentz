package com.example.usmentz.adapter;

import com.example.usmentz.fina.Expense;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for ExpenseAdapter crash-prone areas.
 */
public class ExpenseAdapterTest {

    private ExpenseAdapter adapter;

    @Before
    public void setUp() {
        adapter = new ExpenseAdapter();
    }

    @Test
    public void testEmptyListDoesNotCrash() {
        adapter.setExpenses(new ArrayList<>());
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testNullListDoesNotCrash() {
        adapter.setExpenses(null);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testGetItemIdWithValidData() {
        List<Expense> expenses = new ArrayList<>();
        Expense exp = new Expense("Test", 100.0, 1, Expense.TYPE_EXPENSES, "Cash");
        exp.setId(99);
        expenses.add(exp);
        adapter.setExpenses(expenses);
        assertEquals(99, adapter.getItemId(0));
    }

    @Test
    public void testExpenseWithNullPaymentMethod() {
        Expense exp = new Expense("Test", 50.0, 1, Expense.TYPE_EXPENSES, null);
        assertNull(exp.getPaymentMethod());
    }

    @Test
    public void testExpenseWithNullType() {
        Expense exp = new Expense("Test", 50.0, 1, null, "Cash");
        assertNull(exp.getType());
    }

    @Test
    public void testSortOptions() {
        List<Expense> expenses = new ArrayList<>();
        expenses.add(new Expense("A", 100.0, 1, Expense.TYPE_EXPENSES, "Cash"));
        expenses.add(new Expense("B", 50.0, 1, Expense.TYPE_EXPENSES, "Cash"));
        expenses.add(new Expense("C", 200.0, 1, Expense.TYPE_EXPENSES, "Cash"));
        adapter.setExpenses(expenses);

        adapter.setSortOption(ExpenseAdapter.SortOption.NEWEST_FIRST);
        adapter.setSortOption(ExpenseAdapter.SortOption.OLDEST_FIRST);
        adapter.setSortOption(ExpenseAdapter.SortOption.HIGHEST_AMOUNT);
        adapter.setSortOption(ExpenseAdapter.SortOption.LOWEST_AMOUNT);

        assertEquals(3, adapter.getItemCount());
    }

    @Test
    public void testDeleteListenerNullSafety() {
        adapter.setOnExpenseDeleteListener(null);
        // No crash expected
    }

    @Test
    public void testExpenseConstructorDefaults() {
        Expense exp = new Expense("Test", 100.0, 1, Expense.TYPE_EXPENSES, "Cash");
        assertEquals("Test", exp.getDescription());
        assertEquals(100.0, exp.getAmount(), 0.01);
        assertEquals(1, exp.getMomentId());
        assertEquals(Expense.TYPE_EXPENSES, exp.getType());
        assertEquals("Cash", exp.getPaymentMethod());
    }
}
