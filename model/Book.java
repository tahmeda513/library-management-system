package model;

/**
 * Book – represents a library book entity.
 *
 * OOP concepts: Encapsulation (private fields + getters/setters),
 * Inheritance (extends LibraryEntity), Polymorphism (overrides getSummary).
 */
public class Book extends LibraryEntity {

    public enum AvailabilityStatus { Available, Borrowed }

    private int bookId;
    private String title;
    private String author;
    private String category;
    private String availabilityStatus;

    public Book() {}

    public Book(int bookId, String title, String author, String category, String availabilityStatus) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.category = category;
        this.availabilityStatus = availabilityStatus;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────
    @Override
    public int getId() { return bookId; }
    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public boolean isAvailable() {
        return "Available".equalsIgnoreCase(availabilityStatus);
    }

    // ── Polymorphic override ───────────────────────────────────────────────────
    @Override
    public String getSummary() {
        return String.format("BOOK ID: %d | Title: %-35s | Author: %-20s | Category: %-20s | Status: %s",
                bookId, title, author, category, availabilityStatus);
    }
}
