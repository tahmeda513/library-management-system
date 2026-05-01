package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * InputValidator – centralised input validation utility.
 *
 * OOP concepts: Utility class with static methods (stateless service).
 * All validation rules defined in one place for easy maintenance.
 */
public final class InputValidator {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd

    // Prevent instantiation of utility class
    private InputValidator() {}

    /** Returns true if the string is non-null and non-blank. */
    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    /** Returns true if the value is a valid positive integer. */
    public static boolean isPositiveInteger(String value) {
        try {
            return Integer.parseInt(value.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Returns true if the string matches a valid email format. */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /** Returns true if the string is a valid ISO date (yyyy-MM-dd). */
    public static boolean isValidDate(String date) {
        if (date == null || date.isBlank()) return false;
        try {
            LocalDate.parse(date.trim(), DATE_FORMAT);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Returns true if dueDate is strictly after borrowDate.
     * Both must be valid dates.
     */
    public static boolean isDueDateAfterBorrowDate(String borrowDate, String dueDate) {
        try {
            LocalDate borrow = LocalDate.parse(borrowDate.trim(), DATE_FORMAT);
            LocalDate due = LocalDate.parse(dueDate.trim(), DATE_FORMAT);
            return due.isAfter(borrow);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /** Returns true if the membership type is either 'Student' or 'Staff'. */
    public static boolean isValidMembershipType(String type) {
        return "Student".equalsIgnoreCase(type) || "Staff".equalsIgnoreCase(type);
    }

    /** Returns true if the availability status is 'Available' or 'Borrowed'. */
    public static boolean isValidAvailabilityStatus(String status) {
        return "Available".equalsIgnoreCase(status) || "Borrowed".equalsIgnoreCase(status);
    }

    /** Returns true if the return status is one of the recognised values. */
    public static boolean isValidReturnStatus(String status) {
        return "Borrowed".equalsIgnoreCase(status)
            || "Returned".equalsIgnoreCase(status)
            || "Overdue".equalsIgnoreCase(status);
    }

    /** Parses a string to LocalDate; returns null on failure. */
    public static LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
